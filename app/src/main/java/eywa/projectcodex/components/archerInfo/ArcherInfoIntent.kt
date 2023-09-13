package eywa.projectcodex.components.archerInfo

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow

sealed class ArcherInfoIntent {
    data class SetIsGent(val isGent: Boolean) : ArcherInfoIntent()

    data class AgeSelected(val age: ClassificationAge) : ArcherInfoIntent()
    object AgeClicked : ArcherInfoIntent()

    data class BowSelected(val bow: ClassificationBow) : ArcherInfoIntent()
    object BowClicked : ArcherInfoIntent()

    object CloseDropdown : ArcherInfoIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ArcherInfoIntent()
}
