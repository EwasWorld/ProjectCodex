package eywa.projectcodex.components.archerRoundScore

import androidx.annotation.StringRes
import eywa.projectcodex.R

sealed class ArcherRoundError(@StringRes val messageId: Int) {
    object NoArrowsCannotBackSpace : ArcherRoundError(R.string.err_input_end__end_empty)
    object EndFullCannotAddMore : ArcherRoundError(R.string.err_input_end__end_full)
    object NotEnoughArrowsInputted : ArcherRoundError(R.string.err_input_end__end_not_full)
    object TooManyArrowsInputted : ArcherRoundError(R.string.err_input_end__end_overfull)
}
