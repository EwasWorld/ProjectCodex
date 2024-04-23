package eywa.projectcodex.components

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.*
import eywa.projectcodex.components.handicapTables.HandicapTablesState
import eywa.projectcodex.components.handicapTables.HandicapTablesViewModel
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import eywa.projectcodex.testUtils.mockUpdateDefaultRoundsTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class HandicapTablesViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val datastore = MockDatastore()
    private val helpShowcase = mock<HelpShowcaseUseCase> { }

    private val initialRounds = listOf(
            RoundPreviewHelper.indoorMetricRoundData,
            RoundPreviewHelper.outdoorImperialRoundData,
            RoundPreviewHelper.singleSubtypeRoundData,
            RoundPreviewHelper.yorkRoundData,
            RoundPreviewHelper.wa25RoundData,
    )

    private fun getSut(): HandicapTablesViewModel {
        db.rounds.fullRoundsInfo = initialRounds
        return HandicapTablesViewModel(
                db = db.mock,
                helpShowcase = helpShowcase,
                datastore = datastore.mock,
                updateDefaultRoundsTask = mockUpdateDefaultRoundsTask,
        )
    }

    private fun TestScope.setUpGetHandicapsTest(handicap: Int): HandicapTablesViewModel {
        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                HandicapTablesState(
                        selectRoundDialogState = SelectRoundDialogState(allRounds = initialRounds),
                        updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
                ),
                sut.state.value,
        )

        val round = RoundPreviewHelper.yorkRoundData
        sut.handle(InputChanged(handicap.toString()))
        sut.handle(SelectRoundDialogAction(SelectRoundDialogIntent.RoundIntent.OpenRoundDialog))
        sut.handle(SelectRoundDialogAction(SelectRoundDialogIntent.RoundIntent.RoundSelected(round.round)))

        assertEquals(
                HandicapTablesState(
                        selectRoundDialogState = SelectRoundDialogState(
                                allRounds = initialRounds,
                                selectedRoundId = round.round.roundId,
                                selectedSubTypeId = 1,
                        ),
                        input = PartialNumberFieldState().onTextChanged(handicap.toString()),
                        updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
                ),
                sut.state.value.copy(handicaps = emptyList(), highlightedHandicap = null),
        )

        return sut
    }

    @Test
    fun testGetHandicaps_Standard() = runTest {
        val sut = setUpGetHandicapsTest(50)

        assertEquals(
                (45..55).toList(),
                sut.state.value.handicaps.map { it.handicap },
        )
        assertEquals(
                50,
                sut.state.value.highlightedHandicap!!.handicap,
        )
    }

    @Test
    fun testGetHandicaps_Best() = runTest {
        val sut = setUpGetHandicapsTest(2)

        assertEquals(
                (0..7).toList(),
                sut.state.value.handicaps.map { it.handicap },
        )
        assertEquals(
                2,
                sut.state.value.highlightedHandicap!!.handicap,
        )
    }

    @Test
    fun testGetHandicaps_Worst() = runTest {
        val sut = setUpGetHandicapsTest(148)

        assertEquals(
                listOf(131, 134, 137, 141, 147, 150),
                sut.state.value.handicaps.map { it.handicap },
        )
        assertEquals(
                150,
                sut.state.value.highlightedHandicap!!.handicap,
        )
    }

    @Test
    fun extraTest() = runTest {
        val sut = setUpGetHandicapsTest(120)

        sut.handle(SelectRoundDialogAction(SelectRoundDialogIntent.RoundIntent.OpenRoundDialog))
        sut.handle(SelectRoundDialogAction(SelectRoundDialogIntent.RoundIntent.RoundSelected(RoundPreviewHelper.wa25RoundData.round)))
        sut.handle(SelectFaceDialogAction(SelectRoundFaceDialogIntent.Open))
        sut.handle(SelectFaceDialogAction(SelectRoundFaceDialogIntent.SingleFaceClicked(RoundFace.TRIPLE)))

        assertEquals(
                listOf(RoundFace.TRIPLE),
                sut.state.value.selectFaceDialogState.selectedFaces,
        )
        assertEquals(
                RoundPreviewHelper.wa25RoundData.round.roundId,
                sut.state.value.selectRoundDialogState.selectedRoundId,
        )
        assertEquals(
                listOf(114, 115, 116, 117, 119, 120, 122, 124, 126, 129, 132),
                sut.state.value.handicaps.map { it.handicap },
        )
    }
}
