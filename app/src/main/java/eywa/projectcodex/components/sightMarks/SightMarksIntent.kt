package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent
import eywa.projectcodex.model.SightMark

sealed class SightMarksIntent {
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : SightMarksIntent()
    data class MenuAction(val action: SightMarksMenuIntent) : SightMarksIntent()
    data class SightMarkClicked(val item: SightMark) : SightMarksIntent()
    object CreateSightMarkClicked : SightMarksIntent()

    object StartShiftAndScale : SightMarksIntent()
    object EndShiftAndScale : SightMarksIntent()
    sealed class ShiftAndScaleIntent : SightMarksIntent() {
        object FlipClicked : ShiftAndScaleIntent()
        data class Shift(val increased: Boolean, val bigger: Boolean) : ShiftAndScaleIntent()
        data class Scale(val increased: Boolean, val bigger: Boolean) : ShiftAndScaleIntent()
        object ShiftReset : ShiftAndScaleIntent()
        object ScaleReset : ShiftAndScaleIntent()
        object SubmitClicked : ShiftAndScaleIntent()
        object ConfirmSubmitClicked : ShiftAndScaleIntent()
        object CancelSubmitClicked : ShiftAndScaleIntent()
    }

    object CreateSightMarkHandled : SightMarksIntent()
    object OpenSightMarkHandled : SightMarksIntent()
}
