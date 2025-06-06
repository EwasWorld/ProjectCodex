package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual

sealed class ArrowInputsError(@StringRes val messageId: Int) {
    object NoArrowsCannotBackSpace : ArrowInputsError(R.string.err_input_end__end_empty)
    object EndFullCannotAddMore : ArrowInputsError(R.string.err_input_end__end_full)
    object NotEnoughArrowsInputted : ArrowInputsError(R.string.err_input_end__end_not_full)
    object TooManyArrowsInputted : ArrowInputsError(R.string.err_input_end__end_overfull)

    val message: ResOrActual<String>
        get() = ResOrActual.StringResource(messageId)
}
