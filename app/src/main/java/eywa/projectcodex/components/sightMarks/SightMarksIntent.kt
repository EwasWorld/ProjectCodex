package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent
import eywa.projectcodex.model.SightMark

sealed class SightMarksIntent {
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : SightMarksIntent()
    data class MenuAction(val action: SightMarksMenuIntent) : SightMarksIntent()
    data class SightMarkClicked(val item: SightMark) : SightMarksIntent()
    object CreateSightMarkClicked : SightMarksIntent()

    object CreateSightMarkHandled : SightMarksIntent()
    object OpenSightMarkHandled : SightMarksIntent()
}
