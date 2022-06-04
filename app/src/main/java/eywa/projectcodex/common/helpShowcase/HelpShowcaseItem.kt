package eywa.projectcodex.common.helpShowcase

import androidx.appcompat.app.AppCompatActivity

interface HelpShowcaseItem {
    companion object {
        const val DEFAULT_HELP_PRIORITY = 0
    }

    var priority: Int?

    fun show(
            activity: AppCompatActivity,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit
    )

    /**
     * The shape the showcase will use to highlight the given view
     */
    enum class Shape { CIRCLE, OVAL, RECTANGLE, NO_SHAPE }
}