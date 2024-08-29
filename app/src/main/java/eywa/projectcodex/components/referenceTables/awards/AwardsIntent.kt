package eywa.projectcodex.components.referenceTables.awards

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow

sealed class AwardsIntent {
    data object BowClicked : AwardsIntent()
    data class BowSelected(val bow: ClassificationBow) : AwardsIntent()
    data object CloseDropdown : AwardsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : AwardsIntent()
}
