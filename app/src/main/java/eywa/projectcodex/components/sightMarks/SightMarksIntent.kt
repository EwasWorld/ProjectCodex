package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent

sealed class SightMarksIntent {
    data class MenuAction(val action: SightMarksMenuIntent) : SightMarksIntent()
    data class SightMarkClicked(val item: SightMark) : SightMarksIntent()
    object CreateSightMarkClicked : SightMarksIntent()

    object CreateSightMarkHandled : SightMarksIntent()
    object OpenSightMarkHandled : SightMarksIntent()
}
