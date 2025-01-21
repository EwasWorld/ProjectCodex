package eywa.projectcodex.components.shootDetails.headToHead

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.previewHelpers.HeadToHeadPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.asDatabaseFullShootInfo
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndExtras
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndState
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndViewModel
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockSavedStateHandle
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class HeadToHeadAddEndViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val helpShowcase = mock<HelpShowcaseUseCase> {}
    private val database = MockScoresRoomDatabase()
    private val datastore = MockDatastore()

    private var job = Job()
    private var currentState: HeadToHeadAddEndState? = null

    private fun getSut(
            testScope: CoroutineScope,
    ): HeadToHeadAddEndViewModel {
        database.roundsRepo.fullRoundsInfo = listOf(RoundPreviewHelper.wa70RoundData)

        val shootDetailsRepo = ShootDetailsRepo(
                shootId = 1,
                shootScope = CoroutineScope(testScope.coroutineContext + job),
                shootComponentManager = mock(),
                db = database.mock,
                datastore = datastore.mock,
                helpShowcase = helpShowcase,
        )

        val sut = HeadToHeadAddEndViewModel(
                repo = shootDetailsRepo,
                helpShowcaseUseCase = helpShowcase,
                savedStateHandle = MockSavedStateHandle().apply { values["shootId"] = 1 }.mock,
        )
        testScope.launch(context = job) {
            sut.state.collectLatest {
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
                    h2h = HeadToHeadPreviewHelperDsl(1).apply {
                        addMatch { }
                    }.asFull()
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
                true,
                currentState?.extras?.openAddMatchScreen,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_HeatComplete_Win() = runTest {
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    h2h = HeadToHeadPreviewHelperDsl(1).apply {
                        addMatch {
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
                true,
                currentState?.extras?.openAddMatchScreen,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_HeatComplete_ShootOffComplete() = runTest {
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    h2h = HeadToHeadPreviewHelperDsl(1).apply {
                        addMatch {
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
                true,
                currentState?.extras?.openAddMatchScreen,
        )

        job.cancel()
    }

    /**
     * Expect to edit last set
     */
    @Test
    fun testInitialisation_LastSetIsEmptyUnknown() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows() }
                addSet { addRows() }
                addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        getSut(this)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadAddEndState(
                        roundInfo = HeadToHeadRoundInfo(),
                        extras = HeadToHeadAddEndExtras(
                                set = h2h.matches.first().sets.last(),
                                selected = HeadToHeadArcherType.TEAM,
                        ),
                        teamRunningTotal = null,
                        opponentRunningTotal = null,
                        isRecurveStyle = true,
                        match = h2h.matches.first().match,
                ),
                currentState,
        )

        job.cancel()
    }

    /**
     * Expect to edit last set
     */
    @Test
    fun testInitialisation_LastSetIsIncomplete() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows() }
                addSet { addRows() }
                addSet { addRows(result = HeadToHeadResult.INCOMPLETE) }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        getSut(this)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadAddEndState(
                        roundInfo = HeadToHeadRoundInfo(),
                        extras = HeadToHeadAddEndExtras(
                                set = h2h.matches.first().sets.last(),
                                selected = HeadToHeadArcherType.TEAM,
                        ),
                        teamRunningTotal = null,
                        opponentRunningTotal = null,
                        isRecurveStyle = true,
                        match = h2h.matches.first().match,
                ),
                currentState,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_CreateNewSet() = runTest {
        val inputs = listOf(
                HeadToHeadPreviewHelperDsl(1).apply {
                    addMatch {}
                },
                HeadToHeadPreviewHelperDsl(1).apply {
                    addMatch {
                        addSet { addRows() }
                    }
                },
                HeadToHeadPreviewHelperDsl(1).apply {
                    addMatch {
                        addSet {
                            addRows()
                            removeRow(HeadToHeadArcherType.OPPONENT)
                        }
                    }
                },
                HeadToHeadPreviewHelperDsl(1).apply {
                    addMatch {
                        match = match.copy(isBye = true)
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
                    HeadToHeadAddEndState(
                            roundInfo = HeadToHeadRoundInfo(),
                            extras = HeadToHeadAddEndExtras(
                                    set = FullHeadToHeadSet(
                                            setNumber = h2h.matches.first().sets.size + 1,
                                            data = listOf(
                                                    HeadToHeadGridRowData.Arrows(
                                                            type = HeadToHeadArcherType.SELF,
                                                            expectedArrowCount = 3,
                                                    ),
                                                    HeadToHeadGridRowData.EditableTotal(
                                                            type = HeadToHeadArcherType.OPPONENT,
                                                            expectedArrowCount = 3,
                                                    ),
                                            ),
                                            teamSize = 1,
                                            isShootOffWin = false,
                                            isRecurveStyle = true,
                                    ),
                                    selected = HeadToHeadArcherType.SELF,
                            ),
                            teamRunningTotal = if (index == 2) null else h2h.matches.first().sets.size * 2,
                            opponentRunningTotal = if (index == 2) null else 0,
                            isRecurveStyle = true,
                            match = h2h.matches.first().match,
                    ),
                    currentState,
            )
        }

        job.cancel()
    }
}
