package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.components.archerRoundScore.ArcherRoundError.EndFullCannotAddMore
import eywa.projectcodex.components.archerRoundScore.ArcherRoundError.NoArrowsCannotBackSpace
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.*
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.testUtils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ArcherRoundViewModelUnitTest {
    // TODO_CURRENT Check clear on change screens

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()

    private val helpShowcase: HelpShowcaseUseCase = mock { }
    private val datastore = MockDatastore()

    private fun getSut(
            startingScreen: ArcherRoundScreen = ArcherRoundScreen.INPUT_END,
            datastoreUse2023System: Boolean = true,
            archerRoundId: Int? = 1,
    ): ArcherRoundViewModel {
        datastore.values = mapOf(DatastoreKey.Use2023HandicapSystem to datastoreUse2023System)
        val savedState = MockSavedStateHandle().apply {
            values["screen"] = startingScreen.toString()
            archerRoundId?.let { values["archerRoundId"] = it }
        }.mock
        return ArcherRoundViewModel(db.mock, helpShowcase, datastore.mock, savedState)
    }

    /*
     * Init
     */

    @Test
    fun testInitialisation_RoundNotFound() = runTest {
        val sut = getSut()

        assertEquals(
                ArcherRoundState.Loading(ArcherRoundScreen.INPUT_END),
                sut.state.value,
        )

        advanceUntilIdle()
        assertEquals(
                ArcherRoundState.InvalidArcherRoundError(),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_NoRoundSet() = runTest {
        val sut = getSut(archerRoundId = null)

        assertEquals(
                ArcherRoundState.InvalidArcherRoundError(),
                sut.state.value,
        )
        advanceUntilIdle()
        assertEquals(
                ArcherRoundState.InvalidArcherRoundError(),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_ArcherRoundsAnd2023System() = runTest {
        fun create(arrowCount: Int) =
                DatabaseFullArcherRoundInfo(
                        archerRound = ArcherRound(1, Calendar.getInstance(), 1),
                        arrows = List(arrowCount) { ArrowValue(1, it + 1, 7, false) },
                )

        val archerRoundInitial = create(0)
        val archerRoundSecond = create(12)
        db.archerRoundDao.fullArcherRounds = listOf(archerRoundInitial)
        db.archerRoundDao.secondFullArcherRounds = listOf(archerRoundSecond)
        datastore.valuesDelayed = mapOf(DatastoreKey.Use2023HandicapSystem to false)
        val sut = getSut()

        assertEquals(
                ArcherRoundState.Loading(ArcherRoundScreen.INPUT_END),
                sut.state.value,
        )
        advanceTimeBy(1)
        assertEquals(
                ArcherRoundState.Loaded(
                        currentScreen = ArcherRoundScreen.INPUT_END,
                        fullArcherRoundInfo = FullArcherRoundInfo(archerRoundInitial, true),
                ),
                sut.state.value,
        )

        advanceUntilIdle()
        assertEquals(
                ArcherRoundState.Loaded(
                        currentScreen = ArcherRoundScreen.INPUT_END,
                        fullArcherRoundInfo = FullArcherRoundInfo(archerRoundSecond, false),
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_RoundCompleted() = runTest {
        val archerRoundInitial = createArcherRound_WithRound(0)
        val archerRoundSecond = createArcherRound_WithRound()
        db.archerRoundDao.fullArcherRounds = listOf(archerRoundInitial)
        db.archerRoundDao.secondFullArcherRounds = listOf(archerRoundSecond)
        val sut = getSut()

        advanceTimeBy(1)
        assertEquals(
                ArcherRoundState.Loaded(
                        currentScreen = ArcherRoundScreen.INPUT_END,
                        fullArcherRoundInfo = FullArcherRoundInfo(archerRoundInitial, true),
                ),
                sut.state.value,
        )

        advanceUntilIdle()
        assertEquals(
                ArcherRoundState.Loaded(
                        currentScreen = ArcherRoundScreen.INPUT_END,
                        fullArcherRoundInfo = FullArcherRoundInfo(archerRoundSecond, true),
                        displayRoundCompletedDialog = true,
                ),
                sut.state.value,
        )
    }

    /*
     * ArrowInputsIntent
     */

    @Test
    fun testArrowInputsIntent_subScreenInputArrows() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut(ArcherRoundScreen.SCORE_PAD)
        advanceUntilIdle()
        sut.handle(ScorePadIntent.RowClicked(1))
        sut.handle(ScorePadIntent.EditEndClicked)

        var state = sut.getLoadedStateValue()
        val roundArrows = state.subScreenInputArrows
        assertEquals(TestData.ARROWS.take(6), roundArrows)

        // No change
        sut.handle(ArrowInputsIntent.ResetArrowsInputted)
        assertEquals(state, sut.getLoadedStateValue())

        // Cleared
        sut.handle(ArrowInputsIntent.ClearArrowsInputted)
        assertEquals(state.copy(subScreenInputArrows = emptyList()), sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.ResetArrowsInputted)
        assertEquals(state, sut.getLoadedStateValue())

        // Few changed
        val arrows = List(6) { if (it < 3) TestData.ARROWS[it] else Arrow(7, false) }
        repeat(3) { sut.handle(ArrowInputsIntent.BackspaceArrowsInputted) }
        repeat(3) { sut.handle(ArrowInputsIntent.ArrowInputted(Arrow(7, false))) }
        assertEquals(state.copy(subScreenInputArrows = arrows), sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.ResetArrowsInputted)
        assertEquals(state, sut.getLoadedStateValue())

        // Check backspace
        repeat(6) { sut.handle(ArrowInputsIntent.BackspaceArrowsInputted) }
        state = state.copy(subScreenInputArrows = emptyList())
        assertEquals(state, sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.BackspaceArrowsInputted)
        assertEquals(state.copy(errors = setOf(NoArrowsCannotBackSpace)), sut.getLoadedStateValue())
    }

    @Test
    fun testArrowInputsIntent_ArrowInputted() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        var state = sut.getLoadedStateValue()

        val arrows = TestData.ARROWS.take(6)
        arrows.forEachIndexed { index, arrow ->
            sut.handle(ArrowInputsIntent.ArrowInputted(arrow))
            assertEquals(state.copy(inputArrows = arrows.take(index + 1)), sut.getLoadedStateValue())
        }
        state = sut.getLoadedStateValue()

        // Too many arrows
        sut.handle(ArrowInputsIntent.ArrowInputted(TestData.ARROWS.last()))
        assertEquals(state.copy(errors = setOf(EndFullCannotAddMore)), sut.getLoadedStateValue())
    }

    @Test
    fun testArrowInputsIntent_ClearArrowsInputted() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()

        // No arrows
        sut.handle(ArrowInputsIntent.ClearArrowsInputted)
        assertEquals(state, sut.getLoadedStateValue())

        // Full arrows
        val arrows = TestData.ARROWS.take(6)
        arrows.forEach { arrow -> sut.handle(ArrowInputsIntent.ArrowInputted(arrow)) }
        assertEquals(state.copy(inputArrows = arrows), sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.ClearArrowsInputted)
        assertEquals(state, sut.getLoadedStateValue())

        // One arrow
        sut.handle(ArrowInputsIntent.ArrowInputted(arrows.first()))
        assertEquals(state.copy(inputArrows = arrows.take(1)), sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.ClearArrowsInputted)
        assertEquals(state, sut.getLoadedStateValue())
    }

    @Test
    fun testArrowInputsIntent_BackspaceArrowsInputted() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()

        // Full arrows
        val arrows = TestData.ARROWS.take(6)
        arrows.forEach { arrow -> sut.handle(ArrowInputsIntent.ArrowInputted(arrow)) }
        assertEquals(state.copy(inputArrows = arrows), sut.getLoadedStateValue())

        repeat(arrows.size) {
            sut.handle(ArrowInputsIntent.BackspaceArrowsInputted)
            assertEquals(state.copy(inputArrows = arrows.take(arrows.size - it - 1)), sut.getLoadedStateValue())
        }
        assertEquals(state, sut.getLoadedStateValue())

        // No arrows
        sut.handle(ArrowInputsIntent.BackspaceArrowsInputted)
        assertEquals(state.copy(errors = setOf(NoArrowsCannotBackSpace)), sut.getLoadedStateValue())
    }

    @Test
    fun testArrowInputsIntent_SubmitClicked_Input() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()
        assertEquals(ArcherRoundScreen.INPUT_END, state.currentScreen)

        repeat(6) { sut.handle(ArrowInputsIntent.ArrowInputted(Arrow(10, true))) }
        assertEquals(state.copy(inputArrows = List(6) { Arrow(10, true) }), sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.SubmitClicked)
        assertEquals(state.copy(inputArrows = emptyList()), sut.getLoadedStateValue())
        advanceUntilIdle()

        verify(db.arrowValueDao).insert(*List(6) { ArrowValue(1, it + 13, 10, true) }.toTypedArray())
    }

    @Test
    fun testArrowInputsIntent_SubmitClicked_Insert() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut(startingScreen = ArcherRoundScreen.SCORE_PAD)
        advanceUntilIdle()

        sut.handle(ScorePadIntent.RowLongClicked(1))
        sut.handle(ScorePadIntent.InsertEndClicked)
        val state = sut.getLoadedStateValue()
        assertEquals(1, state.scorePadSelectedEnd)
        assertEquals(ArcherRoundScreen.INSERT_END, state.currentScreen)

        repeat(6) { sut.handle(ArrowInputsIntent.ArrowInputted(Arrow(10, true))) }
        assertEquals(state.copy(subScreenInputArrows = List(6) { Arrow(10, true) }), sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.SubmitClicked)
        assertEquals(
                state.copy(
                        currentScreen = ArcherRoundScreen.SCORE_PAD,
                        scorePadSelectedEnd = null,
                        subScreenInputArrows = emptyList(),
                ),
                sut.getLoadedStateValue(),
        )
        advanceUntilIdle()

        verify(db.arrowValueDao).updateAndInsert(
                update = List(6) { ArrowValue(1, it + 1, 10, true) }
                        .plus(TestData.ARROWS.take(6).mapIndexed { index, arrow -> arrow.toArrowValue(1, index + 7) }),
                insert = TestData.ARROWS.takeLast(6).mapIndexed { index, arrow -> arrow.toArrowValue(1, index + 13) },
        )
    }

    @Test
    fun testArrowInputsIntent_SubmitClicked_Edit() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut(startingScreen = ArcherRoundScreen.SCORE_PAD)
        advanceUntilIdle()

        sut.handle(ScorePadIntent.RowLongClicked(1))
        sut.handle(ScorePadIntent.EditEndClicked)
        val state = sut.getLoadedStateValue()
        assertEquals(1, state.scorePadSelectedEnd)
        assertEquals(ArcherRoundScreen.EDIT_END, state.currentScreen)

        sut.handle(ArrowInputsIntent.ClearArrowsInputted)
        repeat(6) { sut.handle(ArrowInputsIntent.ArrowInputted(Arrow(10, true))) }
        assertEquals(state.copy(subScreenInputArrows = List(6) { Arrow(10, true) }), sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.SubmitClicked)
        assertEquals(
                state.copy(
                        currentScreen = ArcherRoundScreen.SCORE_PAD,
                        scorePadSelectedEnd = null,
                        subScreenInputArrows = emptyList(),
                ),
                sut.getLoadedStateValue(),
        )
        advanceUntilIdle()

        verify(db.arrowValueDao).update(*List(6) { ArrowValue(1, it + 1, 10, true) }.toTypedArray())
    }

    @Test
    fun testArrowInputsIntent_CancelClicked() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut(ArcherRoundScreen.SCORE_PAD)
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()

        // Edit
        sut.handle(ScorePadIntent.RowClicked(1))
        sut.handle(ScorePadIntent.EditEndClicked)
        assertEquals(
                state.copy(
                        currentScreen = ArcherRoundScreen.EDIT_END,
                        scorePadSelectedEnd = 1,
                        subScreenInputArrows = TestData.ARROWS.take(6),
                ),
                sut.getLoadedStateValue(),
        )

        sut.handle(ArrowInputsIntent.CancelClicked)
        assertEquals(state, sut.getLoadedStateValue())

        // Insert
        sut.handle(ScorePadIntent.RowClicked(1))
        sut.handle(ScorePadIntent.InsertEndClicked)
        assertEquals(
                state.copy(
                        currentScreen = ArcherRoundScreen.INSERT_END,
                        scorePadSelectedEnd = 1,
                ),
                sut.getLoadedStateValue(),
        )

        sut.handle(ArrowInputsIntent.CancelClicked)
        assertEquals(state, sut.getLoadedStateValue())

        // Score
        sut.handle(ScorePadIntent.RowClicked(1))
        assertEquals(state.copy(scorePadSelectedEnd = 1), sut.getLoadedStateValue())

        sut.handle(ArrowInputsIntent.CancelClicked)
        assertEquals(state.copy(scorePadSelectedEnd = 1), sut.getLoadedStateValue())
    }

    @Test
    fun testArrowInputsIntent_HelpShowcaseAction() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()

        val intent = HelpShowcaseIntent.Clear
        sut.handle(ArrowInputsIntent.HelpShowcaseAction(intent))
        assertEquals(state, sut.getLoadedStateValue())
        verify(helpShowcase).handle(intent, ArcherRoundFragment::class)
    }

    /*
     * ScorePadIntent
     */

    @Test
    fun testScorePadIntent_RowLongClickedAndClosed() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()
        assertEquals(null, state.scorePadSelectedEnd)

        sut.handle(ScorePadIntent.RowLongClicked(1))
        assertEquals(state.copy(scorePadSelectedEnd = 1), sut.getLoadedStateValue())

        // Close
        sut.handle(ScorePadIntent.CloseDropdownMenu)
        assertEquals(state, sut.getLoadedStateValue())
    }

    @Test
    fun testScorePadIntent_RowClickedAndClosed() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()
        assertEquals(null, state.scorePadSelectedEnd)

        sut.handle(ScorePadIntent.RowClicked(1))
        assertEquals(state.copy(scorePadSelectedEnd = 1), sut.getLoadedStateValue())

        // Close
        sut.handle(ScorePadIntent.CloseDropdownMenu)
        assertEquals(state, sut.getLoadedStateValue())
    }

    @Test
    fun testScorePadIntent_EditEndClicked() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()

        sut.handle(ScorePadIntent.RowLongClicked(1))
        val state = sut.getLoadedStateValue()
        assertEquals(1, state.scorePadSelectedEnd)

        sut.handle(ScorePadIntent.EditEndClicked)
        assertEquals(
                state.copy(
                        currentScreen = ArcherRoundScreen.EDIT_END,
                        subScreenInputArrows = TestData.ARROWS.take(6),
                ), sut.getLoadedStateValue()
        )
    }

    @Test
    fun testScorePadIntent_InsertEndClicked() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()

        sut.handle(ScorePadIntent.RowLongClicked(1))
        val state = sut.getLoadedStateValue()
        assertEquals(1, state.scorePadSelectedEnd)

        sut.handle(ScorePadIntent.InsertEndClicked)
        assertEquals(
                state.copy(
                        currentScreen = ArcherRoundScreen.INSERT_END,
                        subScreenInputArrows = emptyList(),
                ), sut.getLoadedStateValue()
        )
    }

    @Test
    fun testScorePadIntent_DeleteEndClicked() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()

        sut.handle(ScorePadIntent.RowLongClicked(1))
        val state = sut.getLoadedStateValue()
        assertEquals(1, state.scorePadSelectedEnd)

        // Delete
        sut.handle(ScorePadIntent.DeleteEndClicked)
        assertEquals(state.copy(displayDeleteEndConfirmationDialog = true), sut.getLoadedStateValue())

        // Cancel
        sut.handle(ScorePadIntent.DeleteEndDialogCancelClicked)
        assertEquals(
                state.copy(
                        displayDeleteEndConfirmationDialog = false,
                        scorePadSelectedEnd = null,
                ), sut.getLoadedStateValue()
        )

        // Delete
        sut.handle(ScorePadIntent.RowLongClicked(1))
        assertEquals(state, sut.getLoadedStateValue())

        sut.handle(ScorePadIntent.DeleteEndClicked)
        assertEquals(state.copy(displayDeleteEndConfirmationDialog = true), sut.getLoadedStateValue())

        // Confirm
        sut.handle(ScorePadIntent.DeleteEndDialogOkClicked)
        assertEquals(
                state.copy(
                        displayDeleteEndConfirmationDialog = false,
                        scorePadSelectedEnd = null,
                ), sut.getLoadedStateValue()
        )
        advanceUntilIdle()
        verify(db.arrowValueDao).update(
                *TestData.ARROWS
                        .takeLast(6)
                        .mapIndexed { index, arrow -> arrow.toArrowValue(1, index + 1) }
                        .toTypedArray()
        )
        verify(db.arrowValueDao).deleteArrows(1, (7..12).toList())
    }

    @Test
    fun testScorePadIntent_HelpShowcaseAction() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()

        val intent = HelpShowcaseIntent.Clear
        sut.handle(ScorePadIntent.HelpShowcaseAction(intent))
        assertEquals(state, sut.getLoadedStateValue())
        verify(helpShowcase).handle(intent, ArcherRoundFragment::class)
    }

    /*
     * SettingsIntent
     */

    @Test
    fun testSettingsIntent_InputEndSizeChanged() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut(startingScreen = ArcherRoundScreen.SETTINGS)
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()
        assertEquals(6, state.inputEndSizePartial)

        sut.handle(SettingsIntent.InputEndSizeChanged(12))
        assertEquals(state.copy(inputEndSizePartial = 12), sut.getLoadedStateValue())
    }

    @Test
    fun testSettingsIntent_ScorePadEndSizeChanged() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut(startingScreen = ArcherRoundScreen.SETTINGS)
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()
        assertEquals(6, state.scorePadEndSizePartial)

        sut.handle(SettingsIntent.ScorePadEndSizeChanged(12))
        assertEquals(state.copy(scorePadEndSizePartial = 12), sut.getLoadedStateValue())
    }

    @Test
    fun testSettingsIntent_HelpShowcaseAction() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()

        val intent = HelpShowcaseIntent.Clear
        sut.handle(SettingsIntent.HelpShowcaseAction(intent))
        assertEquals(state, sut.getLoadedStateValue())
        verify(helpShowcase).handle(intent, ArcherRoundFragment::class)
    }

    /*
     * Other
     */

    @Test
    fun testNavBarClicked() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut(ArcherRoundScreen.SCORE_PAD)
        advanceUntilIdle()
        var state = sut.getLoadedStateValue()
        assertEquals(ArcherRoundScreen.SCORE_PAD, state.currentScreen)

        ArcherRoundScreen.values().forEach {
            try {
                sut.handle(NavBarClicked(it))

                if (it.isMainScreen) {
                    val newState = state.copy(currentScreen = it)
                    assertEquals(newState, sut.getLoadedStateValue())
                    state = newState
                }
                else {
                    fail("Don't navigate to sub-screen")
                }
            }
            catch (e: IllegalArgumentException) {
                if (it.isMainScreen) {
                    fail("Should navigate")
                }
                else {
                    assertEquals(state, sut.getLoadedStateValue())
                }
            }
        }
    }

    @Test
    fun testInvalidArcherRoundIntent() = runTest {
        fun ArcherRoundViewModel.getInvalidStateValue() = state.value as ArcherRoundState.InvalidArcherRoundError

        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getInvalidStateValue()

        sut.handle(InvalidArcherRoundIntent.ReturnToMenuClicked)
        assertEquals(state.copy(mainMenuClicked = true), sut.getInvalidStateValue())

        sut.handle(InvalidArcherRoundIntent.ReturnToMenuHandled)
        assertEquals(state.copy(mainMenuClicked = false), sut.getInvalidStateValue())
    }

    @Test
    fun testCannotInputEndDialog() = runTest {
        db.archerRoundDao.fullArcherRounds = listOf(createArcherRound_WithRound())
        val sut = getSut(startingScreen = ArcherRoundScreen.SCORE_PAD)
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()
        assertEquals(ArcherRoundScreen.SCORE_PAD, state.currentScreen)
        assert(!state.displayCannotInputEndDialog)

        sut.handle(NavBarClicked(ArcherRoundScreen.INPUT_END))
        assertEquals(state.copy(displayCannotInputEndDialog = true), sut.getLoadedStateValue())

        sut.handle(CannotInputEndDialogOkClicked)
        assertEquals(state, sut.getLoadedStateValue())
    }

    @Test
    fun testRoundCompleteDialogOkClicked() = runTest {
        db.archerRoundDao.fullArcherRounds = listOf(createArcherRound_WithRound())
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()
        assertEquals(ArcherRoundScreen.INPUT_END, state.currentScreen)
        assert(state.displayRoundCompletedDialog)

        sut.handle(RoundCompleteDialogOkClicked)
        assertEquals(
                state.copy(currentScreen = ArcherRoundScreen.STATS, displayRoundCompletedDialog = false),
                sut.getLoadedStateValue(),
        )
    }

    @Test
    fun testNoArrowsDialogOkClicked() = runTest {
        db.archerRoundDao.fullArcherRounds = listOf(
                DatabaseFullArcherRoundInfo(
                        archerRound = ArcherRound(1, Calendar.getInstance(), 1),
                        arrows = emptyList(),
                )
        )
        val sut = getSut(startingScreen = ArcherRoundScreen.SCORE_PAD)
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()
        assertEquals(ArcherRoundScreen.SCORE_PAD, state.currentScreen)

        sut.handle(ScorePadIntent.NoArrowsDialogOkClicked)
        assertEquals(state.copy(currentScreen = ArcherRoundScreen.INPUT_END), sut.getLoadedStateValue())

    }

    @Test
    fun testErrorHandled() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()

        // EndFullCannotAddMore
        TestData.ARROWS.take(7).forEach { sut.handle(ArrowInputsIntent.ArrowInputted(it)) }
        assertEquals(
                state.copy(
                        inputArrows = TestData.ARROWS.take(6),
                        errors = setOf(EndFullCannotAddMore),
                ),
                sut.getLoadedStateValue(),
        )

        // NoArrowsCannotBackSpace
        sut.handle(ArrowInputsIntent.ClearArrowsInputted)
        sut.handle(ArrowInputsIntent.BackspaceArrowsInputted)
        assertEquals(
                state.copy(errors = setOf(EndFullCannotAddMore, NoArrowsCannotBackSpace)),
                sut.getLoadedStateValue(),
        )

        sut.handle(ErrorHandled(EndFullCannotAddMore))
        assertEquals(state.copy(errors = setOf(NoArrowsCannotBackSpace)), sut.getLoadedStateValue())

        sut.handle(ErrorHandled(NoArrowsCannotBackSpace))
        assertEquals(state, sut.getLoadedStateValue())
    }

    @Test
    fun testHelpShowcaseAction() = runTest {
        setupSimpleDbData_NoRound()
        val sut = getSut()
        advanceUntilIdle()
        val state = sut.getLoadedStateValue()

        val intent = HelpShowcaseIntent.Clear
        sut.handle(HelpShowcaseAction(intent))
        assertEquals(state, sut.getLoadedStateValue())
        verify(helpShowcase).handle(intent, ArcherRoundFragment::class)
    }

    /*
     * Helpers
     */

    private fun createArcherRound_WithRound(arrowCount: Int = 36) =
            DatabaseFullArcherRoundInfo(
                    archerRound = ArcherRound(1, Calendar.getInstance(), 1, roundId = 1),
                    arrows = List(arrowCount) { ArrowValue(1, it + 1, 7, false) },
                    round = Round(1, "", "", true, true),
                    roundArrowCounts = listOf(RoundArrowCount(1, 1, 122f, 36)),
                    allRoundSubTypes = listOf(),
                    allRoundDistances = listOf(RoundDistance(1, 1, 1, 50)),
            )

    private fun setupSimpleDbData_NoRound() {
        db.archerRoundDao.fullArcherRounds = listOf(
                DatabaseFullArcherRoundInfo(
                        archerRound = ArcherRound(1, Calendar.getInstance(), 1),
                        arrows = TestData.ARROWS.mapIndexed { index, arrow -> arrow.toArrowValue(1, index + 1) },
                )
        )
    }

    private fun ArcherRoundViewModel.getLoadedStateValue() = state.value as ArcherRoundState.Loaded
}
