package eywa.projectcodex.components.commonUtils

import android.app.Activity
import android.content.res.Resources
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.ActionBarHelp.ShowcaseShape.Companion.setShowcaseShape
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
     */
    class HelpShowcaseItem private constructor() : Comparable<HelpShowcaseItem> {
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
                val hasItemsAfterThis = remainingItems.size > 1

                endSequenceOnDismiss = false
                val dismissText = if (hasItemsAfterThis) R.string.general_next else R.string.action_bar__close_help
                val showcaseBuilder = MaterialShowcaseView.Builder(activity)
                        .setTarget(itemToShow.getView(activity))
                        .setDismissText(activity.getString(dismissText))
                        .setTitleText(itemToShow.getTitle(activity.resources))
                        .setContentText(itemToShow.getBody(activity.resources))
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
                        .setShowcaseShape(itemToShow.shape)
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
                if (itemToShow.shapePadding != null) {
                    showcaseBuilder.setShapePadding(itemToShow.shapePadding!!)
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
         * The ID of the view to highlight (can be null if [view] is set or if [shape] is set to [ShowcaseShape.NO_SHAPE])
         */
        private var viewId: Int? = null

        /**
         * The view to highlight (can be null if [viewId] is set or if [shape] is set to [ShowcaseShape.NO_SHAPE])
         */
        private var view: View? = null

        /**
         * The ID of the title of the showcase
         */
        private var helpTitleId: Int? = null

        /**
         * The title of the showcase
         */
        private var helpTitle: String? = null

        /**
         * The ID of the body/message to display on showcase
         */
        private var helpBodyId: Int? = null

        /**
         * The body/message to display on showcase
         */
        private var helpBody: String? = null

        /**
         * The shape to use to highlight the view ([ShowcaseShape.NO_SHAPE] will take precedence over [view] and [viewId])
         */
        private var shape: ShowcaseShape = ShowcaseShape.CIRCLE

        /**
         * Set the padding between the outer edge of the view and the highlight shape
         */
        private var shapePadding: Int? = null

        /**
         * Indicates the order in which [HelpShowcaseItem]s will be shown. Lower number = higher priority
         *
         * Setter: sets if and only if the current value is null
         */
        var priority: Int? = null
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

        fun getTitle(resources: Resources): String {
            return helpTitle ?: resources.getString(helpTitleId!!)
        }

        fun getBody(resources: Resources): String {
            return helpBody ?: resources.getString(helpBodyId!!)
        }

        fun getView(activity: Activity): View {
            return when {
                shape == ShowcaseShape.NO_SHAPE -> activity.findViewById(R.id.action_bar__help)
                view != null -> view!!
                else -> activity.findViewById(viewId!!)
            }
        }

        class Builder {
            /**
             * Separate field for priority as after it becomes non-null in the actual item, it cannot be changed
             * @see HelpShowcaseItem.priority
             */
            private var priority: Int? = null
            private val item = HelpShowcaseItem()
            private var isBuilt = false

            fun build(): HelpShowcaseItem {
                check(!isBuilt) { "Item already build (build)" }
                require(item.helpTitleId != null || item.helpTitle != null) { "No title given" }
                require(item.helpBodyId != null || item.helpBody != null) { "No body given" }
                require(item.shape == ShowcaseShape.NO_SHAPE || item.viewId != null || item.view != null)
                isBuilt = true
                item.priority = priority
                return item
            }

            fun setViewId(value: Int): Builder {
                check(!isBuilt) { "Item already build (setViewId)" }
                item.viewId = value
                item.view = null
                return this
            }

            fun setView(value: View): Builder {
                check(!isBuilt) { "Item already build (setView)" }
                item.view = value
                item.viewId = null
                return this
            }

            fun setHelpTitleId(value: Int): Builder {
                check(!isBuilt) { "Item already build (setHelpTitleId)" }
                item.helpTitleId = value
                item.helpTitle = null
                return this
            }

            fun setHelpTitle(value: String): Builder {
                check(!isBuilt) { "Item already build (setHelpTitle)" }
                item.helpTitle = value
                item.helpTitleId = null
                return this
            }

            fun setHelpBodyId(value: Int): Builder {
                check(!isBuilt) { "Item already build (setHelpBodyId)" }
                item.helpBodyId = value
                item.helpBody = null
                return this
            }

            fun setHelpBody(value: String): Builder {
                check(!isBuilt) { "Item already build (setHelpBody)" }
                item.helpBody = value
                item.helpBodyId = null
                return this
            }

            fun setPriority(value: Int?): Builder {
                check(!isBuilt) { "Item already build (setPriority)" }
                priority = value
                return this
            }

            fun setShape(value: ShowcaseShape): Builder {
                check(!isBuilt) { "Item already build (setShape)" }
                item.shape = value
                if (value == ShowcaseShape.NO_SHAPE) {
                    item.view = null
                    item.viewId = null
                    item.shapePadding = null
                }
                return this
            }

            fun setShapePadding(value: Int): Builder {
                check(!isBuilt) { "Item already build (setShapePadding)" }
                item.shapePadding = value
                return this
            }
        }
    }

    /**
     * The shape the showcase will use to highlight the given view
     */
    enum class ShowcaseShape {
        CIRCLE, OVAL, RECTANGLE, NO_SHAPE;

        companion object {
            fun MaterialShowcaseView.Builder.setShowcaseShape(showcaseShape: ShowcaseShape): MaterialShowcaseView.Builder {
                return when (showcaseShape) {
                    CIRCLE -> this.withCircleShape()
                    OVAL -> this.withOvalShape()
                    RECTANGLE -> this.withRectangleShape()
                    NO_SHAPE -> this.withoutShape()
                }
            }
        }
    }
}