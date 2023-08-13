package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundError
import eywa.projectcodex.components.archerRoundScore.ArcherRoundError.*
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.model.Arrow

sealed class ArrowInputsIntent {
    data class ArrowInputted(val arrow: Arrow) : ArrowInputsIntent()
    object ResetArrowsInputted : ArrowInputsIntent()
    object ClearArrowsInputted : ArrowInputsIntent()
    object BackspaceArrowsInputted : ArrowInputsIntent()

    object SubmitClicked : ArrowInputsIntent()
    object CancelClicked : ArrowInputsIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ArrowInputsIntent()

    fun handle(
            enteredArrows: List<Arrow>,
            endSize: Int,
            dbArrows: List<DatabaseArrowScore>?,
            setEnteredArrows: (List<Arrow>, ArcherRoundError?) -> Unit,
            onCancel: () -> Unit = { throw NotImplementedError() },
            onSubmit: () -> Unit,
            helpListener: (HelpShowcaseIntent) -> Unit,
    ) {
        when (this) {
            is ArrowInputted -> {
                if (enteredArrows.size == endSize) setEnteredArrows(enteredArrows, EndFullCannotAddMore)
                else setEnteredArrows(enteredArrows.plus(arrow), null)
            }
            BackspaceArrowsInputted -> {
                if (enteredArrows.isEmpty()) setEnteredArrows(enteredArrows, NoArrowsCannotBackSpace)
                else setEnteredArrows(enteredArrows.dropLast(1), null)
            }
            ClearArrowsInputted -> setEnteredArrows(emptyList(), null)
            CancelClicked -> onCancel()
            is HelpShowcaseAction -> helpListener(action)
            ResetArrowsInputted -> setEnteredArrows(dbArrows!!.map { Arrow(it.score, it.isX) }, null)
            SubmitClicked -> {
                if (enteredArrows.size < endSize) setEnteredArrows(enteredArrows, NotEnoughArrowsInputted)
                else if (enteredArrows.size > endSize) setEnteredArrows(enteredArrows, TooManyArrowsInputted)
                else onSubmit()
            }
        }
    }
}
