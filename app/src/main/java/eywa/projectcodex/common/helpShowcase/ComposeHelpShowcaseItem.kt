package eywa.projectcodex.common.helpShowcase

import android.app.Activity
import androidx.compose.ui.layout.LayoutCoordinates

class ComposeHelpShowcaseItem(
        private val layoutCoordinates: LayoutCoordinates,
        private var helpTitle: String? = null,
        private var helpBody: String? = null,
        private var shapePadding: Int? = null,
        override var priority: Int? = HelpShowcaseItem.DEFAULT_HELP_PRIORITY,
) : HelpShowcaseItem {
    override fun show(activity: Activity, remainingItems: List<HelpShowcaseItem>?) {
        TODO("Not yet implemented")
    }
    //    override fun show(activity: Activity, remainingItems: List<HelpShowcaseItem>?) {
//        ComposeHelpShowcase(
//                viewInfo = layoutCoordinates,
//                screenHeight = 200.dp,
//                screenWidth = 100.dp,
//                shown = true
//        )
//    }
}