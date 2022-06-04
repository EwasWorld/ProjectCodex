package eywa.projectcodex.common.helpShowcase

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ToastSpamPrevention


/**
 * Used on fragments to add actions to the help icon on the action bar
 */
interface ActionBarHelp {
    companion object {
        private const val LOG_TAG = "ActionBarHelp"
        private val showcaseInProgressLock = Object()
        private var showcaseInProgress = false
        private var displayedIndex by mutableStateOf(0)

        /**
         * Executes the [getHelpShowcases] showcases for all [fragments] in order of priority
         *
         * @param fragments fragments current shown which implement [ActionBarHelp]
         */
        fun executeHelpPressed(fragments: List<ActionBarHelp>, activity: AppCompatActivity) {
            if (fragments.isEmpty()) {
                CustomLogger.customLogger.d(LOG_TAG, "No help information defined")
                activity.displayHasNoHelpToast()
                return
            }
            synchronized(showcaseInProgressLock) {
                if (showcaseInProgress) return
                showcaseInProgress = true
            }
            // Using a list over a priority queue as a priority queue is not stable
            //     (if elements are equal, order is not preserved)
            val helpItemsList = mutableListOf<HelpShowcaseItem>()
            for (fragment in fragments) {
                val fragmentPriority = fragment.getHelpPriority() ?: HelpShowcaseItem.DEFAULT_HELP_PRIORITY
                fragment.getHelpShowcases().forEach {
                    it.priority = fragmentPriority
                    helpItemsList.add(it)
                }
            }
            helpItemsList.sortBy { it.priority }
            if (helpItemsList.isEmpty()) {
                CustomLogger.customLogger.w(LOG_TAG, "No help information found")
                activity.displayHasNoHelpToast()
                synchronized(showcaseInProgressLock) {
                    showcaseInProgress = false
                }
                return
            }
            displayedIndex = 0
            showHelpItem(helpItemsList, activity)
        }

        private fun showHelpItem(helpItemsList: List<HelpShowcaseItem>, activity: AppCompatActivity) {
            if (displayedIndex !in helpItemsList.indices) {
                synchronized(showcaseInProgressLock) {
                    showcaseInProgress = false
                }
                return
            }
            helpItemsList[displayedIndex].show(
                    activity = activity,
                    hasNextItem = displayedIndex == helpItemsList.lastIndex,
                    goToNextItemListener = {
                        displayedIndex++
                        showHelpItem(helpItemsList, activity)
                    },
                    endShowcaseListener = {
                        synchronized(showcaseInProgressLock) {
                            showcaseInProgress = false
                        }
                    }
            )
        }

        private fun Activity.displayHasNoHelpToast() = ToastSpamPrevention.displayToast(
                applicationContext,
                resources.getString(R.string.err_action_bar__no_help_info)
        )
    }

    /**
     * @return a list of help items to show any time this is visible
     */
    fun getHelpShowcases(): List<HelpShowcaseItem>

    /**
     * @return the default priority for this fragment's help actions. Lower number = higher priority
     * Will override [ViewHelpShowcaseItem.DEFAULT_HELP_PRIORITY] but will be overridden by an individual
     * [ViewHelpShowcaseItem]'s priority if set
     * @see ViewHelpShowcaseItem.priority
     */
    fun getHelpPriority(): Int?
}