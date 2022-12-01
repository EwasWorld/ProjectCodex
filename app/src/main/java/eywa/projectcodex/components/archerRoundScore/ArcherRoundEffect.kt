package eywa.projectcodex.components.archerRoundScore

sealed class ArcherRoundEffect {
    sealed class Error : ArcherRoundEffect() {
        object NoArrowsCannotBackSpace : Error()
        object EndFullCannotAddMore : Error()
        object NotEnoughArrowsInputted : Error()
    }
}
