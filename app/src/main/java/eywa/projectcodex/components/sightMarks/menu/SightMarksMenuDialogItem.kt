package eywa.projectcodex.components.sightMarks.menu

import android.content.res.Resources
import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.CodexMenuDialogItem
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.sightMarks.SightMarksTestTag
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.*
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent.*

enum class SightMarksMenuDialogItem(
        @StringRes val displayNameId: Int,
        val intent: SightMarksMenuIntent,
        val testTag: SightMarksTestTag,
) {
    FLIP_DIAGRAM(R.string.sight_marks__menu_flip_diagram, FlipDiagram, FLIP_DIAGRAM_MENU_BUTTON),
    ARCHIVE_ALL(R.string.sight_marks__menu_archive_all, ArchiveAll, ARCHIVE_MENU_BUTTON),
    SHIFT_AND_SCALE(R.string.sight_marks__menu_shift_and_scale, ShiftAndScale, SHIFT_AND_SCALE_MENU_BUTTON),
    ;

    fun asCodexMenuItem(
            resources: Resources,
            listener: (SightMarksMenuIntent) -> Unit,
    ) = object : CodexMenuDialogItem {
        override val displayName: String = resources.getString(displayNameId)
        override val onClick: () -> Unit = { listener(intent) }
        override val itemTestTag: CodexTestTag = testTag
    }
}
