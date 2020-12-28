package eywa.projectcodex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.logic.checkDefaultRounds
import eywa.projectcodex.logic.roundsFromJson
import eywa.projectcodex.viewModels.MainMenuViewModel
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MainMenuFragment : Fragment() {
    private lateinit var mainMenuViewModel: MainMenuViewModel
    private var rounds: List<Round> = listOf()
    private var roundArrowCounts: List<RoundArrowCount> = listOf()
    private var roundSubTypes: List<RoundSubType> = listOf()
    private var roundDistances: List<RoundDistance> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.main_menu__title)

        mainMenuViewModel = ViewModelProvider(this).get(MainMenuViewModel::class.java)
        mainMenuViewModel.rounds.observe(viewLifecycleOwner, Observer { it?.let { rounds = it } })
        mainMenuViewModel.roundArrowCounts.observe(viewLifecycleOwner, Observer { it?.let { roundArrowCounts = it } })
        mainMenuViewModel.roundSubTypes.observe(viewLifecycleOwner, Observer { it?.let { roundSubTypes = it } })
        mainMenuViewModel.roundDistances.observe(viewLifecycleOwner, Observer { it?.let { roundDistances = it } })

        button_main_menu__start_new_round.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToNewRoundFragment()
            view.findNavController().navigate(action)
        }

        button_main_menu__view_rounds.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToViewRoundsFragment()
            view.findNavController().navigate(action)
        }

        button_main_menu__update_default_rounds.setOnClickListener {
            val defaultRounds = roundsFromJson(
                    resources.openRawResource(R.raw.default_rounds_data).bufferedReader().use { it.readText() })
            val updates = checkDefaultRounds(
                    defaultRounds, rounds, roundArrowCounts, roundSubTypes, roundDistances
            )
            mainMenuViewModel.updateRounds(updates)
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Do nothing
        }
        callback.isEnabled = true
    }
}
