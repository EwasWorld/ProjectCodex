package eywa.projectcodex.common.helpShowcase

import android.app.Activity

interface HelpShowcaseItem {
    companion object {
        const val DEFAULT_HELP_PRIORITY = 0
    }

    var priority: Int?

    fun show(activity: Activity, remainingItems: List<HelpShowcaseItem>?)

    /**
     * The shape the showcase will use to highlight the given view
     */
    enum class Shape { CIRCLE, OVAL, RECTANGLE, NO_SHAPE }
}