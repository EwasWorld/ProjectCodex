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
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.viewModels.NewRoundViewModel
import kotlinx.android.synthetic.main.fragment_new_round.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class NewRoundFragment : Fragment() {
    private lateinit var newRoundViewModel: NewRoundViewModel
    private var initialId: Boolean = true
    // Used to find the round that was just created
    private var maxId: Int = 0
    private var allRoundSubTypes: List<RoundSubType> = listOf()
    private var availableRounds: Array<Pair<String, Int?>> = arrayOf()
    private var selectedRoundsSubtypes: Array<Pair<String, Int>> = arrayOf()
    private var selectedRoundId: Int? = null

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
        newRoundViewModel.allRounds.observe(viewLifecycleOwner, Observer { dbRounds ->
            availableRounds = arrayOf<Pair<String, Int?>>(getString(R.string.create_round__no_round) to null).plus(
                    dbRounds.map { it.displayName to it.roundId })
            // Should always be at least one for the 'no round' option
            if (availableRounds.size <= 1) {
                spinner_select_round.adapter = ArrayAdapter(
                        activity!!.applicationContext, R.layout.spinner_light_background,
                        arrayOf(getString(R.string.create_round__no_rounds_found))
                )
            }
            else {
                // TODO Implement some way of localising the round names
                spinner_select_round.adapter = ArrayAdapter(
                        activity!!.applicationContext, R.layout.spinner_light_background,
                        availableRounds.map { it.first }
                )
            }
        })
        newRoundViewModel.allRoundSubTypes.observe(viewLifecycleOwner, Observer { dbRounds ->
            allRoundSubTypes = dbRounds
        })

        button_create_round.setOnClickListener {
            // TODO Check date locales (I want to store in UTC)
            var selectedSubType: Int? = null
            if (selectedRoundId != null && selectedRoundsSubtypes.isNotEmpty()) {
                selectedSubType = selectedRoundsSubtypes[spinner_select_round_sub_type.selectedItemPosition].second
            }
            newRoundViewModel.insert(
                    ArcherRound(0, Date(), 1, false, roundId = selectedRoundId, roundSubTypeId = selectedSubType)
            )
            // Navigate to the round's input end screen navigating to the newly created round id (found using maxId)
        }

        spinner_select_round.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            /**
             * Hide the subtype spinner if no round is selected
             * Populate the subtype spinner with the appropriate items
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRoundId = availableRounds[position].second
                if (selectedRoundId == null) {
                    layout_select_round_sub_type.visibility = View.GONE
                    selectedRoundId = null
                    selectedRoundsSubtypes = arrayOf()
                    return
                }

                val filteredRounds =
                    allRoundSubTypes.filter { it.roundId == selectedRoundId }.map { it.name to it.subTypeId }
                if (filteredRounds.isEmpty() || filteredRounds.size == 1 && filteredRounds[0].first.isNullOrBlank()) {
                    selectedRoundsSubtypes = arrayOf()
                    return
                }
                selectedRoundsSubtypes = filteredRounds.map { (it.first ?: "") to it.second }.toTypedArray()
                spinner_select_round_sub_type.adapter =
                    ArrayAdapter(
                            activity!!.applicationContext, R.layout.spinner_light_background,
                            selectedRoundsSubtypes.map { it.first }
                    )

                layout_select_round_sub_type.visibility = View.VISIBLE
            }
        }
    }
}
