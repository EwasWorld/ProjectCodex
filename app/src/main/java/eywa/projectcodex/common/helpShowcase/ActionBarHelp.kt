package eywa.projectcodex.common.helpShowcase

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ToastSpamPrevention

internal const val DEFAULT_HELP_PRIORITY = 0

/**
 * Used on fragments to add actions to the help icon on the action bar
 */
interface ActionBarHelp {
    companion object {
        private const val LOG_TAG = "ActionBarHelp"
        private val showcaseInProgressLock = Object()
        private var showcaseInProgress = false
        private var displayedIndex by mutableStateOf(0)
        private var helpItemsList: List<ComposeHelpShowcaseItem> = listOf()

        /**
         * Executes the [getHelpShowcases] showcases for all [fragments] in order of priority
         *
         * @param fragments fragments current shown which implement [ActionBarHelp]
         */
        fun executeHelpPressed(
                fragments: List<ActionBarHelp>,
                activity: AppCompatActivity
        ): List<ComposeHelpShowcaseItem>? {
            if (fragments.isEmpty()) {
                CustomLogger.customLogger.d(LOG_TAG, "No help information defined")
                activity.displayHasNoHelpToast()
                return null
            }
            synchronized(showcaseInProgressLock) {
                if (showcaseInProgress) return null
                showcaseInProgress = true
            }
            // Using a list over a priority queue as a priority queue is not stable
            //     (if elements are equal, order is not preserved)
            val helpItemsList = mutableListOf<ComposeHelpShowcaseItem>()
            for (fragment in fragments) {
                val fragmentPriority = fragment.getHelpPriority() ?: DEFAULT_HELP_PRIORITY
                fragment.getHelpShowcases().forEach {
                    it.priority = it.priority ?: fragmentPriority
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
                return null
            }
            displayedIndex = 0
            this.helpItemsList = helpItemsList

            return helpItemsList
        }

        fun markShowcaseComplete() {
            synchronized(showcaseInProgressLock) {
                showcaseInProgress = false
            }
        }

        private fun Activity.displayHasNoHelpToast() = ToastSpamPrevention.displayToast(
                applicationContext,
                resources.getString(R.string.err_action_bar__no_help_info)
        )
    }

    /**
     * @return a list of help items to show any time this is visible
     */
    fun getHelpShowcases(): List<ComposeHelpShowcaseItem>

    /**
     * @return the default priority for this fragment's help actions. Lower number = higher priority
     * Will override [DEFAULT_HELP_PRIORITY] but will be overridden by an individual
     * [ComposeHelpShowcaseItem]'s priority if set
     * @see ComposeHelpShowcaseItem.priority
     */
    fun getHelpPriority(): Int?
}
