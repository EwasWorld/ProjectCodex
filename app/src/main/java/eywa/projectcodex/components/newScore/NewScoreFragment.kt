package eywa.projectcodex.components.newScore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.R
import eywa.projectcodex.common.elements.DatePickerDialog
import eywa.projectcodex.common.elements.TimePickerDialog
import eywa.projectcodex.common.utils.ActionBarHelp
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.common.utils.resourceStringReplace
import eywa.projectcodex.database.archerRound.ArcherRound
import kotlinx.android.synthetic.main.fragment_new_score.*
import java.util.*

class NewScoreFragment : Fragment(), ActionBarHelp {
    private val args: NewScoreFragmentArgs by navArgs()
    private lateinit var newRoundViewModel: NewScoreViewModel

    /**
     * Round information for the two round selection spinners
     */
    private lateinit var roundSelection: RoundSelection

    /**
     * The date and time currently inputted
     */
    private var date = Calendar.getInstance()

    /**
     * Used to check whether the rounds spinner can be displayed (doesn't display until update is complete
     */
    private var updateDefaultRoundsState = UpdateDefaultRounds.UpdateTaskState.NOT_STARTED

    /**
     * Null if the information is for a new score, else is the database's information about the round being edited
     */
    private var archerRound: ArcherRound? = null

    /**
     * How many arrows have currently been added to the round (always 0 if creating a new round).
     * Used to prevent the round being changed to one where the arrowsShot exceeds the number of arrows in the round
     */
    private var arrowsShot = 0

    /**
     * Used when the round spinners are in the process being set to match the information in [archerRound]
     */
    private var resetModeSubTypeId: Int? = null

    private val datePickerDialog: DatePickerDialog by lazy {
        val okListener = object : DatePickerDialog.OnOkListener {
            override fun onSelect(value: Calendar) {
                date = value
                updateDateTime()
            }
        }
        DatePickerDialog(
                getString(R.string.create_round__date_shot_date_picker_title), null, null, null, date, okListener
        )
    }

    private val timePickerDialog: TimePickerDialog by lazy {
        val okListener = object : TimePickerDialog.OnOkListener {
            override fun onSelect(hours: Int, minutes: Int) {
                date.set(Calendar.MINUTE, minutes)
                date.set(Calendar.HOUR_OF_DAY, hours)
                date.set(Calendar.SECOND, 0)
                date.set(Calendar.MILLISECOND, 0)
                updateDateTime()
            }
        }
        TimePickerDialog(
                getString(R.string.create_round__time_shot_time_picker_title), null, date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE), DateTimeFormat.TIME_FORMAT.pattern.contains("HH"), okListener
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_score, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // True if the fragment is being used to edit an existing round
        val isInEditMode = args.archerRoundId != -1

        activity?.title = getString(
                if (!isInEditMode) R.string.create_round__title else R.string.create_round__edit_title
        )

        var submitPressed = false
        newRoundViewModel = ViewModelProvider(this).get(NewScoreViewModel::class.java)
        roundSelection = RoundSelection(resources, newRoundViewModel, viewLifecycleOwner)

        if (isInEditMode) {
            newRoundViewModel.getArcherRound(args.archerRoundId).observe(viewLifecycleOwner, { ar ->
                ar?.let {
                    archerRound = it
                    setValuesFromArcherRound()
                }
            })
            newRoundViewModel.getArrowsForRound(args.archerRoundId).observe(viewLifecycleOwner, { arrows ->
                if (arrows != null) arrowsShot = arrows.count()
            })
            layout_create_round__edit_submit_buttons.visibility = View.VISIBLE
            layout_create_round__new_submit_buttons.visibility = View.GONE
        }
        else {
            layout_create_round__edit_submit_buttons.visibility = View.GONE
            layout_create_round__new_submit_buttons.visibility = View.VISIBLE
        }

        newRoundViewModel.maxId.observe(viewLifecycleOwner, { maxId ->
            if (submitPressed && maxId != null) {
                // When the new round entry has been added, open the input end dialog
                val action = NewScoreFragmentDirections.actionNewScoreFragmentToInputEndFragment(maxId)
                view.findNavController().navigate(action)
            }
        })

        updateDateTime()
        text_create_round__date.setOnClickListener {
            datePickerDialog.show(childFragmentManager, "date picker")
        }
        text_create_round__time.setOnClickListener {
            timePickerDialog.show(childFragmentManager, "time picker")
        }

        // Hide the round spinner if rounds are being updated
        newRoundViewModel.updateDefaultRoundsState.observe(viewLifecycleOwner, { state ->
            updateDefaultRoundsState = state
            val isInProgress = updateDefaultRoundsState == UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS
            fun getVisibility(isShown: Boolean) = if (isShown) View.VISIBLE else View.GONE
            text_create_round__default_rounds_updating_warning.visibility = getVisibility(isInProgress)
            text_create_round__default_rounds_updating_status.visibility = getVisibility(isInProgress)
            layout_create_round__round.visibility = getVisibility(!isInProgress)
        })
        newRoundViewModel.updateDefaultRoundsProgressMessage.observe(viewLifecycleOwner, { message ->
            val newText = when {
                message != null -> message
                updateDefaultRoundsState == UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS -> {
                    resources.getString(R.string.about__update_default_rounds_in_progress)
                }
                else -> ""
            }
            text_create_round__default_rounds_updating_status.updateText(newText)
        })

        // Update the spinners if the database updates (spinners don't display correctly at the start without this)
        newRoundViewModel.allRounds.observe(viewLifecycleOwner, {
            spinner_create_round__round.adapter = ArrayAdapter(
                    requireActivity().applicationContext, R.layout.spinner_light_background,
                    roundSelection.getAvailableRounds()
            )
            if (isInEditMode) {
                setValuesFromArcherRound(true)
            }
            else {
                spinner_create_round__round.setSelection(roundSelection.noRoundPosition)
            }
        })

        button_create_round__submit.setOnClickListener {
            val roundId = roundSelection.getSelectedRoundId()
            val roundSubtypeId = if (roundId != null) roundSelection.getSelectedSubtypeId() else null
            // TODO Check date locales (I want to store in UTC)
            newRoundViewModel.insert(
                    ArcherRound(0, date.time, 1, false, roundId = roundId, roundSubTypeId = roundSubtypeId)
            ).invokeOnCompletion {
                // Ensures that when max ID changes due to the newly created round, it will navigate to the input end
                //      screen
                submitPressed = true
            }
        }

        button_create_round__reset.setOnClickListener {
            setValuesFromArcherRound()
        }

        button_create_round__cancel.setOnClickListener {
            requireView().findNavController().popBackStack()
        }

        button_create_round__complete.setOnClickListener {
            val roundId = roundSelection.getSelectedRoundId()
            val roundSubtypeId = if (roundId != null) roundSelection.getSelectedSubtypeId() else null
            // TODO Check date locales (I want to store in UTC)
            archerRound?.let { ar ->
                newRoundViewModel.update(
                        ArcherRound(
                                ar.archerRoundId,
                                date.time,
                                ar.archerId,
                                ar.countsTowardsHandicap,
                                roundId = roundId,
                                roundSubTypeId = roundSubtypeId
                        )
                )
            }
            requireView().findNavController().popBackStack()
        }

        spinner_create_round__round.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            /**
             * Hide the subtype spinner if no round is selected
             * Populate the subtype spinner with the appropriate items
             * Update round info indicators as appropriate
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                roundSelection.selectedRoundPosition = position

                /*
                 * Check the round can be used
                 */
                var displayArrowCountWarning = false
                if (isInEditMode) {
                    val arrowsInSelectedRound = roundSelection.getTotalArrowsInRound()
                    if (arrowsInSelectedRound != null && arrowsInSelectedRound < arrowsShot) {
                        displayArrowCountWarning = true
                        text_create_round__too_many_arrows_warning.text = resourceStringReplace(
                                resources.getString(R.string.err_create_round__too_many_arrows),
                                mapOf(
                                        "round" to roundSelection.getSelectedRoundName()!!,
                                        "shot arrows" to arrowsShot.toString(),
                                        "round arrows" to arrowsInSelectedRound.toString()
                                )
                        )
                        text_create_round__too_many_arrows_warning.visibility = View.VISIBLE
                        button_create_round__complete.isEnabled = false
                    }
                }
                if (!displayArrowCountWarning) {
                    text_create_round__too_many_arrows_warning.visibility = View.GONE
                    button_create_round__complete.isEnabled = true
                }

                /*
                 * Hide everything for no round
                 */
                if (position == roundSelection.noRoundPosition) {
                    roundSelection.selectedSubtypePosition = null
                    layout_create_round__round_sub_type.visibility = View.GONE
                    text_create_round__arrow_count_indicator.visibility = View.GONE
                    text_create_round__distance_indicator.visibility = View.GONE
                    return
                }

                /*
                 * Setup the sub type spinner
                 */
                val roundSubTypes = roundSelection.getRoundSubtypes()
                if (roundSubTypes.isNullOrEmpty()) {
                    roundSelection.selectedSubtypePosition = null
                    layout_create_round__round_sub_type.visibility = View.GONE
                }
                else {
                    spinner_create_round__round_sub_type.adapter = ArrayAdapter(
                            requireActivity().applicationContext, R.layout.spinner_light_background, roundSubTypes
                    )

                    if (resetModeSubTypeId != null) {
                        val subTypePos = roundSelection.getPositionOfSubtype(
                                roundSelection.getSelectedRoundId(), resetModeSubTypeId!!
                        )
                        if (subTypePos != null) {
                            spinner_create_round__round_sub_type.setSelection(subTypePos)
                        }
                        else {
                            roundSelection.selectedSubtypePosition = null
                        }
                        resetModeSubTypeId = null
                    }

                    layout_create_round__round_sub_type.visibility = View.VISIBLE
                }

                setDistanceIndicatorText(roundSelection)

                /*
                 * Create the arrow count indicator string
                 */
                val arrowCountText = roundSelection.getArrowCountIndicatorText()
                if (arrowCountText != null) {
                    text_create_round__arrow_count_indicator.updateText(arrowCountText)
                    text_create_round__arrow_count_indicator.visibility = View.VISIBLE
                }
                else {
                    text_create_round__arrow_count_indicator.visibility = View.GONE
                }
            }
        }

        /**
         * Update distance indicators
         */
        spinner_create_round__round_sub_type.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                roundSelection.selectedSubtypePosition = position
                setDistanceIndicatorText(roundSelection)
            }
        }
    }

    private fun updateDateTime() {
        requireView().findViewById<TextView>(R.id.text_create_round__date).text =
                DateTimeFormat.LONG_DATE_FORMAT.format(date.time)
        requireView().findViewById<TextView>(R.id.text_create_round__time).text =
                DateTimeFormat.TIME_FORMAT.format(date.time)
    }

    private fun setValuesFromArcherRound(spinnersOnly: Boolean = false) {
        archerRound?.let { ar ->
            if (!spinnersOnly) {
                date.time = ar.dateShot
                updateDateTime()
            }

            /*
             * Set spinners
             */
            val roundPos = roundSelection.getPositionOfRound(ar.roundId)
            if (roundPos != null) {
                resetModeSubTypeId = ar.roundSubTypeId
                spinner_create_round__round.setSelection(roundPos)
            }
            else {
                roundSelection.selectedRoundPosition = roundSelection.noRoundPosition
            }
        }
    }

    /**
     * Updates the distance indicator to the distances of the given round id and subtype
     */
    fun setDistanceIndicatorText(roundSelection: RoundSelection) {
        val distanceText = roundSelection.getDistanceIndicatorText()
        if (distanceText != null) {
            text_create_round__distance_indicator.updateText(distanceText)
            text_create_round__distance_indicator.visibility = View.VISIBLE
        }
        else {
            text_create_round__distance_indicator.visibility = View.GONE
        }
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        val mainList = mutableListOf(
                ActionBarHelp.HelpShowcaseItem.Builder()
                        .setViewId(R.id.layout_create_round__date)
                        .setHelpTitleId(R.string.help_create_round__date_title)
                        .setHelpBodyId(R.string.help_create_round__date_body)
                        .setShape(ActionBarHelp.ShowcaseShape.OVAL)
                        .setShapePadding(0)
                        .build(),
                ActionBarHelp.HelpShowcaseItem.Builder()
                        .setViewId(R.id.layout_create_round__round)
                        .setHelpTitleId(R.string.help_create_round__round_title)
                        .setHelpBodyId(R.string.help_create_round__round_body)
                        .setShape(ActionBarHelp.ShowcaseShape.OVAL)
                        .setShapePadding(0)
                        .build()
        )
        if (layout_create_round__round_sub_type.visibility == View.VISIBLE) {
            mainList.addAll(
                    listOf(
                            ActionBarHelp.HelpShowcaseItem.Builder()
                                    .setViewId(R.id.layout_create_round__round_sub_type)
                                    .setHelpTitleId(R.string.help_create_round__sub_round_title)
                                    .setHelpBodyId(R.string.help_create_round__sub_round_body)
                                    .setShape(ActionBarHelp.ShowcaseShape.OVAL)
                                    .setShapePadding(0)
                                    .build(),
                            ActionBarHelp.HelpShowcaseItem.Builder()
                                    .setViewId(R.id.text_create_round__arrow_count_indicator)
                                    .setHelpTitleId(R.string.help_create_round__arrow_count_indicator_title)
                                    .setHelpBodyId(R.string.help_create_round__arrow_count_indicator_body)
                                    .setShape(ActionBarHelp.ShowcaseShape.OVAL)
                                    .setShapePadding(0)
                                    .build(),
                            ActionBarHelp.HelpShowcaseItem.Builder()
                                    .setViewId(R.id.text_create_round__distance_indicator)
                                    .setHelpTitleId(R.string.help_create_round__distance_indicator_title)
                                    .setHelpBodyId(R.string.help_create_round__distance_indicator_body)
                                    .setShape(ActionBarHelp.ShowcaseShape.OVAL)
                                    .setShapePadding(0)
                                    .build()
                    )
            )
        }
        mainList.add(
                ActionBarHelp.HelpShowcaseItem.Builder()
                        .setViewId(R.id.button_create_round__submit)
                        .setHelpTitleId(R.string.help_create_round__submit_title)
                        .setHelpBodyId(R.string.help_create_round__submit_body)
                        .setShape(ActionBarHelp.ShowcaseShape.OVAL)
                        .build()
        )
        return mainList
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
