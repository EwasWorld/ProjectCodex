package eywa.projectcodex.components.archerRoundScore

sealed class ArcherRoundEffect {
    object NavigateUp : ArcherRoundEffect()

    sealed class Error {
        object NoArrowsCannotBackSpace : ArcherRoundEffect()
        object EndFullCannotAddMore : ArcherRoundEffect()
    }
}
