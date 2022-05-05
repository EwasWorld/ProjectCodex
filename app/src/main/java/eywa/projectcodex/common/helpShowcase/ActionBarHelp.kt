package eywa.projectcodex.common.helpShowcase

import android.app.Activity
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ToastSpamPrevention


/**
 * Used on fragments to add actions to the help icon on the action bar
 */
interface ActionBarHelp {
    companion object {
        private const val LOG_TAG = "ActionBarHelp"
        val showcaseInProgressLock = Object()
        var showcaseInProgress = false

        /**
         * Executes the [getHelpShowcases] showcases for all [fragments] in order of priority
         *
         * @param fragments fragments current shown which implement [ActionBarHelp]
         */
        fun executeHelpPressed(fragments: List<ActionBarHelp>, activity: Activity) {
            if (fragments.isEmpty()) {
                CustomLogger.customLogger.i(LOG_TAG, "No help information defined")
                ToastSpamPrevention.displayToast(
                        activity.applicationContext,
                        activity.resources.getString(R.string.err_action_bar__no_help_info)
                )
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
            helpItemsList.first().show(activity, helpItemsList.drop(1))
        }
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