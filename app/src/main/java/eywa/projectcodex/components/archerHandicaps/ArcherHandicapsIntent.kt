package eywa.projectcodex.components.archerHandicaps

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.archer.HandicapType

sealed class ArcherHandicapsIntent {
    data class RowClicked(val item: DatabaseArcherHandicap) : ArcherHandicapsIntent()
    object AddClicked : ArcherHandicapsIntent()
    object EditClicked : ArcherHandicapsIntent()
    object EditSubmit : ArcherHandicapsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ArcherHandicapsIntent()
    data class AddHandicapTextUpdated(val value: String?) : ArcherHandicapsIntent()
    object AddSubmit : ArcherHandicapsIntent()
    object SelectHandicapTypeOpen : ArcherHandicapsIntent()
    object SelectHandicapTypeDialogClose : ArcherHandicapsIntent()
    data class SelectHandicapTypeDialogItemClicked(val value: HandicapType) : ArcherHandicapsIntent()
}
