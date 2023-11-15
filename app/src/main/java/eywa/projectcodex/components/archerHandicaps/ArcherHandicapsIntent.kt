package eywa.projectcodex.components.archerHandicaps

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.database.archer.DatabaseArcherHandicap

sealed class ArcherHandicapsIntent {
    data class RowClicked(val item: DatabaseArcherHandicap) : ArcherHandicapsIntent()
    object AddClicked : ArcherHandicapsIntent()
    object AddHandled : ArcherHandicapsIntent()
    data class DeleteClicked(val item: DatabaseArcherHandicap) : ArcherHandicapsIntent()
    object DeleteDialogOkClicked : ArcherHandicapsIntent()
    object DeleteDialogCancelClicked : ArcherHandicapsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ArcherHandicapsIntent()
}
