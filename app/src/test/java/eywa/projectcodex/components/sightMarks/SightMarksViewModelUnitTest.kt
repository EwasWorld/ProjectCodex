package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class SightMarksViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val helpShowcase: HelpShowcaseUseCase = mock { }

    private val data = ScoresRoomDatabase.fakeSightMarkData.map { SightMark(it) }

    private fun getSut(): SightMarksViewModel {
        db.sightMarksDao.sightMarks = data
        return SightMarksViewModel(db.mock, helpShowcase)
    }

    private fun checkLoadedState(expected: SightMarksState.Loaded, sut: SightMarksViewModel) {
        assertEquals(
                expected.copy(sightMarks = emptyList()),
                (sut.state.value as SightMarksState.Loaded).copy(sightMarks = emptyList()),
        )
        assertEquals(
                expected.sightMarks.toSet(),
                (sut.state.value as SightMarksState.Loaded).sightMarks.toSet(),
        )
    }

    @Test
    fun testInitialise() = runTest {
        val sut = getSut()
        assertEquals(
                SightMarksState.Loading(),
                sut.state.value,
        )
        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )
    }

    @Test
    fun testInitialise_HighestAtTop() = runTest {
        db.bowDao.isHighestAtTop = true
        val sut = getSut()
        assertEquals(
                SightMarksState.Loading(),
                sut.state.value,
        )
        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, isHighestNumberAtTheTop = true),
                sut,
        )
    }

    @Test
    fun testHelpShowcaseAction() {
        getSut().handle(SightMarksIntent.HelpShowcaseAction(HelpShowcaseIntent.Clear))
        verify(helpShowcase).handle(HelpShowcaseIntent.Clear, CodexNavRoute.SIGHT_MARKS::class)
    }

    @Test
    fun testCreateSightMarkClicked() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.CreateSightMarkClicked)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, createNewSightMark = true),
                sut,
        )

        sut.handle(SightMarksIntent.CreateSightMarkHandled)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )
    }

    @Test
    fun testSightMarkClicked() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.SightMarkClicked(data.first { it.id == 2 }))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, openSightMarkDetail = 2),
                sut,
        )

        sut.handle(SightMarksIntent.OpenSightMarkHandled)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )
    }

    @Test
    fun testMenuAction_FlipDiagram() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.MenuAction(SightMarksMenuIntent.FlipDiagram))
        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )
        verify(db.bowDao.mock).setHighestAtTop(DEFAULT_BOW_ID, true)
    }

    @Test
    fun testMenuAction_ArchiveAll() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.MenuAction(SightMarksMenuIntent.ArchiveAll))
        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )
        verify(db.sightMarksDao.mock).archiveAll()
    }

    @Test
    fun testMenuAction_ShiftAndScale() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.MenuAction(SightMarksMenuIntent.ShiftAndScale))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )
    }

    @Test
    fun testToggleShiftAndScale_AndFlipClicked() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.StartShiftAndScale)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.FlipClicked)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f, flipScale = true),
                sut,
        )

        sut.handle(SightMarksIntent.EndShiftAndScale)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )
    }

    @Test
    fun testShiftAndScaleIntent_Shift() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.StartShiftAndScale)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Shift(increased = true, bigger = false))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0.1f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Shift(increased = false, bigger = false))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Shift(increased = true, bigger = true))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 1f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Shift(increased = false, bigger = true))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )
    }

    @Test
    fun testShiftAndScaleIntent_Scale() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.StartShiftAndScale)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Scale(increased = true, bigger = false))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1.1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Scale(increased = false, bigger = false))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Scale(increased = true, bigger = true))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 2f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Scale(increased = false, bigger = true))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )
    }

    @Test
    fun testShiftAndScaleIntent_ShiftReset() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.StartShiftAndScale)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )

        repeat(3) {
            sut.handle(SightMarksIntent.ShiftAndScaleIntent.Shift(increased = true, bigger = true))
            sut.handle(SightMarksIntent.ShiftAndScaleIntent.Scale(increased = true, bigger = true))
        }
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 3f, scaleAmount = 4f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.ShiftReset)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 4f),
                sut,
        )
    }

    @Test
    fun testShiftAndScaleIntent_ScaleReset() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.StartShiftAndScale)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )

        repeat(3) {
            sut.handle(SightMarksIntent.ShiftAndScaleIntent.Shift(increased = true, bigger = true))
            sut.handle(SightMarksIntent.ShiftAndScaleIntent.Scale(increased = true, bigger = true))
        }
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 3f, scaleAmount = 4f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.ScaleReset)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 3f, scaleAmount = 1f),
                sut,
        )
    }

    @Test
    fun testShiftAndScaleIntent_SubmitClicked() = runTest {
        val sut = getSut()

        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )

        sut.handle(SightMarksIntent.StartShiftAndScale)
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 0f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.Shift(increased = true, bigger = true))
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data, shiftAmount = 1f, scaleAmount = 1f),
                sut,
        )

        sut.handle(SightMarksIntent.ShiftAndScaleIntent.SubmitClicked)
        advanceUntilIdle()
        checkLoadedState(
                SightMarksState.Loaded(sightMarks = data),
                sut,
        )
        verify(db.sightMarksDao.mock).update(
                *(sut.state.value as SightMarksState.Loaded).sightMarks
                        .map { it.copy(sightMark = it.sightMark + 1f).asDatabaseSightMark() }.toTypedArray()
        )
    }
}
