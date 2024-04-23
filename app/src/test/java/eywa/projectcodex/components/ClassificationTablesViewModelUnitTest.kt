package eywa.projectcodex.components

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent.OpenRoundDialog
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent.RoundSelected
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SubTypeIntent.OpenSubTypeDialog
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SubTypeIntent.SubTypeSelected
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.*
import eywa.projectcodex.components.classificationTables.ClassificationTablesState
import eywa.projectcodex.components.classificationTables.ClassificationTablesViewModel
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
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
        )
    }

    @Test
    fun test() = runTest {
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
}
