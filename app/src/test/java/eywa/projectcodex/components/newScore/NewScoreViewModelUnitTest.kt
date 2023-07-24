package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState.*
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockSavedStateHandle
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class NewScoreViewModelUnitTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val helpShowcaseUseCase: HelpShowcaseUseCase = mock { }
    private val updateDefaultRoundsStatesDelay: Long = 1000

    private val updateDefaultRoundsCompleteState = Complete(1, CompletionType.ALREADY_UP_TO_DATE)

    private suspend fun getSut(
            testScope: CoroutineScope,
            archerRoundId: Int? = null,
            updateDefaultRoundsStates: List<UpdateDefaultRoundsState?>? = listOf(updateDefaultRoundsCompleteState),
    ): NewScoreViewModel {
        val savedStateHandle = MockSavedStateHandle().apply {
            archerRoundId?.let { values["archerRoundId"] = it }
        }

        val updateDefaultRoundsTask: UpdateDefaultRoundsTask = mock {
            on { state } doReturn flow {
                if (updateDefaultRoundsStates.isNullOrEmpty()) {
                    emit(null)
                }
                updateDefaultRoundsStates?.forEach {
                    emit(it)
                    delay(updateDefaultRoundsStatesDelay)
                }
            }.stateIn(testScope)
        }

        return NewScoreViewModel(
                db = db.mock,
                updateDefaultRoundsTask = updateDefaultRoundsTask,
                helpShowcase = helpShowcaseUseCase,
                savedStateHandle = savedStateHandle.mock,
        )
    }

    @Test
    fun testInitialisation_ArcherRoundNotFound() = runTest {
        val sut = getSut(testScope = this, archerRoundId = 1, updateDefaultRoundsStates = null)

        Assert.assertEquals(NewScoreState(), sut.state.value)

        advanceUntilIdle()
        Assert.assertEquals(
                NewScoreState(
                        roundsData = emptyList(),
                        roundNotFoundError = true,
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_NoArcherRoundSet() = runTest {
        val sut = getSut(testScope = this, archerRoundId = null, updateDefaultRoundsStates = null)

        Assert.assertEquals(NewScoreState(), sut.state.value)
        advanceUntilIdle()
        Assert.assertEquals(NewScoreState(roundsData = emptyList()), sut.state.value)
    }

    @Test
    fun testInitialisation_EditingExistingScore() = runTest {
        fun create(arrowCount: Int) =
                DatabaseFullArcherRoundInfo(
                        archerRound = ArcherRound(1, Calendar.getInstance(), 1),
                        arrows = List(arrowCount) { ArrowValue(1, it + 1, 7, false) },
                )

        val archerRoundInitial = create(0)
        val archerRoundSecond = create(12)
        db.archerRoundDao.fullArcherRounds = listOf(archerRoundInitial)
        db.archerRoundDao.secondFullArcherRounds = listOf(archerRoundSecond)
        val sut = getSut(testScope = this, archerRoundId = 1, updateDefaultRoundsStates = null)

        Assert.assertEquals(NewScoreState(), sut.state.value)
        advanceTimeBy(1)
        Assert.assertEquals(
                NewScoreState(
                        roundBeingEdited = archerRoundInitial.archerRound,
                        roundBeingEditedArrowsShot = archerRoundInitial.arrows.orEmpty().count(),
                        dateShot = archerRoundInitial.archerRound.dateShot,
                        selectedRound = archerRoundInitial.round,
                        selectedSubtype = archerRoundInitial.roundSubType,
                        roundsData = emptyList(),
                ),
                sut.state.value,
        )

        advanceUntilIdle()
        Assert.assertEquals(
                NewScoreState(
                        roundBeingEdited = archerRoundSecond.archerRound,
                        roundBeingEditedArrowsShot = archerRoundSecond.arrows.orEmpty().count(),
                        dateShot = archerRoundSecond.archerRound.dateShot,
                        selectedRound = archerRoundSecond.round,
                        selectedSubtype = archerRoundSecond.roundSubType,
                        roundsData = emptyList(),
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_UpdateDbTask() = runTest {
        val sut = getSut(
                testScope = this,
                updateDefaultRoundsStates = listOf(
                        Initialising,
                        updateDefaultRoundsCompleteState,
                ),
        )

        Assert.assertEquals(NewScoreState(), sut.state.value)
        advanceTimeBy(1)
        Assert.assertEquals(
                NewScoreState(
                        roundsData = emptyList(),
                        updateDefaultRoundsState = Initialising
                ),
                sut.state.value,
        )
        advanceUntilIdle()
        Assert.assertEquals(
                NewScoreState(
                        roundsData = emptyList(),
                        updateDefaultRoundsState = updateDefaultRoundsCompleteState
                ),
                sut.state.value,
        )
    }


    @Test
    fun testInitialisation_Rounds() = runTest {
        val data = listOf(
                RoundPreviewHelper.indoorMetricRoundData,
                RoundPreviewHelper.outdoorImperialRoundData,
        )
        db.roundDao.fullRoundsInfo = data
        db.roundDao.secondFullRoundsInfo = data.take(1)
        val sut = getSut(testScope = this, updateDefaultRoundsStates = null)

        Assert.assertEquals(NewScoreState(), sut.state.value)
        advanceTimeBy(1)
        Assert.assertEquals(
                NewScoreState(roundsData = data),
                sut.state.value,
        )
        advanceUntilIdle()
        Assert.assertEquals(
                NewScoreState(roundsData = data.take(1)),
                sut.state.value,
        )
    }

    // TODO_CURRENT: Test intent handling
}
