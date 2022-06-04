package eywa.projectcodex.common.helpShowcase

import android.graphics.drawable.ColorDrawable
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.ComposeView
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcase
import eywa.projectcodex.common.utils.getColourResource

class ComposeHelpShowcaseItem(
        @StringRes var helpTitle: Int,
        @StringRes private var helpBody: Int,
        private var shapePadding: Int? = null,
        override var priority: Int? = HelpShowcaseItem.DEFAULT_HELP_PRIORITY,
) : HelpShowcaseItem {
    private var layoutCoordinates: LayoutCoordinates? = null
    private var isShown by mutableStateOf(false)

    private fun setIsShown(value: Boolean, activity: AppCompatActivity) {
        isShown = value
        activity.setTitleBarColor(value)
    }

    override fun show(
            activity: AppCompatActivity,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit
    ) {
        val composeView = activity.findViewById<ComposeView>(R.id.content_main_compose)
        activity.setTitleBarColor(true)
        composeView.setContent {
            ComposeHelpShowcase(
                    viewInfo = layoutCoordinates!!,
                    screenHeight = composeView.height.toFloat(),
                    screenWidth = composeView.width.toFloat(),
                    shown = isShown,
                    onDismissListener = {
                        setIsShown(false, activity)
                        endShowcaseListener()
                    }
            )
        }
        setIsShown(true, activity)
        // TODO Add text
    }

    private fun AppCompatActivity.setTitleBarColor(isShowcaseShown: Boolean) {
        val color = if (isShowcaseShown) R.color.colorPrimaryDarkTransparent else R.color.colorPrimary
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColourResource(resources, color, theme)))
    }

    fun updateLayoutCoordinates(layoutCoordinates: LayoutCoordinates) {
        this.layoutCoordinates = layoutCoordinates
    }
}