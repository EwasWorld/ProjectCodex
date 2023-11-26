package eywa.projectcodex.database.bow

import eywa.projectcodex.common.utils.CodexPreviewHelperDsl

@CodexPreviewHelperDsl
object DatabaseBowPreviewHelper {
    val default = DatabaseBow(
            bowId = DEFAULT_BOW_ID,
            name = "Default",
            isSightMarkDiagramHighestAtTop = false,
    )
}
