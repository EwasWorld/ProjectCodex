package eywa.projectcodex.components.archerHandicaps.add

import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo

internal sealed class ArcherHandicapsAddIntent {
    data class DateChanged(val info: UpdateCalendarInfo) : ArcherHandicapsAddIntent()
    data class HandicapTextUpdated(val value: String?) : ArcherHandicapsAddIntent()
    object SubmitPressed : ArcherHandicapsAddIntent()
    object CloseHandled : ArcherHandicapsAddIntent()
}
