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
import eywa.projectcodex.ui.commonUtils.ActionBarHelp
import eywa.projectcodex.viewModels.MainMenuViewModel
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MainMenuFragment : Fragment(), ActionBarHelp {
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

        mainMenuViewModel.updateDefaultRoundsProgress.observe(viewLifecycleOwner, Observer { progressString ->
            fun getVisibility(show: Boolean) = if (show) View.VISIBLE else View.GONE
            val progressText: String
            val updateHappening: Boolean
            if (progressString == null) {
                progressText = getString(R.string.main_menu__update_default_rounds_progress_init)
                updateHappening = false
            }
            else {
                progressText = progressString
                updateHappening = true
            }
            text_main_menu__update_default_rounds_progress.text = progressText
            button_main_menu__update_default_rounds.visibility = getVisibility(!updateHappening)
            label_main_menu__update_default_rounds_progress.visibility = getVisibility(updateHappening)
            text_main_menu__update_default_rounds_progress.visibility = getVisibility(updateHappening)
            button_main_menu__update_default_rounds_cancel.visibility = getVisibility(updateHappening)
            // TODO Some completion indication (otherwise it just goes back to the start button
        })

        button_main_menu__start_new_round.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToNewRoundFragment()
            view.findNavController().navigate(action)
        }

        button_main_menu__view_rounds.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToViewRoundsFragment()
            view.findNavController().navigate(action)
        }

        button_main_menu__update_default_rounds.setOnClickListener {
            mainMenuViewModel.updateDefaultRounds(resources)
        }

        button_main_menu__update_default_rounds_cancel.setOnClickListener {
            mainMenuViewModel.cancelUpdateDefaultRounds()
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Do nothing
        }
        callback.isEnabled = true
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        // TODO Replace test showcase with actual information
        return listOf(
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_main_menu__start_new_round,
                        "Title 1",
                        "Text 1"
                ),
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_main_menu__view_rounds,
                        "Title 2",
                        "Text 2"
                )
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
