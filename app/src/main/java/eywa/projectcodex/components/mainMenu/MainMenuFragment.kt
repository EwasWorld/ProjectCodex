package eywa.projectcodex.components.mainMenu

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
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MainMenuFragment : Fragment(), ActionBarHelp {
    private lateinit var mainMenuViewModel: MainMenuViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.main_menu__title)

        mainMenuViewModel = ViewModelProvider(this).get(MainMenuViewModel::class.java)
        mainMenuViewModel.updateDefaultRoundsState.observe(viewLifecycleOwner, Observer { state ->
            fun getVisibility(show: Boolean) = if (show) View.VISIBLE else View.GONE
            val updateHappening = state == UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS
            val updateNotStarted = state == UpdateDefaultRounds.UpdateTaskState.NOT_STARTED

            // Switch between start/cancel buttons
            button_main_menu__update_default_rounds.visibility = getVisibility(!updateHappening)
            button_main_menu__update_default_rounds_cancel.visibility = getVisibility(
                    updateHappening && state != UpdateDefaultRounds.UpdateTaskState.CANCELLING
            )

            // Display status information if not in the NOT_STARTED state
            label_main_menu__update_default_rounds_progress.visibility = getVisibility(!updateNotStarted)
            text_main_menu__update_default_rounds_progress.visibility = getVisibility(!updateNotStarted)
        })
        mainMenuViewModel.updateDefaultRoundsProgressMessage.observe(viewLifecycleOwner, Observer { message ->
            val progressText = message ?: getString(R.string.main_menu__update_default_rounds_initialising)
            text_main_menu__update_default_rounds_progress.text = progressText
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
            mainMenuViewModel.cancelUpdateDefaultRounds(resources)
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Do nothing
        }
        callback.isEnabled = true
    }

    override fun onStop() {
        super.onStop()
        mainMenuViewModel.resetUpdateDefaultRoundsStateIfComplete()
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        return listOf(
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_main_menu__start_new_round,
                        getString(R.string.help_main_menu__new_round_title),
                        getString(R.string.help_main_menu__new_round_body)
                ),
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_main_menu__view_rounds,
                        getString(R.string.help_main_menu__view_rounds_title),
                        getString(R.string.help_main_menu__view_rounds_body)
                ),
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_main_menu__update_default_rounds,
                        getString(R.string.help_main_menu__update_default_title),
                        getString(R.string.help_main_menu__update_default_body)
                )
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
