package eywa.projectcodex.ui.commonElements

import android.app.Activity
import android.view.MotionEvent
import android.widget.Button
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener
import com.github.amlcurran.showcaseview.targets.ViewTarget
import eywa.projectcodex.R
import java.util.*

/**
 * Used on fragments to add actions to the help icon on the action bar
 */
interface ActionBarHelp {
    companion object {
        private const val DEFAULT_HELP_PRIORITY = 0

        /**
         * Executes the [getHelpShowcases] showcases for all [fragments] in order of priority
         *
         * @param fragments fragments current shown which implement [ActionBarHelp]
         */
        fun executeHelpPressed(fragments: List<ActionBarHelp>, activity: Activity) {
            if (fragments.isEmpty()) {
                throw IllegalStateException("No help information defined")
            }
            val priorityQueue = PriorityQueue(5,
                    Comparator<HelpShowcaseItem> { o1, o2 ->
                        val o1Priority = o1?.priority ?: DEFAULT_HELP_PRIORITY
                        val o2Priority = o2?.priority ?: DEFAULT_HELP_PRIORITY
                        o1Priority.compareTo(o2Priority)
                    })
            for (fragment in fragments) {
                fragment.getHelpShowcases().forEach {
                    if (it.priority == null) {
                        it.priority = fragment.getHelpPriority() ?: DEFAULT_HELP_PRIORITY
                    }
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
            var priority: Int? = null
    ) {
        companion object {
            /**
             * Turns the next [HelpShowcaseItem] in [remainingItems] into a [ShowcaseView] and shows it. Dismissing it
             * will trigger the next item in [remainingItems] to be shown. Pressing anywhere else will end the showcase
             */
            fun showNext(activity: Activity, remainingItems: PriorityQueue<HelpShowcaseItem>?) {
                val itemToShow = remainingItems?.poll() ?: return

                val button = Button(activity.applicationContext)
                button.setText(if (remainingItems.isNullOrEmpty()) R.string.button_finish else R.string.button_next)

                var showcase: ShowcaseView? = null
                showcase = ShowcaseView
                        .Builder(activity)
                        .setTarget(ViewTarget(itemToShow.viewId, activity))
                        .setContentTitle(itemToShow.helpTitle)
                        .setContentText(itemToShow.helpBody)
                        // Allows us to cancel the chain of showcases when the user presses outside of the view
                        .blockAllTouches()
                        .replaceEndButton(button)
                        .setShowcaseEventListener(object : SimpleShowcaseEventListener() {
                            var continueToNextShowcase = true

                            override fun onShowcaseViewDidHide(showcaseView: ShowcaseView?) {
                                super.onShowcaseViewDidHide(showcaseView)
                                if (!continueToNextShowcase) {
                                    return
                                }
                                showNext(activity, remainingItems)
                            }

                            override fun onShowcaseViewTouchBlocked(motionEvent: MotionEvent?) {
                                super.onShowcaseViewTouchBlocked(motionEvent)
                                continueToNextShowcase = false
                                showcase!!.hide()
                            }
                        })
                        .build()
                showcase!!.show()
            }
        }
    }
}