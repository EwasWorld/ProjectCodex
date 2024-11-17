package eywa.projectcodex.components.shootDetails.headToHead

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.previewHelpers.HeadToHeadPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.asDatabaseFullShootInfo
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatExtras
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatState
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatViewModel
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockSavedStateHandle
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class HeadToHeadAddHeatViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val helpShowcase = mock<HelpShowcaseUseCase> {}
    private val database = MockScoresRoomDatabase()
    private val datastore = MockDatastore()

    private var job = Job()
    private var currentState: HeadToHeadAddHeatState? = null

    private fun getSut(
            testScope: CoroutineScope,
    ): HeadToHeadAddHeatViewModel {
        database.roundsRepo.fullRoundsInfo = listOf(RoundPreviewHelper.wa70RoundData)

        val shootDetailsRepo = ShootDetailsRepo(
                shootId = 1,
                shootScope = CoroutineScope(testScope.coroutineContext + job),
                shootComponentManager = mock(),
                db = database.mock,
                datastore = datastore.mock,
                helpShowcase = helpShowcase,
        )

        val sut = HeadToHeadAddHeatViewModel(
                db = database.mock,
                repo = shootDetailsRepo,
                helpShowcaseUseCase = helpShowcase,
                savedStateHandle = MockSavedStateHandle().apply { values["shootId"] = 1 }.mock,
        )
        testScope.launch(context = job) {
            sut.state.collect {
                currentState = it.getData()
            }
        }

        return sut
    }

    @Test
    fun testInitialisation_RoundInfo() = runTest {
        val sightMark = SightMark(SightMarksPreviewHelper.sightMarks[5])
        database.sightMarksRepo.sightMarks = listOf(sightMark)
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.wa70RoundData
                    h2h = HeadToHeadPreviewHelperDsl(1).asFull()
                }.asDatabaseFullShootInfo()
        )

        getSut(this)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadRoundInfo(
                        distance = 70,
                        isMetric = true,
                        sightMark = sightMark,
                        round = RoundPreviewHelper.wa70RoundData.round,
                        face = null,
                ),
                currentState?.roundInfo,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_NoHeat() = runTest {
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    h2h = HeadToHeadPreviewHelperDsl(1).asFull()
                }.asDatabaseFullShootInfo()
        )

        getSut(this)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadAddHeatState(
                        roundInfo = HeadToHeadRoundInfo(),
                        extras = HeadToHeadAddHeatExtras(),
                        previousHeat = null,
                ),
                currentState,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_HeatComplete_Win() = runTest {
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    h2h = HeadToHeadPreviewHelperDsl(1).apply {
                        addHeat {
                            heat = heat.copy(heat = 3)
                            addSet { addRows() }
                            addSet { addRows() }
                            addSet { addRows() }
                        }
                    }.asFull()
                }.asDatabaseFullShootInfo()
        )

        getSut(this)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadAddHeatState(
                        roundInfo = HeadToHeadRoundInfo(),
                        extras = HeadToHeadAddHeatExtras(heat = 2),
                        previousHeat = HeadToHeadAddHeatState.PreviousHeat(
                                heat = 3,
                                result = HeadToHeadResult.WIN,
                                runningTotal = 6 to 0,
                        ),
                ),
                currentState,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_HeatComplete_ShootOffComplete() = runTest {
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    h2h = HeadToHeadPreviewHelperDsl(1).apply {
                        addHeat {
                            heat = heat.copy(heat = 3)
                            addSet { addRows() }
                            addSet { addRows() }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.TIE) }
                            addSet { addRows(result = HeadToHeadResult.LOSS, winnerScore = 10, loserScore = 1) }
                        }
                    }.asFull()
                }.asDatabaseFullShootInfo()
        )

        getSut(this)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadAddHeatState(
                        roundInfo = HeadToHeadRoundInfo(),
                        extras = HeadToHeadAddHeatExtras(heat = 2),
                        previousHeat = HeadToHeadAddHeatState.PreviousHeat(
                                heat = 3,
                                result = HeadToHeadResult.LOSS,
                                runningTotal = 5 to 6,
                        ),
                ),
                currentState,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_CreateNewSet() = runTest {
        val inputs = listOf(
                HeadToHeadPreviewHelperDsl(1).apply {
                    addHeat {}
                },
                HeadToHeadPreviewHelperDsl(1).apply {
                    addHeat {
                        addSet { addRows() }
                    }
                },
                HeadToHeadPreviewHelperDsl(1).apply {
                    addHeat {
                        addSet {
                            addRows()
                            removeRow(HeadToHeadArcherType.OPPONENT)
                        }
                    }
                },
                HeadToHeadPreviewHelperDsl(1).apply {
                    addHeat {
                        addSet { addRows() }
                        addSet { addRows() }
                        addSet { addRows(result = HeadToHeadResult.INCOMPLETE) }
                    }
                },
                HeadToHeadPreviewHelperDsl(1).apply {
                    addHeat {
                        addSet { addRows() }
                        addSet { addRows() }
                        addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                    }
                },
        ).map { it.asFull() }

        inputs.forEachIndexed { index, h2h ->
            database.shootRepo.fullShoots = listOf(
                    ShootPreviewHelperDsl.create {
                        this.h2h = h2h
                    }.asDatabaseFullShootInfo()
            )

            getSut(this)
            advanceUntilIdle()

            assertEquals(
                    "$index",
                    true,
                    currentState?.extras?.openAddEndScreen,
            )
        }

        job.cancel()
    }
}
