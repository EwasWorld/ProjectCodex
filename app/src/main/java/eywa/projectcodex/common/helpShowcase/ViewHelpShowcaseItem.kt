package eywa.projectcodex.common.helpShowcase

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.getColourResource
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView

/**
 * Information about a view which will be highlighted when the help icon on the action bar is pressed
 */
class ViewHelpShowcaseItem private constructor() : HelpShowcaseItem, Comparable<ViewHelpShowcaseItem> {
    private var endSequenceOnDismiss = false

    /**
     * Displays the current item as a [MaterialShowcaseView].
     *
     * Dismissing it will trigger [goToNextItemListener] if it [hasNextItem]. Pressing anywhere else will end
     * the showcase
     *
     * Note cannot use [MaterialShowcaseSequence] as when it sets the config for each item, colours represented
     * using negatives are not accepted (even though that is the standard way to represent colours...)
     */
    override fun show(
            activity: AppCompatActivity,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit
    ) {
        endSequenceOnDismiss = false
        val dismissText = if (hasNextItem) R.string.general_next else R.string.action_bar__close_help
        val showcaseBuilder = MaterialShowcaseView.Builder(activity)
                .setTarget(
                        when (shape) {
                            HelpShowcaseItem.Shape.NO_SHAPE -> activity.findViewById(R.id.action_bar__help)
                            else -> view ?: activity.findViewById(viewId!!)
                        }
                )
                .setDismissText(activity.getString(dismissText))
                .setTitleText(helpTitle ?: activity.resources.getString(helpTitleId!!))
                .setContentText(helpBody ?: activity.resources.getString(helpBodyId!!))
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
                .setShowcaseShape(shape)
                .setContentTextColor(getColourResource(activity.resources, R.color.white, activity.theme))
                .setDismissTextColor(getColourResource(activity.resources, R.color.white, activity.theme))
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .setListener(object : IShowcaseListener {
                    override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView?) {
                    }

                    override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView?) {
                        if (endSequenceOnDismiss || !hasNextItem) endShowcaseListener() else goToNextItemListener()
                    }
                })

        val skipText = activity.getString(R.string.action_bar__close_help)
        if (hasNextItem) {
            showcaseBuilder.setSkipText(skipText)
        }
        shapePadding?.let { showcaseBuilder.setShapePadding(it) }

        /*
         * Build showcase view
         */
        val showcaseView = showcaseBuilder.build()
        if (hasNextItem) {
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

    /**
     * The ID of the view to highlight (can be null if [view] is set or if [shape] is set to [HelpShowcaseItem.Shape.NO_SHAPE])
     */
    private var viewId: Int? = null

    /**
     * The view to highlight (can be null if [viewId] is set or if [shape] is set to [HelpShowcaseItem.Shape.NO_SHAPE])
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
     * The shape to use to highlight the view ([HelpShowcaseItem.Shape.NO_SHAPE] will take precedence over [view] and [viewId])
     */
    private var shape: HelpShowcaseItem.Shape = HelpShowcaseItem.Shape.CIRCLE

    /**
     * Set the padding between the outer edge of the view and the highlight shape
     */
    private var shapePadding: Int? = null

    /**
     * Indicates the order in which [ViewHelpShowcaseItem]s will be shown. Lower number = higher priority
     *
     * Setter: sets if and only if the current value is null
     */
    override var priority: Int? = null
        set(value) {
            if (field == null) {
                field = value
            }
        }

    /**
     * Compares two items (uses [HelpShowcaseItem.DEFAULT_HELP_PRIORITY] for any null values)
     */
    override fun compareTo(other: ViewHelpShowcaseItem): Int {
        val o1Priority = priority ?: HelpShowcaseItem.DEFAULT_HELP_PRIORITY
        val o2Priority = other.priority ?: HelpShowcaseItem.DEFAULT_HELP_PRIORITY
        return o1Priority.compareTo(o2Priority)
    }

    private fun MaterialShowcaseView.Builder.setShowcaseShape(showcaseShape: HelpShowcaseItem.Shape)
            : MaterialShowcaseView.Builder {
        return when (showcaseShape) {
            HelpShowcaseItem.Shape.CIRCLE -> this.withCircleShape()
            HelpShowcaseItem.Shape.OVAL -> this.withOvalShape()
            HelpShowcaseItem.Shape.RECTANGLE -> this.withRectangleShape()
            HelpShowcaseItem.Shape.NO_SHAPE -> this.withoutShape()
        }
    }

    class Builder {
        /**
         * Separate field for priority as after it becomes non-null in the actual item, it cannot be changed
         * @see ViewHelpShowcaseItem.priority
         */
        private var priority: Int? = null
        private val item = ViewHelpShowcaseItem()
        private var isBuilt = false

        fun build(): ViewHelpShowcaseItem {
            check(!isBuilt) { "Item already build (build)" }
            require(item.helpTitleId != null || item.helpTitle != null) { "No title given" }
            require(item.helpBodyId != null || item.helpBody != null) { "No body given" }
            require(item.shape == HelpShowcaseItem.Shape.NO_SHAPE || item.viewId != null || item.view != null)
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

        fun setShape(value: HelpShowcaseItem.Shape): Builder {
            check(!isBuilt) { "Item already build (setShape)" }
            item.shape = value
            if (value == HelpShowcaseItem.Shape.NO_SHAPE) {
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