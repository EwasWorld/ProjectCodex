package eywa.projectcodex.components.viewScores.ui.convertScoreDialog

import eywa.projectcodex.components.viewScores.utils.ConvertScoreType

sealed class ConvertScoreIntent {
    object Close : ConvertScoreIntent()
    data class Ok(val convertType: ConvertScoreType) : ConvertScoreIntent()
}
