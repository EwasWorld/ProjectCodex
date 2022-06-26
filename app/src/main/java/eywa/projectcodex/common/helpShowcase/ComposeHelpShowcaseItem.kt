package eywa.projectcodex.common.helpShowcase

import android.graphics.drawable.ColorDrawable
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcase
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcaseState
import eywa.projectcodex.common.utils.getColourResource

class ComposeHelpShowcaseItem(
        @StringRes var helpTitle: Int,
        @StringRes private var helpBody: Int,
        private var shapePadding: Dp? = null,
        override var priority: Int? = HelpShowcaseItem.DEFAULT_HELP_PRIORITY,
) : HelpShowcaseItem {
    private var layoutCoordinates: LayoutCoordinates? = null
    private var isShown = MutableTransitionState(false)

    private fun setIsShown(value: Boolean, activity: AppCompatActivity) {
        isShown.targetState = value
        activity.setTitleBarColor(value)
    }

    override fun show(
            activity: AppCompatActivity,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit
    ) {
        val composeView = activity.findViewById<ComposeView>(R.id.content_main_compose)
        composeView.setContent {
            ComposeHelpShowcase(
                    ComposeHelpShowcaseState.from(
                            title = stringResource(id = helpTitle),
                            message = stringResource(id = helpBody),
                            viewInfo = layoutCoordinates!!,
                            screenHeight = composeView.height.toFloat(),
                            screenWidth = composeView.width.toFloat(),
                            padding = shapePadding ?: ComposeHelpShowcaseState.DEFAULT_PADDING,
                            density = LocalDensity.current,
                            shown = isShown,
                            closeButtonListener = {
                                setIsShown(false, activity)
                                endShowcaseListener()
                            },
                            nextButtonListener = {
                                setIsShown(false, activity)
                                goToNextItemListener()
                            },
                            overlayClickedListener = {
                                setIsShown(false, activity)
                                goToNextItemListener()
                            }
                    )
            )
        }
        setIsShown(true, activity)
    }

    private fun AppCompatActivity.setTitleBarColor(isShowcaseShown: Boolean) {
        val color = if (isShowcaseShown) R.color.colorPrimaryDarkTransparent else R.color.colorPrimary
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColourResource(resources, color, theme)))
    }

    fun updateLayoutCoordinates(layoutCoordinates: LayoutCoordinates) {
        this.layoutCoordinates = layoutCoordinates
    }
}