package eywa.projectcodex.ui.commonUtils

import android.app.Activity
import android.graphics.*
import eywa.projectcodex.R
import eywa.projectcodex.ui.getColourResource
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import java.util.*


/**
 * Used on fragments to add actions to the help icon on the action bar
 */
interface ActionBarHelp {
    companion object {
        private val showcaseInProgressLock = Object()
        protected var showcaseInProgress = false

        /**
         * Executes the [getHelpShowcases] showcases for all [fragments] in order of priority
         *
         * @param fragments fragments current shown which implement [ActionBarHelp]
         */
        fun executeHelpPressed(fragments: List<ActionBarHelp>, activity: Activity) {
            synchronized(showcaseInProgressLock) {
                if (showcaseInProgress) return
                showcaseInProgress = true
            }
            if (fragments.isEmpty()) {
                throw IllegalStateException("No help information defined")
            }
            val priorityQueue = PriorityQueue<HelpShowcaseItem>(1)
            for (fragment in fragments) {
                fragment.getHelpShowcases().forEach {
                    it.setPriorityIfNull(fragment.getHelpPriority())
                    priorityQueue.offer(it)
                }
            }
            HelpShowcaseItem.showNext(activity, priorityQueue)
        }
    }

    /**
     * @return a list of showcase views which highlight UI elements with a priority (lower number is higher priority)
     */
    fun getHelpShowcases(): List<HelpShowcaseItem>

    /**
     * @return the default priority for this fragment's help actions. Will override [DEFAULT_HELP_PRIORITY] but will be
     * overridden by an individual [HelpShowcaseItem]'s priority if set. Lower number = higher priority
     */
    fun getHelpPriority(): Int?

    /**
     * Information about a view which will be highlighted when the help icon on the action bar is pressed
     *
     * @param viewId the id of the view to highlight
     * @param helpTitle the title of the showcase when highlighting [viewId]
     * @param helpBody the body of the showcase when highlighting [viewId]
     * @param priority indicates the order in which [HelpShowcaseItem]s will be shown. Lower number = higher priority
     */
    class HelpShowcaseItem(
            private val viewId: Int,
            private val helpTitle: String,
            private val helpBody: String,
            private var priority: Int? = null,
            private val shape: ShowcaseShape = ShowcaseShape.CIRCLE
    ) : Comparable<HelpShowcaseItem> {
        companion object {
            private const val DEFAULT_HELP_PRIORITY = 0

            /**
             * Turns the next [HelpShowcaseItem] in [remainingItems] into a [ShowcaseView] and shows it. Dismissing it
             * will trigger the next item in [remainingItems] to be shown. Pressing anywhere else will end the showcase
             */
            fun showNext(activity: Activity, remainingItems: PriorityQueue<HelpShowcaseItem>?) {
                val itemToShow = remainingItems?.poll()
                if (itemToShow == null) {
                    synchronized(showcaseInProgressLock) {
                        showcaseInProgress = false
                    }
                    return
                }

                val showcaseBuilder = MaterialShowcaseView.Builder(activity)
                        .setTarget(activity.findViewById(itemToShow.viewId))
                        .setDismissText("Got it")
                        .setTitleText(itemToShow.helpTitle)
                        .setContentText(itemToShow.helpBody)
                        .setMaskColour(
                                getColourResource(
                                        activity.resources, R.color.colorPrimaryDarkTransparent, activity.theme
                                )
                        )
                        .setTitleTextColor(getColourResource(activity.resources, R.color.white, activity.theme))
                        .setContentTextColor(getColourResource(activity.resources, R.color.white, activity.theme))
                        .setDismissTextColor(getColourResource(activity.resources, R.color.white, activity.theme))
                        .setListener(object : IShowcaseListener {
                            override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView?) {
                            }

                            override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView?) {
                                showNext(activity, remainingItems)
                            }
                        })
                when (itemToShow.shape) {
                    ShowcaseShape.CIRCLE -> showcaseBuilder.withCircleShape()
                    ShowcaseShape.OVAL -> showcaseBuilder.withOvalShape()
                    ShowcaseShape.RECTANGLE -> showcaseBuilder.withRectangleShape()
                }
                showcaseBuilder.show()
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

        /**
         * Sets the [priority] to [newPriority] if [priority] is null, else does nothing
         */
        fun setPriorityIfNull(newPriority: Int?) {
            if (priority == null) {
                priority = newPriority
            }
        }
    }

    /**
     * The shape the showcase will use to highlight the given view
     */
    enum class ShowcaseShape { CIRCLE, OVAL, RECTANGLE }
}