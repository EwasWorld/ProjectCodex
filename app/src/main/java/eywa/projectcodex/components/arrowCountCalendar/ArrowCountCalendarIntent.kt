package eywa.projectcodex.components.arrowCountCalendar

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent

sealed class ArrowCountCalendarIntent {
    object GoToNextMonth : ArrowCountCalendarIntent()
    object GoToPreviousMonth : ArrowCountCalendarIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ArrowCountCalendarIntent()
}
