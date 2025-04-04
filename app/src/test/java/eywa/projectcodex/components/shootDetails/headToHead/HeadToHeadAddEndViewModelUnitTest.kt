package eywa.projectcodex.components.shootDetails.headToHead

import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.previewHelpers.HeadToHeadPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.asDatabaseFullShootInfo
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndExtras
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndIntent
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndState
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndViewModel
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet
import eywa.projectcodex.model.user.CodexUserPreviewHelper
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
import org.junit.Assert.assertThrows
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
            editingMatchNumber: Int? = null,
            editingSetNumber: Int? = null,
            isInserting: Boolean = false,
    ): HeadToHeadAddEndViewModel {
        database.roundsRepo.fullRoundsInfo = listOf(RoundPreviewHelper.wa70RoundData)

        val shootDetailsRepo = ShootDetailsRepo(
                shootId = 1,
                shootScope = CoroutineScope(testScope.coroutineContext + job),
                shootComponentManager = mock(),
                db = database.mock,
                datastore = datastore.mock,
                helpShowcase = helpShowcase,
                user = CodexUserPreviewHelper.allCapabilities,
        )

        val sut = HeadToHeadAddEndViewModel(
                repo = shootDetailsRepo,
                helpShowcaseUseCase = helpShowcase,
                savedStateHandle = MockSavedStateHandle().apply {
                    values["shootId"] = 1
                    values["matchNumber"] = editingMatchNumber
                    values["setNumber"] = editingSetNumber
                    values["isInsert"] = isInserting
                }.mock,
        )
        testScope.launch(context = job) {
            sut.state.collectLatest {
                currentState = it.getData()
            }
        }

        return sut
    }

    private fun getEmptyData(
            types: List<HeadToHeadArcherType> = listOf(HeadToHeadArcherType.SELF, HeadToHeadArcherType.OPPONENT),
            expectedArrowCount: Int = 3,
    ) =
            listOf(
                    HeadToHeadGridRowData.Arrows(
                            type = HeadToHeadArcherType.SELF,
                            expectedArrowCount = expectedArrowCount,
                    ),
                    HeadToHeadGridRowData.EditableTotal(
                            type = HeadToHeadArcherType.TEAM_MATE,
                            expectedArrowCount = expectedArrowCount,
                    ),
                    HeadToHeadGridRowData.EditableTotal(
                            type = HeadToHeadArcherType.OPPONENT,
                            expectedArrowCount = expectedArrowCount,
                    ),
                    HeadToHeadGridRowData.Result(result = HeadToHeadResult.LOSS),
                    HeadToHeadGridRowData.ShootOff(result = null),
            ).filter { it.type in types }

    private fun HeadToHeadAddEndState.updateExtras(block: HeadToHeadAddEndExtras.() -> HeadToHeadAddEndExtras) =
            copy(extras = extras.block())

    private fun HeadToHeadAddEndState.updateSet(block: FullHeadToHeadSet.() -> FullHeadToHeadSet) =
            updateExtras { copy(set = set.block()) }

    private fun HeadToHeadAddEndState.replaceRow(newRow: HeadToHeadGridRowData) =
            updateSet { copy(data = data.filter { it.type != newRow.type }.plus(newRow)) }

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
    fun testInitialisation_LastSetIsIncomplete() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows() }
                addSet { addRows() }
                addSet {
                    addRow(HeadToHeadGridRowData.Arrows(HeadToHeadArcherType.SELF, 3, listOf(Arrow(10))))
                }
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
                                selected = HeadToHeadArcherType.SELF,
                        ),
                        // Given we're editing the incomplete set, these won't be null
                        teamRunningTotal = 4,
                        opponentRunningTotal = 0,
                        isSetPoints = true,
                        match = h2h.matches.first().match,
                ),
                currentState,
        )

        job.cancel()
    }

    /**
     * Expect running totals to be null
     */
    @Test
    fun testInitialisation_UnknownSet() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows() }
                addSet {
                    addRows()
                    removeRow(HeadToHeadArcherType.OPPONENT)
                }
                addSet { addRows() }
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
                                set = h2h.matches.first().sets.last()
                                        .copy(setNumber = 4, data = getEmptyData()),
                                selected = HeadToHeadArcherType.SELF,
                        ),
                        teamRunningTotal = null,
                        opponentRunningTotal = null,
                        isSetPoints = true,
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
                                            data = getEmptyData(
                                                    listOfNotNull(
                                                            HeadToHeadArcherType.SELF,
                                                            HeadToHeadArcherType.OPPONENT.takeIf { index != 2 },
                                                    ),
                                            ),
                                            teamSize = 1,
                                            isSetPoints = true,
                                            endSize = 3,
                                    ),
                                    selected = HeadToHeadArcherType.SELF,
                            ),
                            teamRunningTotal = if (index == 2) null else h2h.matches.first().sets.size * 2,
                            opponentRunningTotal = if (index == 2) null else 0,
                            isSetPoints = true,
                            match = h2h.matches.first().match,
                    ),
                    currentState,
            )
        }

        job.cancel()
    }

    @Test
    fun testInitialisation_EditingSet() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows() }
                addSet { addRows(isEditable = true) }
                addSet { addRows() }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        getSut(this, editingMatchNumber = 1, editingSetNumber = 2)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadAddEndState(
                        roundInfo = HeadToHeadRoundInfo(),
                        extras = HeadToHeadAddEndExtras(
                                set = h2h.matches.first().sets[1],
                                selected = HeadToHeadArcherType.SELF,
                        ),
                        teamRunningTotal = null,
                        opponentRunningTotal = null,
                        isSetPoints = true,
                        match = h2h.matches.first().match,
                        editingSet = h2h.matches.first().sets[1],
                ),
                currentState,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_EditingNonExistentSet() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows() }
                addSet { addRows() }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        getSut(this, editingMatchNumber = 1, editingSetNumber = 3)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadAddEndState(
                        roundInfo = HeadToHeadRoundInfo(),
                        extras = HeadToHeadAddEndExtras(
                                set = h2h.matches.first().sets[1].copy(
                                        setNumber = 3,
                                        data = getEmptyData(),
                                ),
                                selected = HeadToHeadArcherType.SELF,
                        ),
                        teamRunningTotal = 4,
                        opponentRunningTotal = 0,
                        isSetPoints = true,
                        match = h2h.matches.first().match,
                ),
                currentState,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_Inserting() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows() }
                addSet { addRows() }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        getSut(this, editingMatchNumber = 1, editingSetNumber = 1, isInserting = true)
        advanceUntilIdle()

        assertEquals(
                HeadToHeadAddEndState(
                        roundInfo = HeadToHeadRoundInfo(),
                        extras = HeadToHeadAddEndExtras(
                                set = h2h.matches.first().sets[0].copy(
                                        setNumber = 1,
                                        data = getEmptyData(),
                                ),
                                selected = HeadToHeadArcherType.SELF,
                        ),
                        teamRunningTotal = null,
                        opponentRunningTotal = null,
                        isSetPoints = true,
                        match = h2h.matches.first().match,
                        isInserting = true,
                ),
                currentState,
        )

        job.cancel()
    }

    @Test
    fun testInitialisation_InsertingWhenNoSetGiven() {
        val throwable = assertThrows(IllegalStateException::class.java) {
            runTest {
                val h2h = HeadToHeadPreviewHelperDsl(1).apply {
                    addMatch {
                        addSet { addRows() }
                        addSet { addRows() }
                    }
                }.asFull()
                database.shootRepo.fullShoots = listOf(
                        ShootPreviewHelperDsl.create {
                            this.h2h = h2h
                        }.asDatabaseFullShootInfo()
                )

                getSut(this, editingMatchNumber = 1, isInserting = true)
                advanceUntilIdle()
                assertEquals(null, currentState)

                job.cancel()
            }
        }
        assertEquals("Must provide a set number when inserting", throwable.message)
    }

    /**
     * Expect rows to be selected correctly and result to cycle properly
     */
    @Test
    fun testGridRowClicked_ArrowsTotalAndResult() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet {
                    addRows()
                    addRow(HeadToHeadGridRowData.Result(HeadToHeadResult.TIE))
                }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        val sut = getSut(this)
        advanceUntilIdle()

        val initialState = HeadToHeadAddEndState(
                roundInfo = HeadToHeadRoundInfo(),
                extras = HeadToHeadAddEndExtras(
                        set = h2h.matches.first().sets[0]
                                .copy(
                                        setNumber = 2,
                                        data = getEmptyData(
                                                listOf(
                                                        HeadToHeadArcherType.SELF,
                                                        HeadToHeadArcherType.OPPONENT,
                                                        HeadToHeadArcherType.RESULT,
                                                ),
                                        ),
                                ),
                        selected = HeadToHeadArcherType.SELF,
                ),
                teamRunningTotal = 1,
                opponentRunningTotal = 1,
                isSetPoints = true,
                match = h2h.matches.first().match,
        )
        assertEquals(initialState, currentState)

        sut.handle(HeadToHeadAddEndIntent.GridRowClicked(HeadToHeadArcherType.OPPONENT))
        advanceUntilIdle()
        assertEquals(
                initialState.updateExtras { copy(selected = HeadToHeadArcherType.OPPONENT) },
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.GridRowClicked(HeadToHeadArcherType.SELF))
        advanceUntilIdle()
        assertEquals(
                initialState.updateExtras { copy(selected = HeadToHeadArcherType.SELF) },
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.GridRowClicked(HeadToHeadArcherType.RESULT))
        advanceUntilIdle()
        assertEquals(
                initialState.replaceRow(HeadToHeadGridRowData.Result(HeadToHeadResult.WIN)),
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.GridRowClicked(HeadToHeadArcherType.RESULT))
        advanceUntilIdle()
        assertEquals(
                initialState.replaceRow(HeadToHeadGridRowData.Result(HeadToHeadResult.TIE)),
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.GridRowClicked(HeadToHeadArcherType.RESULT))
        advanceUntilIdle()
        assertEquals(
                initialState.replaceRow(HeadToHeadGridRowData.Result(HeadToHeadResult.LOSS)),
                currentState,
        )

        job.cancel()
    }

    /**
     * Expect shoot off result to cycle properly and non-tied scores to nullify shoot off row result
     */
    @Test
    fun testGridRowClicked_ShootOff() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows(result = HeadToHeadResult.TIE) }
                addSet { addRows(result = HeadToHeadResult.TIE) }
                addSet { addRows(result = HeadToHeadResult.TIE) }
                addSet { addRows(result = HeadToHeadResult.TIE) }
                addSet { addRows(result = HeadToHeadResult.TIE) }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        val initialState = HeadToHeadAddEndState(
                roundInfo = HeadToHeadRoundInfo(),
                extras = HeadToHeadAddEndExtras(
                        set = h2h.matches.first().sets[0]
                                .copy(
                                        setNumber = 6,
                                        data = listOf(
                                                HeadToHeadGridRowData.Arrows(
                                                        type = HeadToHeadArcherType.SELF,
                                                        expectedArrowCount = 1,
                                                        arrows = listOf(Arrow(5)),
                                                ),
                                                HeadToHeadGridRowData.EditableTotal(
                                                        type = HeadToHeadArcherType.OPPONENT,
                                                        expectedArrowCount = 1,
                                                ).let { it.copy(text = it.text.onTextChanged("5")) },
                                                HeadToHeadGridRowData.ShootOff(result = HeadToHeadResult.LOSS),
                                        ),
                                        endSize = 1,
                                ),
                        selected = HeadToHeadArcherType.SELF,
                ),
                teamRunningTotal = 5,
                opponentRunningTotal = 5,
                isSetPoints = true,
                match = h2h.matches.first().match,
        )

        val sut = getSut(this)
        advanceUntilIdle()
        assertEquals(
                initialState.updateSet {
                    copy(
                            data = getEmptyData(
                                    types = listOf(
                                            HeadToHeadArcherType.SELF,
                                            HeadToHeadArcherType.OPPONENT,
                                            HeadToHeadArcherType.SHOOT_OFF,
                                    ),
                                    expectedArrowCount = 1,
                            )
                    )
                }, currentState
        )

        sut.handle(HeadToHeadAddEndIntent.ArrowInputAction(ArrowInputsIntent.ArrowInputted(Arrow(5))))
        sut.handle(HeadToHeadAddEndIntent.GridTextValueChanged(HeadToHeadArcherType.OPPONENT, "5"))
        advanceUntilIdle()
        assertEquals(initialState, currentState)

        sut.handle(HeadToHeadAddEndIntent.GridRowClicked(HeadToHeadArcherType.SHOOT_OFF))
        advanceUntilIdle()
        assertEquals(
                initialState.replaceRow(HeadToHeadGridRowData.ShootOff(HeadToHeadResult.WIN)),
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.GridRowClicked(HeadToHeadArcherType.SHOOT_OFF))
        advanceUntilIdle()
        assertEquals(
                initialState.replaceRow(HeadToHeadGridRowData.ShootOff(HeadToHeadResult.TIE)),
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.GridRowClicked(HeadToHeadArcherType.SHOOT_OFF))
        advanceUntilIdle()
        assertEquals(
                initialState.replaceRow(HeadToHeadGridRowData.ShootOff(HeadToHeadResult.LOSS)),
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.GridTextValueChanged(HeadToHeadArcherType.OPPONENT, "4"))
        advanceUntilIdle()
        assertEquals(
                initialState
                        .replaceRow(
                                HeadToHeadGridRowData.EditableTotal(
                                        type = HeadToHeadArcherType.OPPONENT,
                                        expectedArrowCount = 1,
                                ).let { it.copy(text = it.text.onTextChanged("4")) }
                        )
                        .replaceRow(HeadToHeadGridRowData.ShootOff(null)),
                currentState,
        )

        job.cancel()
    }

    @Test
    fun testToggleShootOff() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            headToHead = headToHead.copy(endSize = 3)
            addMatch {
                addSet { addRows() }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        val sut = getSut(this)
        advanceUntilIdle()

        val initialState = HeadToHeadAddEndState(
                roundInfo = HeadToHeadRoundInfo(isStandardFormat = false),
                extras = HeadToHeadAddEndExtras(
                        set = h2h.matches.first().sets[0]
                                .copy(
                                        setNumber = 2,
                                        data = getEmptyData(),
                                ),
                        selected = HeadToHeadArcherType.SELF,
                ),
                teamRunningTotal = 2,
                opponentRunningTotal = 0,
                isSetPoints = true,
                match = h2h.matches.first().match,
        )
        assertEquals(initialState, currentState)

        sut.handle(HeadToHeadAddEndIntent.ToggleShootOff)
        advanceUntilIdle()
        assertEquals(
                initialState.updateSet {
                    copy(
                            endSize = 1,
                            data = getEmptyData(
                                    types = listOf(
                                            HeadToHeadArcherType.SELF,
                                            HeadToHeadArcherType.OPPONENT,
                                            HeadToHeadArcherType.SHOOT_OFF,
                                    ),
                                    expectedArrowCount = 1,
                            ),
                    )
                },
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.ToggleShootOff)
        advanceUntilIdle()
        assertEquals(initialState, currentState)
    }

    @Test
    fun testEditTypes() = runTest {
        val h2h = HeadToHeadPreviewHelperDsl(1).apply {
            addMatch {
                addSet { addRows() }
            }
        }.asFull()
        database.shootRepo.fullShoots = listOf(
                ShootPreviewHelperDsl.create {
                    this.h2h = h2h
                }.asDatabaseFullShootInfo()
        )

        val sut = getSut(this)
        advanceUntilIdle()

        val initialState = HeadToHeadAddEndState(
                roundInfo = HeadToHeadRoundInfo(),
                extras = HeadToHeadAddEndExtras(
                        set = h2h.matches.first().sets[0]
                                .copy(
                                        setNumber = 2,
                                        data = getEmptyData(),
                                ),
                        selected = HeadToHeadArcherType.SELF,
                ),
                teamRunningTotal = 2,
                opponentRunningTotal = 0,
                isSetPoints = true,
                match = h2h.matches.first().match,
        )
        assertEquals(initialState, currentState)

        sut.handle(HeadToHeadAddEndIntent.EditTypesClicked)
        advanceUntilIdle()
        assertEquals(
                initialState.updateExtras {
                    copy(
                            selectRowTypesDialogState = mapOf(
                                    HeadToHeadArcherType.SELF to false,
                                    HeadToHeadArcherType.OPPONENT to true,
                            )
                    )
                },
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.EditTypesItemClicked(HeadToHeadArcherType.SELF))
        advanceUntilIdle()
        assertEquals(
                initialState.updateExtras {
                    copy(
                            selectRowTypesDialogState = mapOf(
                                    HeadToHeadArcherType.SELF to true,
                                    HeadToHeadArcherType.OPPONENT to true,
                            )
                    )
                },
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.EditTypesItemClicked(HeadToHeadArcherType.SELF))
        advanceUntilIdle()
        assertEquals(
                initialState.updateExtras {
                    copy(
                            selectRowTypesDialogState = mapOf(
                                    HeadToHeadArcherType.OPPONENT to true,
                            ),
                            selectRowTypesDialogUnknownWarning = ResOrActual.StringResource(
                                    R.string.head_to_head_add_end__unknown_result_warning_self,
                            )
                    )
                },
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.EditTypesItemClicked(HeadToHeadArcherType.SELF))
        advanceUntilIdle()
        assertEquals(
                initialState.updateExtras {
                    copy(
                            selectRowTypesDialogState = mapOf(
                                    HeadToHeadArcherType.SELF to false,
                                    HeadToHeadArcherType.OPPONENT to true,
                            )
                    )
                },
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.EditTypesItemClicked(HeadToHeadArcherType.SELF))
        advanceUntilIdle()
        assertEquals(
                initialState.updateExtras {
                    copy(
                            selectRowTypesDialogState = mapOf(
                                    HeadToHeadArcherType.SELF to true,
                                    HeadToHeadArcherType.OPPONENT to true,
                            )
                    )
                },
                currentState,
        )

        sut.handle(HeadToHeadAddEndIntent.CompleteEditTypesDialog)
        advanceUntilIdle()
        assertEquals(
                initialState.replaceRow(HeadToHeadGridRowData.EditableTotal(HeadToHeadArcherType.SELF, 3)),
                currentState,
        )
    }
}
