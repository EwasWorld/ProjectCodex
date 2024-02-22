package eywa.projectcodex.components.viewScores.dialogs.convertScoreDialog

sealed class ConvertScoreIntent {
    object Close : ConvertScoreIntent()
    data class Ok(val convertType: ConvertScoreType) : ConvertScoreIntent()
}
