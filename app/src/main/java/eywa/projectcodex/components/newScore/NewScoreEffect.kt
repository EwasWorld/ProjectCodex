package eywa.projectcodex.components.newScore

sealed class NewScoreEffect {
    data class NavigateToInputEnd(val archerRoundId: Int) : NewScoreEffect()
    object PopBackstack : NewScoreEffect()
}