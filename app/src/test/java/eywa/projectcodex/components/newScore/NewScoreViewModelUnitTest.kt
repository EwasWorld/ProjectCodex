package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState.*
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.database.shootData.DatabaseShoot
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
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class NewScoreViewModelUnitTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val helpShowcaseUseCase: HelpShowcaseUseCase = mock { }
    private val updateDefaultRoundsStatesDelay: Long = 1000

    private val updateDefaultRoundsCompleteState = Complete(1, CompletionType.ALREADY_UP_TO_DATE)

    private val emptyRoundsData = SelectRoundDialogState(allRounds = emptyList())

    private suspend fun getSut(
            testScope: CoroutineScope,
            shootId: Int? = null,
            updateDefaultRoundsStates: List<UpdateDefaultRoundsState>? = listOf(updateDefaultRoundsCompleteState),
    ): NewScoreViewModel {
        val savedStateHandle = MockSavedStateHandle().apply {
            shootId?.let { values["shootId"] = it }
        }

        val updateDefaultRoundsTask: UpdateDefaultRoundsTask = mock {
            on { state } doReturn flow {
                if (updateDefaultRoundsStates.isNullOrEmpty()) {
                    emit(NotStarted)
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
                repo = mock { },
        )
    }

    @Test
    fun testInitialisation_ShootNotFound() = runTest {
        val sut = getSut(testScope = this, shootId = 1, updateDefaultRoundsStates = null)

        Assert.assertEquals(NewScoreState(), sut.state.value)

        advanceUntilIdle()
        Assert.assertEquals(
                NewScoreState(
                        selectRoundDialogState = emptyRoundsData,
                        roundNotFoundError = true,
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_NoShootSet() = runTest {
        val sut = getSut(testScope = this, shootId = null, updateDefaultRoundsStates = null)

        Assert.assertEquals(NewScoreState(), sut.state.value)
        advanceUntilIdle()
        Assert.assertEquals(NewScoreState(selectRoundDialogState = emptyRoundsData), sut.state.value)
    }

    @Test
    fun testInitialisation_EditingExistingScore() = runTest {
        fun create(arrowCount: Int) =
                DatabaseFullShootInfo(
                        shoot = DatabaseShoot(1, Calendar.getInstance(), 1),
                        arrows = List(arrowCount) { DatabaseArrowScore(1, it + 1, 7, false) },
                )

        val shootInitial = create(0)
        val shootSecond = create(12)
        db.shootRepo.fullShoots = listOf(shootInitial)
        db.shootRepo.secondFullShoots = listOf(shootSecond)
        val sut = getSut(testScope = this, shootId = 1, updateDefaultRoundsStates = null)

        Assert.assertEquals(NewScoreState(), sut.state.value)
        advanceTimeBy(1)
        Assert.assertEquals(
                NewScoreState(
                        roundBeingEdited = ShootPreviewHelperDsl.create {
                            shoot = shootInitial.shoot
                            arrows = shootInitial.arrows.orEmpty()
                        },
                        dateShot = shootInitial.shoot.dateShot,
                        selectRoundDialogState = emptyRoundsData,
                ),
                sut.state.value,
        )

        advanceUntilIdle()
        Assert.assertEquals(
                NewScoreState(
                        roundBeingEdited = ShootPreviewHelperDsl.create {
                            shoot = shootSecond.shoot
                            arrows = shootSecond.arrows.orEmpty()
                        },
                        dateShot = shootSecond.shoot.dateShot,
                        selectRoundDialogState = emptyRoundsData,
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
                        selectRoundDialogState = emptyRoundsData,
                        updateDefaultRoundsState = Initialising
                ),
                sut.state.value,
        )
        advanceUntilIdle()
        Assert.assertEquals(
                NewScoreState(
                        selectRoundDialogState = emptyRoundsData,
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
        db.roundsRepo.fullRoundsInfo = data
        db.roundsRepo.secondFullRoundsInfo = data.take(1)
        val sut = getSut(testScope = this, updateDefaultRoundsStates = null)

        Assert.assertEquals(NewScoreState(), sut.state.value)
        advanceTimeBy(1)
        Assert.assertEquals(
                NewScoreState(selectRoundDialogState = SelectRoundDialogState(allRounds = data)),
                sut.state.value,
        )
        advanceUntilIdle()
        Assert.assertEquals(
                NewScoreState(selectRoundDialogState = SelectRoundDialogState(allRounds = data.take(1))),
                sut.state.value,
        )
    }

    // TODO_CURRENT: Test intent handling
}
