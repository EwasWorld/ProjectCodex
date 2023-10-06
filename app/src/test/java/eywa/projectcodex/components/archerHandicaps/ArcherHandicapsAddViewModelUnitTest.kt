package eywa.projectcodex.components.archerHandicaps

import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.*
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddIntent
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddState
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddViewModel
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class ArcherHandicapsAddViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val initialState = ArcherHandicapsAddState()

    private fun getSut(): ArcherHandicapsAddViewModel {
        return ArcherHandicapsAddViewModel(db.mock)
    }

    private fun checkState(
            expected: ArcherHandicapsAddState,
            actual: ArcherHandicapsAddState,
    ) {
        assertEquals(
                expected.copy(date = actual.date),
                actual,
        )
        assertEquals(
                expected.date.timeInMillis.toFloat(),
                actual.date.timeInMillis.toFloat(),
                1000 * 60 * 5f,
        )
    }

    @Test
    fun testSubmit() = runTest {
        val sut = getSut()
        checkState(
                ArcherHandicapsAddState(),
                sut.state.value,
        )
        advanceUntilIdle()
        checkState(
                initialState,
                sut.state.value,
        )

        // Dirty the field to show the required flag
        sut.handle(ArcherHandicapsAddIntent.SubmitPressed)
        advanceUntilIdle()
        checkState(
                initialState.copy(handicap = initialState.handicap.markDirty()),
                sut.state.value,
        )

        // Invalid
        sut.handle(ArcherHandicapsAddIntent.HandicapTextUpdated("-1"))
        sut.handle(ArcherHandicapsAddIntent.SubmitPressed)
        advanceUntilIdle()
        checkState(
                initialState.copy(handicap = initialState.handicap.onTextChanged("-1")),
                sut.state.value,
        )

        sut.handle(ArcherHandicapsAddIntent.HandicapTextUpdated("20"))
        sut.handle(ArcherHandicapsAddIntent.SubmitPressed)
        advanceUntilIdle()
        checkState(
                initialState.copy(handicap = initialState.handicap.onTextChanged("20"), shouldCloseDialog = true),
                sut.state.value,
        )

        verifyBlocking(db.archerRepo.mock) { insert(any<DatabaseArcherHandicap>()) }
    }
}
