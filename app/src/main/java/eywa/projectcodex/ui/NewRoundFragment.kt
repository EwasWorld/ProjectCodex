package eywa.projectcodex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.RoundSelection
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.viewModels.NewRoundViewModel
import kotlinx.android.synthetic.main.fragment_new_round.*
import java.util.*

class NewRoundFragment : Fragment() {
    private lateinit var newRoundViewModel: NewRoundViewModel
    private var initialId: Boolean = true

    /**
     * Used to find the round that was just created
     */
    private var maxId: Int = 0
    private var selectedRoundPosition: Int = 0
    private var selectedSubtypePosition: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_round, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.create_round__title)

        newRoundViewModel = ViewModelProvider(this).get(NewRoundViewModel::class.java)
        newRoundViewModel.maxId.observe(viewLifecycleOwner, Observer { id ->
            if (initialId) {
                initialId = false
            }
            else {
                maxId = id
                // When the new round entry has been added, open the input end dialog
                val action = NewRoundFragmentDirections.actionNewRoundFragmentToInputEndFragment(maxId)
                view.findNavController().navigate(action)
            }
        })

        val roundSelection = RoundSelection(resources, newRoundViewModel, viewLifecycleOwner)
        // Update the spinners if the database updates (not sure why it would but whatever)
        newRoundViewModel.allRounds.observe(viewLifecycleOwner, Observer { _ ->
            spinner_select_round.adapter = ArrayAdapter(
                    activity!!.applicationContext, R.layout.spinner_light_background,
                    roundSelection.getAvailableRounds()
            )
            spinner_select_round.setSelection(0)
        })

        button_create_round.setOnClickListener {
            // TODO Check date locales (I want to store in UTC)
            newRoundViewModel.insert(
                    ArcherRound(
                            0, Date(), 1, false, roundId = roundSelection.getSelectedRoundId(selectedRoundPosition),
                            roundSubTypeId = roundSelection.getSelectedSubtypeId(selectedSubtypePosition)
                    )
            )
            // Navigate to the round's input end screen navigating to the newly created round id (found using maxId)
        }

        spinner_select_round.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            /**
             * Hide the subtype spinner if no round is selected
             * Populate the subtype spinner with the appropriate items
             * Update round info indicators as appropriate
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRoundPosition = position

                val roundSubtypes = roundSelection.getRoundSubtypes(position)
                if (roundSubtypes.isNullOrEmpty()) {
                    selectedSubtypePosition = null
                    layout_select_round_sub_type.visibility = View.GONE
                    layout_select_round_arrow_count_indicator.visibility = View.GONE
                    layout_select_round_distance_indicator.visibility = View.GONE
                    return
                }

                spinner_select_round_sub_type.adapter =
                        ArrayAdapter(activity!!.applicationContext, R.layout.spinner_light_background, roundSubtypes)
                layout_select_round_sub_type.visibility = View.VISIBLE

                setDistanceIndicatorText(roundSelection)

                /*
                 * Create the arrow count indicator string
                 */
                val arrowCountText = roundSelection.getArrowCountIndicatorText(position)
                if (arrowCountText != null) {
                    text_select_round_arrow_count_indicator.text = arrowCountText
                    layout_select_round_arrow_count_indicator.visibility = View.VISIBLE
                }
                else {
                    layout_select_round_arrow_count_indicator.visibility = View.GONE
                }
            }
        }

        spinner_select_round_sub_type.onItemSelectedListener = object : OnItemSelectedListener {
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
            text_select_round_distance_indicator.text = distanceText
            layout_select_round_distance_indicator.visibility = View.VISIBLE
        }
        else {
            layout_select_round_distance_indicator.visibility = View.GONE
        }
    }
}
