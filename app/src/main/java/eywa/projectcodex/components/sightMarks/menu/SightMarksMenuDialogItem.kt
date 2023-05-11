package eywa.projectcodex.components.sightMarks.menu

import android.content.res.Resources
import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.CodexMenuDialogItem

enum class SightMarksMenuDialogItem(
        @StringRes val displayNameId: Int,
        val intent: SightMarksMenuIntent,
) {
    FLIP_DIAGRAM(R.string.sight_marks__menu_flip_diagram, SightMarksMenuIntent.FlipDiagram),
    ARCHIVE_ALL(R.string.sight_marks__menu_archive_all, SightMarksMenuIntent.ArchiveAll),
    ;

    fun asCodexMenuItem(
            resources: Resources,
            listener: (SightMarksMenuIntent) -> Unit,
    ) = object : CodexMenuDialogItem {
        override val displayName: String = resources.getString(displayNameId)
        override val onClick: () -> Unit = { listener(intent) }
    }
}
