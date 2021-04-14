package eywa.projectcodex.ui.commonElements

import android.app.Activity
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.github.amlcurran.showcaseview.ShowcaseDrawer
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener
import com.github.amlcurran.showcaseview.targets.ViewTarget
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Used on fragments to add actions to the help icon on the action bar
 */
interface ActionBarHelp {
    companion object {
        protected var showcaseInProgress = false

        /**
         * Executes the [getHelpShowcases] showcases for all [fragments] in order of priority
         *
         * @param fragments fragments current shown which implement [ActionBarHelp]
         */
        fun executeHelpPressed(fragments: List<ActionBarHelp>, activity: Activity) {
            if (showcaseInProgress) return
            showcaseInProgress = true
            if (fragments.isEmpty()) {
                throw IllegalStateException("No help information defined")
            }
            val priorityQueue = PriorityQueue<HelpShowcaseItem>(5)
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
                    showcaseInProgress = false
                    return
                }
                val view = activity.findViewById<View>(itemToShow.viewId)

                var showcase: ShowcaseView? = null
                showcase = ShowcaseView
                        .Builder(activity)
                        .setTarget(ViewTarget(itemToShow.viewId, activity))
                        .setContentTitle(itemToShow.helpTitle)
                        .setContentText(itemToShow.helpBody)
                        // Allows us to cancel the chain of showcases when the user presses outside of the view
                        .blockAllTouches()
                        .setShowcaseDrawer(CustomShowcaseDrawer(view, itemToShow.shape))
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
                showcase.setButtonText(activity.resources.getString(if (remainingItems.isNullOrEmpty()) R.string.button_finish else R.string.button_next))
                showcase!!.show()
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

    interface DrawableShape {
        fun draw(
                canvas: Canvas,
                width: Int,
                height: Int,
                centreX: Float,
                centreY: Float,
                isOuter: Boolean,
                paint: Paint
        )
    }

    enum class ShowcaseShape : DrawableShape {
        CIRCLE {
            override fun draw(
                    canvas: Canvas,
                    width: Int,
                    height: Int,
                    centreX: Float,
                    centreY: Float,
                    isOuter: Boolean,
                    paint: Paint
            ) {
                val sizeIncrease = getSizeIncrease(isOuter)
                val maxRadius = getMaxRadius(width, height) + sizeIncrease
                canvas.drawCircle(centreX, centreY, maxRadius.toFloat() + sizeIncrease, paint)
            }
        },
        ROUNDED_RECTANGLE {
            override fun draw(
                    canvas: Canvas,
                    width: Int,
                    height: Int,
                    centreX: Float,
                    centreY: Float,
                    isOuter: Boolean,
                    paint: Paint
            ) {
                val sizeIncrease = getSizeIncrease(isOuter)
                val xRadius = centreX + width / 2 + sizeIncrease
                val yRadius = centreY + height / 2 + sizeIncrease
                canvas.drawRoundRect(
                        RectF(centreX - xRadius, centreY - yRadius, centreX + xRadius, centreY + yRadius),
                        roundingRadius,
                        roundingRadius,
                        paint
                )
            }
        };

        companion object {
            private const val roundingRadius = 1.3f
            private const val outerSizeIncrease = 48

            private fun getSizeIncrease(isOuter: Boolean) = if (isOuter) outerSizeIncrease else 0
            fun getMaxRadius(width: Int, height: Int) = sqrt((width / 2.0).pow(2) + (height / 2.0).pow(2))
        }
    }


    class CustomShowcaseDrawer(view: View, private val shape: ShowcaseShape) : ShowcaseDrawer {
        private val eraserPaint = Paint()
        private val width: Int = view.width
        private val height: Int = view.height
        private val basicPaint = Paint()
        private var backgroundColor: Int = Color.WHITE
        private val ALPHA_60_PERCENT = 153

        init {
            CustomLogger.customLogger.i("TEST", "View dimensions: $width, $height")
            eraserPaint.color = Color.RED
            eraserPaint.alpha = 0
            val xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            eraserPaint.xfermode = xfermode
            eraserPaint.isAntiAlias = true
            basicPaint.color = Color.GREEN
        }

        override fun setShowcaseColour(color: Int) {
            this.eraserPaint.color = color
        }

        override fun drawShowcase(buffer: Bitmap?, x: Float, y: Float, scaleMultiplier: Float) {
            // Note: scaleMultiplier is not used currently used by the api
            val bufferCanvas = Canvas(buffer!!)
            eraserPaint.alpha = ALPHA_60_PERCENT
            shape.draw(bufferCanvas, width, height, x, y, true, eraserPaint)
            eraserPaint.alpha = 0
            shape.draw(bufferCanvas, width, height, x, y, false, eraserPaint)
        }

        override fun getShowcaseWidth(): Int {
            return width
        }

        override fun getShowcaseHeight(): Int {
            return height
        }

        override fun getBlockedRadius(): Float {
            // TODO Check whether this is what I want
            return ShowcaseShape.getMaxRadius(width, height).toFloat()
        }

        override fun setBackgroundColour(backgroundColor: Int) {
            this.backgroundColor = backgroundColor
        }

        override fun erase(bitmapBuffer: Bitmap?) {
            bitmapBuffer?.eraseColor(backgroundColor)
        }

        override fun drawToCanvas(canvas: Canvas?, bitmapBuffer: Bitmap?) {
            canvas!!.drawBitmap(bitmapBuffer!!, 0f, 0f, basicPaint)
        }
    }
}