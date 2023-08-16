package eywa.projectcodex.components.shootDetails.common.arrowInputs

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.model.Arrow
import org.junit.Assert.assertEquals
import org.junit.Test

class ArrowInputsIntentUnitTest {
    @Test
    fun testArrowInputted() {
        val arrows = List(6) { Arrow(it + 1) }
        val newArrow = Arrow(7)
        ArrowInputsIntent.ArrowInputted(newArrow).checkHandle(
                expectedCalls = listOf(Call.SetArrows(listOf(newArrow), null)),
                enteredArrows = emptyList(),
        )
        ArrowInputsIntent.ArrowInputted(newArrow).checkHandle(
                expectedCalls = listOf(Call.SetArrows(arrows.take(5).plus(newArrow), null)),
                enteredArrows = arrows.take(5),
        )
        ArrowInputsIntent.ArrowInputted(newArrow).checkHandle(
                expectedCalls = listOf(Call.SetArrows(arrows, ArrowInputsError.EndFullCannotAddMore)),
                enteredArrows = arrows,
        )
    }

    @Test
    fun testResetArrowsInputted() {
        val arrows = List(6) { Arrow(it + 1) }
        ArrowInputsIntent.ResetArrowsInputted.checkHandle(
                expectedCalls = listOf(Call.SetArrows(arrows, null)),
                enteredArrows = emptyList(),
                dbArrows = arrows.mapIndexed { index, arrow -> arrow.toArrowScore(1, index) }
        )
        ArrowInputsIntent.ResetArrowsInputted.checkHandle(
                expectedCalls = listOf(Call.SetArrows(arrows, null)),
                enteredArrows = arrows,
                dbArrows = arrows.mapIndexed { index, arrow -> arrow.toArrowScore(1, index) }
        )
        ArrowInputsIntent.ResetArrowsInputted.checkHandle(
                expectedCalls = listOf(Call.SetArrows(arrows, null)),
                enteredArrows = List(6) { Arrow(9) },
                dbArrows = arrows.mapIndexed { index, arrow -> arrow.toArrowScore(1, index) }
        )
    }

    @Test
    fun testClearArrowsInputted() {
        val arrows = List(6) { Arrow(it + 1) }
        ArrowInputsIntent.ClearArrowsInputted.checkHandle(
                expectedCalls = listOf(Call.SetArrows(emptyList(), null)),
                enteredArrows = arrows,
        )
        ArrowInputsIntent.ClearArrowsInputted.checkHandle(
                expectedCalls = listOf(Call.SetArrows(emptyList(), null)),
                enteredArrows = arrows.take(1),
        )
    }

    @Test
    fun testBackspaceArrowsInputted() {
        val arrows = List(6) { Arrow(it + 1) }
        ArrowInputsIntent.BackspaceArrowsInputted.checkHandle(
                expectedCalls = listOf(Call.SetArrows(emptyList(), ArrowInputsError.NoArrowsCannotBackSpace)),
        )
        ArrowInputsIntent.BackspaceArrowsInputted.checkHandle(
                expectedCalls = listOf(Call.SetArrows(arrows.take(5), null)),
                enteredArrows = arrows,
        )
        ArrowInputsIntent.BackspaceArrowsInputted.checkHandle(
                expectedCalls = listOf(Call.SetArrows(emptyList(), null)),
                enteredArrows = arrows.take(1),
        )
    }

    @Test
    fun testSubmitClicked() {
        val arrows = List(12) { Arrow(1) }
        ArrowInputsIntent.SubmitClicked.checkHandle(
                expectedCalls = listOf(Call.SetArrows(emptyList(), ArrowInputsError.NotEnoughArrowsInputted)),
        )
        ArrowInputsIntent.SubmitClicked.checkHandle(
                expectedCalls = listOf(Call.SetArrows(arrows, ArrowInputsError.TooManyArrowsInputted)),
                enteredArrows = arrows,
                endSize = 6,
        )
        ArrowInputsIntent.SubmitClicked.checkHandle(
                expectedCalls = listOf(Call.Submit),
                enteredArrows = arrows.take(6),
                endSize = 6,
        )
    }

    @Test
    fun testCancelClicked() {
        ArrowInputsIntent.CancelClicked.checkHandle(
                expectedCalls = listOf(Call.Cancel),
        )
    }

    @Test
    fun testHelpShowcaseAction() {
        ArrowInputsIntent.HelpShowcaseAction(HelpShowcaseIntent.Clear).checkHandle(
                expectedCalls = listOf(Call.Help(HelpShowcaseIntent.Clear)),
        )
    }

    private fun ArrowInputsIntent.checkHandle(
            expectedCalls: List<Call>,
            enteredArrows: List<Arrow> = emptyList(),
            endSize: Int = 6,
            dbArrows: List<DatabaseArrowScore>? = null,
    ) {
        val calls = mutableListOf<Call>()
        handle(
                enteredArrows = enteredArrows,
                endSize = endSize,
                dbArrows = dbArrows,
                setEnteredArrows = { arrows, error -> calls.add(Call.SetArrows(arrows, error)) },
                onCancel = { calls.add(Call.Cancel) },
                onSubmit = { calls.add(Call.Submit) },
                helpListener = { calls.add(Call.Help(it)) },
        )
        assertEquals(expectedCalls, calls.toList())
    }

    sealed class Call {
        object Cancel : Call()
        object Submit : Call()
        data class Help(val intent: HelpShowcaseIntent) : Call()
        data class SetArrows(val arrows: List<Arrow>, val error: ArrowInputsError?) : Call()
    }
}
