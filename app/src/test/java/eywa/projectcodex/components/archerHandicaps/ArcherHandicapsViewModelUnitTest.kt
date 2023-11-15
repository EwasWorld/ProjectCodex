package eywa.projectcodex.components.archerHandicaps

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.*
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ArcherHandicapsViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val helpShowcase = mock<HelpShowcaseUseCase> { }
    private val defaultHandicaps = ArcherHandicapsPreviewHelper.handicaps
    private val initialState = ArcherHandicapsState(
            currentHandicaps = defaultHandicaps.take(1),
            allHandicaps = defaultHandicaps,
    )

    private fun getSut(
            handicaps: List<DatabaseArcherHandicap> = defaultHandicaps,
    ): ArcherHandicapsViewModel {
        db.archerRepo.handicaps = handicaps
        return ArcherHandicapsViewModel(db.mock, helpShowcase)
    }

    @Test
    fun testInitialise() = runTest {
        val sut = getSut()
        assertEquals(
                ArcherHandicapsState(),
                sut.state.value,
        )
        advanceUntilIdle()
        assertEquals(
                initialState,
                sut.state.value,
        )
    }

    @Test
    fun testRowClicked() = runTest {
        val sut = getSut()

        // No handicaps added
        sut.handle(RowClicked(defaultHandicaps.first()))
        assertEquals(
                ArcherHandicapsState(),
                sut.state.value,
        )
        advanceUntilIdle()

        // Open/close row
        sut.handle(RowClicked(defaultHandicaps.first()))
        assertEquals(
                initialState.copy(lastClickedId = defaultHandicaps.first().archerHandicapId),
                sut.state.value,
        )
        sut.handle(RowClicked(defaultHandicaps.first()))
        assertEquals(
                initialState,
                sut.state.value,
        )

        // Open row then open another row
        sut.handle(RowClicked(defaultHandicaps.first()))
        assertEquals(
                initialState.copy(lastClickedId = defaultHandicaps.first().archerHandicapId),
                sut.state.value,
        )
        sut.handle(RowClicked(defaultHandicaps[1]))
        assertEquals(
                initialState.copy(lastClickedId = defaultHandicaps[1].archerHandicapId),
                sut.state.value,
        )
        sut.handle(AddClicked)
        assertEquals(
                initialState.copy(openAddDialog = true),
                sut.state.value,
        )
    }
}
