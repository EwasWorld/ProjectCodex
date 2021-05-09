package eywa.projectcodex.components.newRound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.components.commonElements.DatePickerDialog
import eywa.projectcodex.components.commonElements.TimePickerDialog
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.database.archerRound.ArcherRound
import kotlinx.android.synthetic.main.fragment_new_round.*
import java.text.SimpleDateFormat
import java.util.*

class NewRoundFragment : Fragment(), ActionBarHelp {
    private lateinit var newRoundViewModel: NewRoundViewModel

    /**
     * Used to find the round that was just created
     */
    private var maxId: Int = 0
    private val noRoundPosition = 0
    private var selectedRoundPosition: Int = noRoundPosition
    private var selectedSubtypePosition: Int? = null
    private var date: Calendar = Calendar.getInstance()

    // TODO Date/time format
    private val dateFormat = SimpleDateFormat("dd MMM yy")
    private val timeFormat = SimpleDateFormat("HH:mm")

    // Lowercase h for 12 hour
    private val is24HourTime = timeFormat.toPattern().contains("HH")

    /**
     * Lazy loaded
     * TODO Does onCreateDialog actually get called until show() is called? If not this is not necessary
     */
    private var datePickerDialog: DatePickerDialog? = null
        get() {
            if (field != null) {
                return field
            }
            val okListener = object : DatePickerDialog.OnOkListener {
                override fun onSelect(value: Calendar) {
                    date = value
                    updateDateTime()
                }
            }
            datePickerDialog = DatePickerDialog(
                    getString(R.string.create_round__date_shot_date_picker_title), null, null, null, date, okListener
            )
            @Suppress("RecursivePropertyAccessor")
            return datePickerDialog
        }

    /**
     * Lazy loaded
     */
    private var timePickerDialog: TimePickerDialog? = null
        get() {
            if (field != null) {
                return field
            }
            val okListener = object : TimePickerDialog.OnOkListener {
                override fun onSelect(hours: Int, minutes: Int) {
                    date.set(Calendar.MINUTE, minutes)
                    date.set(Calendar.HOUR_OF_DAY, hours)
                    updateDateTime()
                }
            }
            timePickerDialog = TimePickerDialog(
                    getString(R.string.create_round__time_shot_time_picker_title), null, date.get(Calendar.HOUR_OF_DAY),
                    date.get(Calendar.MINUTE), is24HourTime, okListener
            )
            @Suppress("RecursivePropertyAccessor")
            return timePickerDialog
        }

    private fun updateDateTime() {
        view!!.findViewById<TextView>(R.id.text_create_round__date).text = dateFormat.format(date.time)
        view!!.findViewById<TextView>(R.id.text_create_round__time).text = timeFormat.format(date.time)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_round, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.create_round__title)

        var initialId = true
        newRoundViewModel = ViewModelProvider(this).get(NewRoundViewModel::class.java)
        newRoundViewModel.maxId.observe(viewLifecycleOwner, Observer { id ->
            if (initialId) {
                initialId = false
            }
            else {
                maxId = id
                // When the new round entry has been added, open the input end dialog
                val action = NewRoundFragmentDirections.actionNewRoundFragmentToInputEndFragment(
                        maxId, selectedRoundPosition != noRoundPosition
                )
                view.findNavController().navigate(action)
            }
        })

        updateDateTime()
        text_create_round__date.setOnClickListener {
            datePickerDialog!!.show(childFragmentManager, "date picker")
        }
        text_create_round__time.setOnClickListener {
            timePickerDialog!!.show(childFragmentManager, "time picker")
        }

        val roundSelection = RoundSelection(resources, newRoundViewModel, viewLifecycleOwner)
        // Update the spinners if the database updates (spinners don't display correctly at the start without this)
        newRoundViewModel.allRounds.observe(viewLifecycleOwner, Observer { _ ->
            spinner_create_round__round.adapter = ArrayAdapter(
                    activity!!.applicationContext, R.layout.spinner_light_background,
                    roundSelection.getAvailableRounds()
            )
            spinner_create_round__round.setSelection(noRoundPosition)
        })

        button_create_round__submit.setOnClickListener {
            val roundId = roundSelection.getSelectedRoundId(selectedRoundPosition)
            val roundSubtypeId =
                    if (roundId != null) roundSelection.getSelectedSubtypeId(selectedSubtypePosition) else null
            // TODO Check date locales (I want to store in UTC)
            newRoundViewModel.insert(
                    ArcherRound(0, date.time, 1, false, roundId = roundId, roundSubTypeId = roundSubtypeId)
            )
            // Navigate to the round's input end screen navigating to the newly created round id (found using maxId)
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
                selectedRoundPosition = position

                if (selectedRoundPosition == noRoundPosition) {
                    selectedSubtypePosition = null
                    layout_create_round__round_sub_type.visibility = View.GONE
                    layout_create_round__arrow_count_indicator.visibility = View.GONE
                    layout_create_round__distance_indicator.visibility = View.GONE
                    return
                }

                val roundSubtypes = roundSelection.getRoundSubtypes(position)
                if (roundSubtypes.isNullOrEmpty()) {
                    selectedSubtypePosition = null
                    layout_create_round__round_sub_type.visibility = View.GONE
                }
                else {
                    spinner_create_round__round_sub_type.adapter = ArrayAdapter(
                            activity!!.applicationContext, R.layout.spinner_light_background, roundSubtypes
                    )
                    layout_create_round__round_sub_type.visibility = View.VISIBLE
                }

                setDistanceIndicatorText(roundSelection)

                /*
                 * Create the arrow count indicator string
                 */
                val arrowCountText = roundSelection.getArrowCountIndicatorText(position)
                if (arrowCountText != null) {
                    text_create_round__arrow_count_indicator.text = arrowCountText
                    layout_create_round__arrow_count_indicator.visibility = View.VISIBLE
                }
                else {
                    layout_create_round__arrow_count_indicator.visibility = View.GONE
                }
            }
        }

        spinner_create_round__round_sub_type.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            /**
             * Update distance indicators
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSubtypePosition = position
                setDistanceIndicatorText(roundSelection)
            }
        }
    }

    /**
     * Updates the distance indicator to the distances of the given round id and subtype
     */
    fun setDistanceIndicatorText(roundSelection: RoundSelection) {
        val distanceText = roundSelection.getDistanceIndicatorText(selectedRoundPosition, selectedSubtypePosition)
        if (distanceText != null) {
            text_create_round__distance_indicator.text = distanceText
            layout_create_round__distance_indicator.visibility = View.VISIBLE
        }
        else {
            layout_create_round__distance_indicator.visibility = View.GONE
        }
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        TODO("Not yet implemented")
    }

    override fun getHelpPriority(): Int? {
        TODO("Not yet implemented")
    }
}
