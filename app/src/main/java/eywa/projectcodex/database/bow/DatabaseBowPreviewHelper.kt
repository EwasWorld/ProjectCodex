package eywa.projectcodex.database.bow

import eywa.projectcodex.common.utils.CodexPreviewHelperDsl

@CodexPreviewHelperDsl
object DatabaseBowPreviewHelper {
    val default = DatabaseBow(
            id = DEFAULT_BOW_ID,
            name = "Default",
            isSightMarkDiagramHighestAtTop = false,
    )
}
