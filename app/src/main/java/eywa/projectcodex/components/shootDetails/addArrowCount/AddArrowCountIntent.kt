package eywa.projectcodex.components.shootDetails.addArrowCount

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent

sealed class AddArrowCountIntent {
    object ClickIncrease : AddArrowCountIntent()
    object ClickDecrease : AddArrowCountIntent()
    object ClickSubmit : AddArrowCountIntent()
    data class OnValueChanged(val value: String?) : AddArrowCountIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : AddArrowCountIntent()
    object ClickEditShootInfo : AddArrowCountIntent()
    object EditShootInfoHandled : AddArrowCountIntent()
    object FullSightMarksClicked : AddArrowCountIntent()
    object FullSightMarksHandled : AddArrowCountIntent()
    object EditSightMarkClicked : AddArrowCountIntent()
    object EditSightMarkHandled : AddArrowCountIntent()
}
