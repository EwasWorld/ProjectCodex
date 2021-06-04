package eywa.projectcodex.components.mainMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MainMenuFragment : Fragment(), ActionBarHelp {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.main_menu__title)

        button_main_menu__start_new_round.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToNewRoundFragment()
            view.findNavController().navigate(action)
        }

        button_main_menu__view_rounds.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToViewRoundsFragment()
            view.findNavController().navigate(action)
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Do nothing
        }
        callback.isEnabled = true
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
                )
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
