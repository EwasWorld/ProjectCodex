package eywa.projectcodex.components.commonUtils

import android.app.Activity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView


/**
 * Used on fragments to add actions to the help icon on the action bar
 */
interface ActionBarHelp {
    companion object {
        private const val LOG_TAG = "ActionBarHelp"
        private val showcaseInProgressLock = Object()
        protected var showcaseInProgress = false

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
                        activity.resources.getString(R.string.err__no_help_info)
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
            HelpShowcaseItem.showNext(activity, helpItemsList.sortedBy { it.priority })
        }
    }

    /**
     * @return a list of help items to show any time this is visible
     */
    fun getHelpShowcases(): List<HelpShowcaseItem>

    /**
     * @return the default priority for this fragment's help actions. Lower number = higher priority
     * Will override [HelpShowcaseItem.DEFAULT_HELP_PRIORITY] but will be overridden by an individual
     * [HelpShowcaseItem]'s priority if set
     * @see HelpShowcaseItem.priority
     */
    fun getHelpPriority(): Int?

    /**
     * Information about a view which will be highlighted when the help icon on the action bar is pressed
     *
     * @param viewId the id of the view to highlight
     * @param helpTitle the title of the showcase when highlighting [viewId]
     * @param helpBody the body of the showcase when highlighting [viewId]
     * @param priority lower number = higher priority
     * @see HelpShowcaseItem.priority
     */
    class HelpShowcaseItem(
            private val viewId: Int?,
            private val helpTitle: String,
            private val helpBody: String,
            priority: Int? = null,
            private val shape: ShowcaseShape = ShowcaseShape.CIRCLE,
            private val shapePadding: Int? = null
    ) : Comparable<HelpShowcaseItem> {
        companion object {
            const val DEFAULT_HELP_PRIORITY = 0
            private var endSequenceOnDismiss = false

            /**
             * Turns the next [HelpShowcaseItem] in [remainingItems] into a [MaterialShowcaseView] and shows it.
             * Dismissing it will trigger the next item in [remainingItems] to be shown. Pressing anywhere else will end
             * the showcase
             *
             * Note cannot use [MaterialShowcaseSequence] as when it sets the config for each item, colours represented
             * using negatives are not accepted (even though that is the standard way to represent colours...)
             */
            fun showNext(activity: Activity, remainingItems: List<HelpShowcaseItem>?) {
                if (remainingItems.isNullOrEmpty()) {
                    synchronized(showcaseInProgressLock) {
                        showcaseInProgress = false
                    }
                    return
                }
                val itemToShow = remainingItems[0]
                val viewIdToShow = itemToShow.viewId ?: R.id.action_bar__help
                val hasItemsAfterThis = remainingItems.size > 1

                endSequenceOnDismiss = false
                val dismissText = if (hasItemsAfterThis) R.string.general_next else R.string.action_bar__close_help
                val showcaseBuilder = MaterialShowcaseView.Builder(activity)
                        .setTarget(activity.findViewById(viewIdToShow))
                        .setDismissText(activity.getString(dismissText))
                        .setTitleText(itemToShow.helpTitle)
                        .setContentText(itemToShow.helpBody)
                        .setMaskColour(
                                getColourResource(
                                        activity.resources, R.color.colorPrimaryDarkTransparent, activity.theme
                                )
                        )
                        .setTitleTextColor(
                                getColourResource(
                                        activity.resources, R.color.colorLightAccent, activity.theme
                                )
                        )
                        .setContentTextColor(getColourResource(activity.resources, R.color.white, activity.theme))
                        .setDismissTextColor(getColourResource(activity.resources, R.color.white, activity.theme))
                        .setDismissOnTouch(true)
                        .renderOverNavigationBar()
                        .setListener(object : IShowcaseListener {
                            override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView?) {
                            }

                            override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView?) {
                                if (endSequenceOnDismiss) {
                                    synchronized(showcaseInProgressLock) {
                                        showcaseInProgress = false
                                    }
                                }
                                else {
                                    showNext(activity, remainingItems.minus(itemToShow))
                                }
                            }
                        })

                val skipText = activity.getString(R.string.action_bar__close_help)
                if (hasItemsAfterThis) {
                    showcaseBuilder.setSkipText(skipText)
                }
                // Shape properties
                if (itemToShow.viewId != null) {
                    if (itemToShow.shapePadding != null) {
                        showcaseBuilder.setShapePadding(itemToShow.shapePadding)
                    }
                    when (itemToShow.shape) {
                        ShowcaseShape.CIRCLE -> showcaseBuilder.withCircleShape()
                        ShowcaseShape.OVAL -> showcaseBuilder.withOvalShape()
                        ShowcaseShape.RECTANGLE -> showcaseBuilder.withRectangleShape()
                    }
                }
                else {
                    showcaseBuilder.withoutShape()
                }

                /*
                 * Build showcase view
                 */
                val showcaseView = showcaseBuilder.build()
                if (hasItemsAfterThis) {
                    // Set the skip button listener
                    //    (done awkwardly since the library doesn't provide an easy way to do this)
                    showcaseView.children.first { it is LinearLayout }.touchables.first {
                        (it as TextView).text.toString().equals(skipText, ignoreCase = true)
                    }.setOnClickListener {
                        endSequenceOnDismiss = true
                        showcaseView.animateOut() // Triggers onShowcaseDismissed
                    }
                }

                /*
                 * Show showcase view
                 */
                showcaseView.show(activity)
            }
        }

        /**
         * Indicates the order in which [HelpShowcaseItem]s will be shown. Lower number = higher priority
         *
         * Setter: sets if and only if the current value is null
         */
        var priority = priority
            set(value) {
                if (field == null) {
                    field = value
                }
            }

        /**
         * Compares two items (uses [DEFAULT_HELP_PRIORITY] for any null values)
         */
        override fun compareTo(other: HelpShowcaseItem): Int {
            val o1Priority = priority ?: DEFAULT_HELP_PRIORITY
            val o2Priority = other.priority ?: DEFAULT_HELP_PRIORITY
            return o1Priority.compareTo(o2Priority)
        }
    }

    /**
     * The shape the showcase will use to highlight the given view
     */
    enum class ShowcaseShape { CIRCLE, OVAL, RECTANGLE }
}