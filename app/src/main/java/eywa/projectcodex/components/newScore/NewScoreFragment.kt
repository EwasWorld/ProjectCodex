package eywa.projectcodex.components.newScore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.R
import eywa.projectcodex.common.customViews.LabelledTextView
import eywa.projectcodex.common.elements.DatePickerDialog
import eywa.projectcodex.common.elements.TimePickerDialog
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ViewHelpShowcaseItem
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.common.utils.resourceStringReplace
import eywa.projectcodex.database.archerRound.ArcherRound
import java.util.*

class NewScoreFragment : Fragment(), ActionBarHelp {
    private val args: NewScoreFragmentArgs by navArgs()

    private val newScoreViewModel: NewScoreViewModel by viewModels()

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
                date.get(Calendar.MINUTE), DateTimeFormat.TIME_24_HOUR.pattern.contains("HH"), okListener
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_score, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roundSpinner = view.findViewById<Spinner>(R.id.spinner_create_round__round)
        val subRoundSpinner = view.findViewById<Spinner>(R.id.spinner_create_round__round_sub_type)

        // True if the fragment is being used to edit an existing round
        val isInEditMode = args.archerRoundId != -1

        activity?.title = getString(
                if (!isInEditMode) R.string.create_round__title else R.string.create_round__edit_title
        )

        var submitPressed = false
        roundSelection = RoundSelection(resources, newScoreViewModel, viewLifecycleOwner)

        if (isInEditMode) {
            newScoreViewModel.getArcherRound(args.archerRoundId).observe(viewLifecycleOwner, { ar ->
                ar?.let {
                    archerRound = it
                    setValuesFromArcherRound()
                }
            })
            newScoreViewModel.getArrowsForRound(args.archerRoundId).observe(viewLifecycleOwner, { arrows ->
                if (arrows != null) arrowsShot = arrows.count()
            })
            view.findViewById<LinearLayout>(R.id.layout_create_round__edit_submit_buttons).visibility = View.VISIBLE
            view.findViewById<LinearLayout>(R.id.layout_create_round__new_submit_buttons).visibility = View.GONE
        }
        else {
            view.findViewById<LinearLayout>(R.id.layout_create_round__edit_submit_buttons).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.layout_create_round__new_submit_buttons).visibility = View.VISIBLE
        }

        newScoreViewModel.maxId.observe(viewLifecycleOwner, { maxId ->
            if (submitPressed && maxId != null) {
                // When the new round entry has been added, open the input end dialog
                val action = NewScoreFragmentDirections.actionNewScoreFragmentToInputEndFragment(maxId)
                view.findNavController().navigate(action)
            }
        })

        updateDateTime()
        view.findViewById<TextView>(R.id.text_create_round__date).setOnClickListener {
            datePickerDialog.show(childFragmentManager, "date picker")
        }
        view.findViewById<TextView>(R.id.text_create_round__time).setOnClickListener {
            timePickerDialog.show(childFragmentManager, "time picker")
        }

        // Hide the round spinner if rounds are being updated
        newScoreViewModel.updateDefaultRoundsState.observe(viewLifecycleOwner, { state ->
            updateDefaultRoundsState = state
            val isInProgress = updateDefaultRoundsState == UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS
            fun getVisibility(isShown: Boolean) = if (isShown) View.VISIBLE else View.GONE
            view.findViewById<TextView>(R.id.text_create_round__default_rounds_updating_warning).visibility =
                    getVisibility(isInProgress)
            view.findViewById<LabelledTextView>(R.id.text_create_round__default_rounds_updating_status).visibility =
                    getVisibility(isInProgress)
            view.findViewById<LinearLayout>(R.id.layout_create_round__round).visibility = getVisibility(!isInProgress)
        })
        newScoreViewModel.updateDefaultRoundsProgressMessage.observe(viewLifecycleOwner, { message ->
            val newText = when {
                message != null -> message
                updateDefaultRoundsState == UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS -> {
                    resources.getString(R.string.about__update_default_rounds_in_progress)
                }
                else -> ""
            }
            view.findViewById<LabelledTextView>(R.id.text_create_round__default_rounds_updating_status)
                    .updateText(newText)
        })

        // Update the spinners if the database updates (spinners don't display correctly at the start without this)
        newScoreViewModel.allRounds.observe(viewLifecycleOwner, {
            roundSpinner.adapter = ArrayAdapter(
                    requireActivity().applicationContext, R.layout.spinner_light_background,
                    roundSelection.getAvailableRounds()
            )
            if (isInEditMode) {
                setValuesFromArcherRound(true)
            }
            else {
                roundSpinner.setSelection(roundSelection.noRoundPosition)
            }
        })

        view.findViewById<Button>(R.id.button_create_round__submit).setOnClickListener {
            val roundId = roundSelection.getSelectedRoundId()
            val roundSubtypeId = if (roundId != null) roundSelection.getSelectedSubtypeId() else null
            // TODO Check date locales (I want to store in UTC)
            newScoreViewModel.insert(
                    ArcherRound(0, date.time, 1, false, roundId = roundId, roundSubTypeId = roundSubtypeId)
            ).invokeOnCompletion {
                // Ensures that when max ID changes due to the newly created round, it will navigate to the input end
                //      screen
                submitPressed = true
            }
        }

        view.findViewById<Button>(R.id.button_create_round__reset).setOnClickListener {
            setValuesFromArcherRound()
        }

        view.findViewById<Button>(R.id.button_create_round__cancel).setOnClickListener {
            requireView().findNavController().popBackStack()
        }

        view.findViewById<Button>(R.id.button_create_round__complete).setOnClickListener {
            val roundId = roundSelection.getSelectedRoundId()
            val roundSubtypeId = if (roundId != null) roundSelection.getSelectedSubtypeId() else null
            // TODO Check date locales (I want to store in UTC)
            archerRound?.let { ar ->
                newScoreViewModel.update(
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

        roundSpinner.onItemSelectedListener = object : OnItemSelectedListener {
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
                val warningMessageView =
                        requireView().findViewById<TextView>(R.id.text_create_round__too_many_arrows_warning)
                if (isInEditMode) {
                    val arrowsInSelectedRound = roundSelection.getTotalArrowsInRound()
                    if (arrowsInSelectedRound != null && arrowsInSelectedRound < arrowsShot) {
                        displayArrowCountWarning = true
                        warningMessageView.text = resourceStringReplace(
                                resources.getString(R.string.err_create_round__too_many_arrows_old),
                                mapOf(
                                        "round" to roundSelection.getSelectedRoundName()!!,
                                        "shot arrows" to arrowsShot.toString(),
                                        "round arrows" to arrowsInSelectedRound.toString()
                                )
                        )
                        warningMessageView.visibility = View.VISIBLE
                        requireView().findViewById<Button>(R.id.button_create_round__complete).isEnabled = false
                    }
                }
                if (!displayArrowCountWarning) {
                    warningMessageView.visibility = View.GONE
                    requireView().findViewById<Button>(R.id.button_create_round__complete).isEnabled = true
                }

                /*
                 * Hide everything for no round
                 */
                if (position == roundSelection.noRoundPosition) {
                    roundSelection.selectedSubtypePosition = null
                    requireView().findViewById<LinearLayout>(R.id.layout_create_round__round_sub_type).visibility =
                            View.GONE
                    requireView().findViewById<LabelledTextView>(R.id.text_create_round__arrow_count_indicator).visibility =
                            View.GONE
                    requireView().findViewById<LabelledTextView>(R.id.text_create_round__distance_indicator).visibility =
                            View.GONE
                    return
                }

                /*
                 * Setup the sub type spinner
                 */
                val roundSubTypes = roundSelection.getRoundSubtypes()
                if (roundSubTypes.isNullOrEmpty()) {
                    roundSelection.selectedSubtypePosition = null
                    requireView().findViewById<LinearLayout>(R.id.layout_create_round__round_sub_type).visibility =
                            View.GONE
                }
                else {
                    subRoundSpinner.adapter = ArrayAdapter(
                            requireActivity().applicationContext,
                            R.layout.spinner_light_background,
                            roundSubTypes
                    )

                    if (resetModeSubTypeId != null) {
                        val subTypePos = roundSelection.getPositionOfSubtype(
                                roundSelection.getSelectedRoundId(), resetModeSubTypeId!!
                        )
                        if (subTypePos != null) {
                            subRoundSpinner.setSelection(subTypePos)
                        }
                        else {
                            roundSelection.selectedSubtypePosition = null
                        }
                        resetModeSubTypeId = null
                    }

                    requireView().findViewById<LinearLayout>(R.id.layout_create_round__round_sub_type).visibility =
                            View.VISIBLE
                }

                setDistanceIndicatorText(roundSelection)

                /*
                 * Create the arrow count indicator string
                 */
                val arrowCountText = roundSelection.getArrowCountIndicatorText()
                if (arrowCountText != null) {
                    requireView().findViewById<LabelledTextView>(R.id.text_create_round__arrow_count_indicator)
                            .updateText(arrowCountText)
                    requireView().findViewById<LabelledTextView>(R.id.text_create_round__arrow_count_indicator).visibility =
                            View.VISIBLE
                }
                else {
                    requireView().findViewById<LabelledTextView>(R.id.text_create_round__arrow_count_indicator).visibility =
                            View.GONE
                }
            }
        }

        /**
         * Update distance indicators
         */
        subRoundSpinner.onItemSelectedListener = object : OnItemSelectedListener {
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
                DateTimeFormat.LONG_DATE.format(date.time)
        requireView().findViewById<TextView>(R.id.text_create_round__time).text =
                DateTimeFormat.TIME_24_HOUR.format(date.time)
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
                requireView().findViewById<Spinner>(R.id.spinner_create_round__round).setSelection(roundPos)
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
            requireView().findViewById<LabelledTextView>(R.id.text_create_round__distance_indicator)
                    .updateText(distanceText)
            requireView().findViewById<LabelledTextView>(R.id.text_create_round__distance_indicator).visibility =
                    View.VISIBLE
        }
        else {
            requireView().findViewById<LabelledTextView>(R.id.text_create_round__distance_indicator).visibility =
                    View.GONE
        }
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        val mainList = mutableListOf(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.layout_create_round__date)
                        .setHelpTitleId(R.string.help_create_round__date_title)
                        .setHelpBodyId(R.string.help_create_round__date_body)
                        .setShape(HelpShowcaseItem.Shape.OVAL)
                        .setShapePadding(0)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.layout_create_round__round)
                        .setHelpTitleId(R.string.help_create_round__round_title)
                        .setHelpBodyId(R.string.help_create_round__round_body)
                        .setShape(HelpShowcaseItem.Shape.OVAL)
                        .setShapePadding(0)
                        .build()
        )
        if (requireView().findViewById<LinearLayout>(R.id.layout_create_round__round_sub_type).visibility == View.VISIBLE) {
            mainList.addAll(
                    listOf(
                            ViewHelpShowcaseItem.Builder()
                                    .setViewId(R.id.layout_create_round__round_sub_type)
                                    .setHelpTitleId(R.string.help_create_round__sub_round_title)
                                    .setHelpBodyId(R.string.help_create_round__sub_round_body)
                                    .setShape(HelpShowcaseItem.Shape.OVAL)
                                    .setShapePadding(0)
                                    .build(),
                            ViewHelpShowcaseItem.Builder()
                                    .setViewId(R.id.text_create_round__arrow_count_indicator)
                                    .setHelpTitleId(R.string.help_create_round__arrow_count_indicator_title)
                                    .setHelpBodyId(R.string.help_create_round__arrow_count_indicator_body)
                                    .setShape(HelpShowcaseItem.Shape.OVAL)
                                    .setShapePadding(0)
                                    .build(),
                            ViewHelpShowcaseItem.Builder()
                                    .setViewId(R.id.text_create_round__distance_indicator)
                                    .setHelpTitleId(R.string.help_create_round__distance_indicator_title)
                                    .setHelpBodyId(R.string.help_create_round__distance_indicator_body)
                                    .setShape(HelpShowcaseItem.Shape.OVAL)
                                    .setShapePadding(0)
                                    .build()
                    )
            )
        }
        mainList.add(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_create_round__submit)
                        .setHelpTitleId(R.string.help_create_round__submit_title)
                        .setHelpBodyId(R.string.help_create_round__submit_body)
                        .setShape(HelpShowcaseItem.Shape.OVAL)
                        .build()
        )
        return mainList
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
