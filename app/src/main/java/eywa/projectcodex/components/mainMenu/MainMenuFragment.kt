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
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.components.commonUtils.SharedPrefs.Companion.getSharedPreferences
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MainMenuFragment : Fragment(), ActionBarHelp {
    private lateinit var mainMenuViewModel: MainMenuViewModel
    private var defaultRoundsVersion = -1
    private var defaultRoundsState = UpdateDefaultRounds.UpdateTaskState.NOT_STARTED
    private val sharedPreferences by lazy { requireActivity().getSharedPreferences() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.main_menu__title)

        mainMenuViewModel = ViewModelProvider(this).get(MainMenuViewModel::class.java)
        mainMenuViewModel.updateDefaultRoundsState.observe(viewLifecycleOwner, Observer { state ->
            this.defaultRoundsState = state
            fun getVisibility(show: Boolean) = if (show) View.VISIBLE else View.GONE
            val updateHappening = state == UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS
            val updateNotStarted = state == UpdateDefaultRounds.UpdateTaskState.NOT_STARTED
            val updateComplete = state == UpdateDefaultRounds.UpdateTaskState.COMPLETE
                    || state == UpdateDefaultRounds.UpdateTaskState.UP_TO_DATE

            // Switch between start/cancel buttons
            button_main_menu__update_default_rounds.visibility = getVisibility(!updateHappening && !updateComplete)
            button_main_menu__update_default_rounds_cancel.visibility = getVisibility(
                    updateHappening && state != UpdateDefaultRounds.UpdateTaskState.CANCELLING && !updateComplete
            )

            // Display status information if not in the NOT_STARTED state
            label_main_menu__update_default_rounds_progress.visibility = getVisibility(!updateNotStarted)
            text_main_menu__update_default_rounds_progress.visibility = getVisibility(!updateNotStarted)
        })
        mainMenuViewModel.updateDefaultRoundsProgressMessage.observe(viewLifecycleOwner, Observer { message ->
            var progressText = message ?: getString(R.string.main_menu__update_default_rounds_initialising)
            if (defaultRoundsState == UpdateDefaultRounds.UpdateTaskState.UP_TO_DATE) {
                progressText = resources.getString(R.string.main_menu__update_default_rounds_up_to_date)
            }
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
            mainMenuViewModel.updateDefaultRounds(resources, sharedPreferences)
        }

        button_main_menu__update_default_rounds_cancel.setOnClickListener {
            mainMenuViewModel.cancelUpdateDefaultRounds(resources)
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Do nothing
        }
        callback.isEnabled = true

        firstLaunch()
    }

    /**
     * Actions to be completed when the app is first launched
     */
    private fun firstLaunch() {
        // Check whether default rounds have been loaded in
        if (defaultRoundsVersion < 0) {
            defaultRoundsVersion = sharedPreferences.getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
            if (defaultRoundsVersion < 0) {
                mainMenuViewModel.updateDefaultRounds(resources, sharedPreferences)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mainMenuViewModel.resetUpdateDefaultRoundsStateIfComplete()
    }

    override fun onPause() {
        super.onPause()
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
