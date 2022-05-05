package eywa.projectcodex.components.mainMenu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ViewHelpShowcaseItem
import kotlinx.android.synthetic.main.fragment_main_menu.*
import kotlin.system.exitProcess

class MainMenuFragment : Fragment(), ActionBarHelp {
    private val exitAppDialog by lazy {
        AlertDialog.Builder(context)
                .setTitle(R.string.main_menu__exit_app_dialog_title)
                .setMessage(R.string.main_menu__exit_app_dialog_body)
                .setPositiveButton(R.string.main_menu__exit_app_dialog_exit) { _, _ ->
                    requireActivity().finish()
                    exitProcess(0)
                }
                .setNegativeButton(R.string.general_cancel) { _, _ -> }
                .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.main_menu__title)

        button_main_menu__start_new_score.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToNewScoreFragment()
            view.findNavController().navigate(action)
        }

        button_main_menu__view_scores.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToViewScoresFragment()
            view.findNavController().navigate(action)
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!exitAppDialog.isShowing) {
                exitAppDialog.show()
            }
            else {
                exitAppDialog.dismiss()
            }
        }
        callback.isEnabled = true
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        return listOf(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_main_menu__start_new_score)
                        .setHelpTitleId(R.string.help_main_menu__new_score_title)
                        .setHelpBodyId(R.string.help_main_menu__new_score_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_main_menu__view_scores)
                        .setHelpTitleId(R.string.help_main_menu__view_scores_title)
                        .setHelpBodyId(R.string.help_main_menu__view_scores_body)
                        .build()
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
