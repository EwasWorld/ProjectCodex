package eywa.projectcodex.components

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.asDatabaseFullShootInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent.OpenRoundDialog
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent.RoundSelected
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SubTypeIntent.*
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.*
import eywa.projectcodex.components.classificationTables.ClassificationTablesState
import eywa.projectcodex.components.classificationTables.ClassificationTablesViewModel
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockSavedStateHandle
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import eywa.projectcodex.testUtils.RawResourcesHelper
import eywa.projectcodex.testUtils.mockUpdateDefaultRoundsTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ClassificationTablesViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val datastore = MockDatastore()
    private val helpShowcase = mock<HelpShowcaseUseCase> { }
    private val classificationTables = RawResourcesHelper.classificationTables
    private val savedStateHandle = MockSavedStateHandle()

    private val initialRounds = listOf(
            RoundPreviewHelper.yorkRoundData,
            RoundPreviewHelper.wa25RoundData,
            RoundPreviewHelper.wa1440RoundData,
    )

    private val initialState = ClassificationTablesState(
            roughHandicaps = classificationTables.getRoughHandicaps(
                    isGent = true,
                    age = ClassificationAge.SENIOR,
                    bow = ClassificationBow.RECURVE,
                    wa1440RoundInfo = RoundPreviewHelper.wa1440RoundData,
            )!!.map { it.copy(score = null) },
            selectRoundDialogState = SelectRoundDialogState(
                    allRounds = initialRounds,
            ),
            updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
    )

    private fun getSut(): ClassificationTablesViewModel {
        db.rounds.fullRoundsInfo = initialRounds
        return ClassificationTablesViewModel(
                db = db.mock,
                helpShowcase = helpShowcase,
                tables = classificationTables,
                datastore = datastore.mock,
                updateDefaultRoundsTask = mockUpdateDefaultRoundsTask,
                savedStateHandle = savedStateHandle.mock,
        )
    }

    @Test
    fun testMain() = runTest {
        val sut = getSut()
        advanceUntilIdle()

        /*
         * No round
         */
        assertEquals(
                initialState,
                sut.state.value,
        )

        /*
         * Round with all possible classifications
         */
        sut.handle(SelectRoundDialogAction(OpenRoundDialog))
        sut.handle(SelectRoundDialogAction(RoundSelected(RoundPreviewHelper.yorkRoundData.round)))

        var official = classificationTables.get(
                isGent = true,
                age = ClassificationAge.SENIOR,
                bow = ClassificationBow.RECURVE,
                fullRoundInfo = RoundPreviewHelper.yorkRoundData,
                roundSubTypeId = 1,
        )!!
        var rough = classificationTables.getRoughHandicaps(
                isGent = true,
                age = ClassificationAge.SENIOR,
                bow = ClassificationBow.RECURVE,
                wa1440RoundInfo = RoundPreviewHelper.wa1440RoundData,
        )!!.map {
            it.copy(
                    score = Handicap.getScoreForRound(
                            round = RoundPreviewHelper.yorkRoundData,
                            subType = 1,
                            handicap = it.handicap!!.toDouble()
                    )
            )
        }

        assertEquals(
                ClassificationTablesState(
                        officialClassifications = official,
                        roughHandicaps = rough,
                        selectRoundDialogState = SelectRoundDialogState(
                                allRounds = initialRounds,
                                selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                                selectedSubTypeId = 1,
                        ),
                        updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
                ),
                sut.state.value,
        )
        assertEquals(
                official.map { it to true },
                sut.state.value.scores,
        )

        /*
         * Round with only some valid classifications
         */
        sut.handle(SelectRoundDialogAction(OpenSubTypeDialog))
        sut.handle(SelectRoundDialogAction(SubTypeSelected(RoundPreviewHelper.yorkRoundData.roundSubTypes?.find { it.subTypeId == 2 }!!)))

        official = classificationTables.get(
                isGent = true,
                age = ClassificationAge.SENIOR,
                bow = ClassificationBow.RECURVE,
                fullRoundInfo = RoundPreviewHelper.yorkRoundData,
                roundSubTypeId = 2,
        )!!
        rough = rough.map {
            it.copy(
                    score = Handicap.getScoreForRound(
                            round = RoundPreviewHelper.yorkRoundData,
                            subType = 2,
                            handicap = it.handicap!!.toDouble()
                    )
            )
        }

        assertEquals(
                ClassificationTablesState(
                        officialClassifications = official,
                        roughHandicaps = rough,
                        selectRoundDialogState = SelectRoundDialogState(
                                allRounds = initialRounds,
                                selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                                selectedSubTypeId = 2,
                        ),
                        updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
                ),
                sut.state.value,
        )
        assertEquals(
                official.map { it to true }
                        .plus(rough.sortedBy { it.handicap }.take(4).map { it to false })
                        .sortedBy { it.first.classification.ordinal },
                sut.state.value.scores,
        )
    }

    @Test
    fun testInitFromSavedState() = runTest {
        savedStateHandle.values[NavArgument.ROUND_ID.toArgName()] = RoundPreviewHelper.yorkRoundData.round.roundId
        savedStateHandle.values[NavArgument.ROUND_SUB_TYPE_ID.toArgName()] = 2

        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                initialState.copy(
                        selectRoundDialogState = initialState.selectRoundDialogState.copy(
                                selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                                selectedSubTypeId = 2,
                        ),
                ).setScores(),
                sut.state.value,
        )
    }

    @Test
    fun testInitFromSavedState_InvalidRound() = runTest {
        savedStateHandle.values[NavArgument.ROUND_ID.toArgName()] = 1000

        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                initialState,
                sut.state.value,
        )
    }

    @Test
    fun testInitFromSavedState_InvalidSubType() = runTest {
        savedStateHandle.values[NavArgument.ROUND_ID.toArgName()] = RoundPreviewHelper.yorkRoundData.round.roundId
        savedStateHandle.values[NavArgument.ROUND_SUB_TYPE_ID.toArgName()] = 1000

        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                initialState.copy(
                        selectRoundDialogState = initialState.selectRoundDialogState.copy(
                                selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                        ),
                ).setScores(),
                sut.state.value,
        )
    }

    @Test
    fun testInitFromDefaults() = runTest {
        db.archerRepo.defaultArcher = DatabaseArcher(10, "test", false, ClassificationAge.OVER_50)
        db.bow.defaultBow = DatabaseBow(10, "test", type = ClassificationBow.COMPOUND)
        db.shootDao.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.yorkRoundData
                    roundSubTypeId = 2
                },
        ).map { it.asDatabaseFullShootInfo() }

        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                initialState.copy(
                        selectRoundDialogState = initialState.selectRoundDialogState.copy(
                                selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                                selectedSubTypeId = 2,
                        ),
                        isGent = false,
                        age = ClassificationAge.OVER_50,
                        bow = ClassificationBow.COMPOUND,
                ).setScores(),
                sut.state.value,
        )
    }

    @Test
    fun testInitFromDefaultsAndSavedState() = runTest {
        db.archerRepo.defaultArcher = DatabaseArcher(10, "test", false, ClassificationAge.OVER_50)
        db.bow.defaultBow = DatabaseBow(10, "test", type = ClassificationBow.COMPOUND)
        db.shootDao.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.wa1440RoundData
                },
        ).map { it.asDatabaseFullShootInfo() }
        savedStateHandle.values[NavArgument.ROUND_ID.toArgName()] = RoundPreviewHelper.yorkRoundData.round.roundId
        savedStateHandle.values[NavArgument.ROUND_SUB_TYPE_ID.toArgName()] = 2

        val sut = getSut()
        advanceUntilIdle()

        val expectedState = initialState.copy(
                selectRoundDialogState = initialState.selectRoundDialogState.copy(
                        selectedRoundId = RoundPreviewHelper.yorkRoundData.round.roundId,
                        selectedSubTypeId = 2,
                ),
                isGent = false,
                age = ClassificationAge.OVER_50,
                bow = ClassificationBow.COMPOUND,
        ).setScores()

        assertEquals(
                expectedState,
                sut.state.value,
        )

        sut.handle(SelectRoundDialogAction(OpenSubTypeDialog))
        sut.handle(SelectRoundDialogAction(CloseSubTypeDialog))

        assertEquals(
                expectedState,
                sut.state.value,
        )
    }

    private fun ClassificationTablesState.setScores(): ClassificationTablesState {
        return copy(
                roughHandicaps = classificationTables.getRoughHandicaps(
                        isGent = isGent,
                        age = age,
                        bow = bow,
                        wa1440RoundInfo = RoundPreviewHelper.wa1440RoundData,
                )!!.map {
                    val score = selectRoundDialogState.selectedRound?.let { round ->
                        Handicap.getScoreForRound(
                                round = round,
                                subType = selectRoundDialogState.selectedSubTypeId,
                                handicap = it.handicap!!.toDouble(),
                                innerTenArcher = bow == ClassificationBow.COMPOUND,
                                use2023Handicaps = use2023Handicaps,
                        )
                    }
                    it.copy(score = score)
                },
                officialClassifications = selectRoundDialogState.selectedRound?.let {
                    classificationTables.get(
                            isGent = isGent,
                            age = age,
                            bow = bow,
                            fullRoundInfo = it,
                            roundSubTypeId = selectRoundDialogState.selectedSubTypeId,
                    )
                }!!,
        )
    }
}
