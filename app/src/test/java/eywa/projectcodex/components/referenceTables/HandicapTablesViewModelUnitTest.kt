package eywa.projectcodex.components.referenceTables

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.asDatabaseFullShootInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SubTypeIntent.CloseSubTypeDialog
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SubTypeIntent.OpenSubTypeDialog
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesIntent.*
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesState
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesViewModel
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockSavedStateHandle
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
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class HandicapTablesViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val datastore = MockDatastore()
    private val helpShowcase = mock<HelpShowcaseUseCase> { }
    private val savedStateHandle = MockSavedStateHandle()

    private val initialRounds = listOf(
            RoundPreviewHelper.indoorMetricRoundData,
            RoundPreviewHelper.outdoorImperialRoundData,
            RoundPreviewHelper.singleSubtypeRoundData,
            RoundPreviewHelper.yorkRoundData,
            RoundPreviewHelper.wa25RoundData,
    )
    private val initialState = HandicapTablesState(
            selectRoundDialogState = SelectRoundDialogState(allRounds = initialRounds),
            updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
    )

    private fun getSut(): HandicapTablesViewModel {
        db.roundsRepo.fullRoundsInfo = initialRounds
        return HandicapTablesViewModel(
                db = db.mock,
                helpShowcase = helpShowcase,
                datastore = datastore.mock,
                updateDefaultRoundsTask = mockUpdateDefaultRoundsTask,
                savedStateHandle = savedStateHandle.mock,
        )
    }

    private fun TestScope.setUpGetHandicapsTest(handicap: Int): HandicapTablesViewModel {
        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                initialState,
                sut.state.value,
        )

        val round = RoundPreviewHelper.yorkRoundData
        sut.handle(InputChanged(handicap.toString()))
        sut.handle(SelectRoundDialogAction(RoundIntent.OpenRoundDialog))
        sut.handle(SelectRoundDialogAction(RoundIntent.RoundSelected(round.round)))

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
                sut.state.value.copy(handicaps = emptyList()),
        )

        return sut
    }

    @Test
    fun testGetHandicaps_StandardOutdoor() = runTest {
        val sut = setUpGetHandicapsTest(50)
        val state = sut.state.value

        assertEquals(
                (45..55).toList(),
                state.handicaps.map { it.handicap },
        )
        assertEquals(
                50,
                state.highlightedHandicap!!.handicap,
        )
        state.handicaps.forEach {
            assertEquals(
                    "Check ${it.handicap}",
                    it.score.toDouble() * 6 / 144,
                    it.averageEnd.toDouble(),
                    0.01,
            )
            assertEquals(
                    "Check ${it.handicap}",
                    it.score.toDouble() / 144,
                    it.averageArrow.toDouble(),
                    0.01,
            )
        }

        assertEquals(
                3,
                state.detailedHandicaps?.size,
        )
        assertEquals(
                state.highlightedHandicap!!.score,
                state.detailedHandicaps?.sumOf { it.score }?.roundToInt(),
        )
        assertEquals(
                state.detailedHandicaps!![0].score,
                state.detailedHandicaps!![0].averageArrow * 72,
                0.01,
        )
        assertEquals(
                state.detailedHandicaps!![0].averageEnd,
                state.detailedHandicaps!![0].averageArrow * 6,
                0.01,
        )
    }

    @Test
    fun testGetHandicaps_StandardIndoor() = runTest {
        val sut = setUpGetHandicapsTest(50)
        sut.handle(SelectRoundDialogAction(RoundIntent.OpenRoundDialog))
        sut.handle(SelectRoundDialogAction(RoundIntent.RoundSelected(RoundPreviewHelper.wa25RoundData.round)))

        assertEquals(
                (45..55).toList(),
                sut.state.value.handicaps.map { it.handicap },
        )
        assertEquals(
                50,
                sut.state.value.highlightedHandicap!!.handicap,
        )
        sut.state.value.handicaps.forEach {
            assertEquals(
                    "Check ${it.handicap}",
                    it.score.toDouble() * 3 / 60,
                    it.averageEnd.toDouble(),
                    0.01,
            )
            assertEquals(
                    "Check ${it.handicap}",
                    it.score.toDouble() / 60,
                    it.averageArrow.toDouble(),
                    0.01,
            )
        }
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

        sut.handle(SelectRoundDialogAction(RoundIntent.OpenRoundDialog))
        sut.handle(SelectRoundDialogAction(RoundIntent.RoundSelected(RoundPreviewHelper.wa25RoundData.round)))
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

    @Test
    fun testInitFromSavedState() = runTest {
        savedStateHandle.values[NavArgument.ROUND_ID.toArgName()] = RoundPreviewHelper.yorkRoundData.round.roundId
        savedStateHandle.values[NavArgument.ROUND_SUB_TYPE_ID.toArgName()] = 2
        savedStateHandle.values[NavArgument.HANDICAP.toArgName()] = 50

        val sut = getSut()
        advanceUntilIdle()

        sut.checkInitialState(
                initialState.copy(
                        input = PartialNumberFieldState("50"),
                        selectRoundDialogState = initialState.selectRoundDialogState.copy(
                                selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                                selectedSubTypeId = 2,
                        ),
                ),
                50,
        )
    }

    @Test
    fun testInitFromDefaults() = runTest {
        db.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.yorkRoundData
                    roundSubTypeId = 2
                },
        ).map { it.asDatabaseFullShootInfo() }
        db.archerRepo.handicaps = ArcherHandicapsPreviewHelper.handicaps

        val sut = getSut()
        advanceUntilIdle()

        sut.checkInitialState(
                initialState.copy(
                        input = PartialNumberFieldState("46"),
                        selectRoundDialogState = initialState.selectRoundDialogState.copy(
                                selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                                selectedSubTypeId = 2,
                        ),
                ),
                46,
        )
    }

    @Test
    fun testInitFromDefaultsAndSavedState() = runTest {
        db.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.indoorMetricRoundData
                },
        ).map { it.asDatabaseFullShootInfo() }
        db.archerRepo.handicaps = ArcherHandicapsPreviewHelper.handicaps

        savedStateHandle.values[NavArgument.ROUND_ID.toArgName()] = RoundPreviewHelper.yorkRoundData.round.roundId
        savedStateHandle.values[NavArgument.ROUND_SUB_TYPE_ID.toArgName()] = 2
        savedStateHandle.values[NavArgument.HANDICAP.toArgName()] = 50

        val sut = getSut()
        advanceUntilIdle()

        val expectedState = initialState.copy(
                input = PartialNumberFieldState("50"),
                selectRoundDialogState = initialState.selectRoundDialogState.copy(
                        selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                        selectedSubTypeId = 2,
                ),
        )

        sut.checkInitialState(
                expectedState,
                50,
        )

        sut.handle(SelectRoundDialogAction(OpenSubTypeDialog))
        sut.handle(SelectRoundDialogAction(CloseSubTypeDialog))

        sut.checkInitialState(
                expectedState,
                50,
        )
    }

    private fun HandicapTablesViewModel.checkInitialState(
            expectedState: HandicapTablesState,
            highlightedHc: Int,
    ) {
        assertEquals(
                expectedState,
                state.value.copy(handicaps = emptyList()),
        )
        assertEquals(
                highlightedHc,
                state.value.highlightedHandicap?.handicap,
        )
    }
}
