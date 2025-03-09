package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import eywa.projectcodex.common.sharedUi.previewHelpers.HeadToHeadPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.shootData.headToHead.HeadToHeadRepo
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.model.headToHead.FullHeadToHead
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HeadToHeadTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase
    private lateinit var h2hRepo: HeadToHeadRepo
    private lateinit var data: FullHeadToHead

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
        h2hRepo = db.h2hRepo()
    }

    @After
    fun closeDb() {
        db.closeDb()
    }

    private suspend fun addSingleH2hEntryToDb(isStandardFormat: Boolean = true) {
        data = HeadToHeadPreviewHelperDsl(1).apply {
            headToHead = headToHead.copy(endSize = if (isStandardFormat) null else 3)
            addMatch {
                addSet {
                    addRows(result = HeadToHeadResult.TIE)
                }
                addSet {
                    addRows()
                }
                addSet {
                    addRows()
                }
            }
            addMatch {
                addSet {
                    addRows()
                }
                addSet {
                    addRows(result = HeadToHeadResult.TIE)
                }
                addSet {
                    addRows()
                }
            }
            addMatch {
                addSet {
                    addRows()
                }
                addSet {
                    addRows()
                }
                addSet {
                    addRows(result = HeadToHeadResult.TIE)
                }
            }
        }.asFull()

        db.add(ShootPreviewHelperDsl.create {})
        db.add(data)
    }

    private suspend fun fetchH2hDataFromDb() = FullHeadToHead(h2hRepo.get(shootId = 1).first()!!, false)

    /**
     * Strips dbId and sorts lists for comparison
     */
    private suspend fun fetchH2hDataFromDbForCompare() = fetchH2hDataFromDb()
            .let { h2h ->
                h2h.copy(matches = h2h.matches.map { match ->
                    match.copy(sets = match.sets.map { set ->
                        set.copy(data = set.data.map {
                            when (it) {
                                is HeadToHeadGridRowData.Arrows -> it.copy(dbIds = null)
                                is HeadToHeadGridRowData.Result -> it.copy(dbId = null)
                                is HeadToHeadGridRowData.Total -> it.copy(dbId = null)
                                is HeadToHeadGridRowData.EditableTotal -> it.copy(dbId = null)
                                is HeadToHeadGridRowData.ShootOff -> it.copy(dbId = null)
                            }
                        }.sortedByDescending { it.type.name })
                    })
                })
            }

    @Test
    fun testDeleteMatch() = runTest {
        addSingleH2hEntryToDb()

        h2hRepo.delete(shootId = 1, matchNumber = 2)

        assertEquals(
                data.copy(
                        matches = listOf(
                                data.matches[0],
                                // matchNumber 2 is removed and matchNumber 3 is changed to matchNumber 2
                                data.matches[2].let { it.copy(match = it.match.copy(matchNumber = 2)) },
                        ),
                ),
                fetchH2hDataFromDbForCompare(),
        )
    }

    @Test
    fun testDeleteSet() = runTest {
        addSingleH2hEntryToDb()

        h2hRepo.delete(shootId = 1, matchNumber = 2, setNumber = 2)

        assertEquals(
                data.copy(
                        matches = listOf(
                                data.matches[0],
                                data.matches[1].let {
                                    // setNumber 2 is removed and setNumber 3 is changed to setNumber 2
                                    it.copy(sets = listOf(it.sets[0], it.sets[2].copy(setNumber = 2)))
                                },
                                data.matches[2],
                        ),
                ),
                fetchH2hDataFromDbForCompare(),
        )
    }

    @Test
    fun testInsertMatch() = runTest {
        addSingleH2hEntryToDb()

        val match = HeadToHeadPreviewHelperDsl(1)
                .apply {
                    addMatch { match = match.copy(matchNumber = 2) }
                }
                .asFull()
                .matches[0]
        h2hRepo.insert(match.match)

        assertEquals(
                data.copy(
                        matches = listOf(
                                data.matches[0],
                                match,
                                // Bump match numbers
                                data.matches[1].let { it.copy(match = it.match.copy(matchNumber = 3)) },
                                data.matches[2].let { it.copy(match = it.match.copy(matchNumber = 4)) },
                        ),
                ),
                fetchH2hDataFromDbForCompare(),
        )
    }

    @Test
    fun testInsertSet() = runTest {
        addSingleH2hEntryToDb(false)

        val set = HeadToHeadPreviewHelperDsl(1)
                .apply {
                    headToHead = headToHead.copy(endSize = 3)
                    addMatch {
                        match = match.copy(matchNumber = 2)
                        addSet {
                            setNumber = 2
                            addRows(result = HeadToHeadResult.LOSS)
                        }
                    }
                }
                .asFull()
                .matches[0]
                .sets[0]
        h2hRepo.insert(*set.asDatabaseDetails(1, 2).toTypedArray())

        assertEquals(
                data.copy(
                        matches = listOf(
                                data.matches[0],
                                data.matches[1].let { match ->
                                    match.copy(
                                            sets = listOf(
                                                    match.sets[0],
                                                    set,
                                                    match.sets[1].copy(setNumber = 3),
                                                    match.sets[2].copy(setNumber = 4),
                                            )
                                    )
                                },
                                data.matches[2],
                        ),
                ),
                fetchH2hDataFromDbForCompare(),
        )
    }

    @Test
    fun testUpdateSet() = runTest {
        addSingleH2hEntryToDb()

        val set = HeadToHeadPreviewHelperDsl(1)
                .apply {
                    headToHead = headToHead.copy(endSize = 3)
                    addMatch {
                        match = match.copy(matchNumber = 2)
                        addSet {
                            setNumber = 2
                            addRows(result = HeadToHeadResult.LOSS)
                        }
                    }
                }
                .asFull()
                .matches[0]
                .sets[0]
        h2hRepo.update(
                newDetails = set.asDatabaseDetails(1, 2),
                oldDetails = fetchH2hDataFromDb().matches[1].sets[1].asDatabaseDetails(1, 2),
        )

        assertEquals(
                data.copy(
                        matches = listOf(
                                data.matches[0],
                                data.matches[1].let { match ->
                                    match.copy(sets = listOf(match.sets[0], set, match.sets[2]))
                                },
                                data.matches[2],
                        ),
                ),
                fetchH2hDataFromDbForCompare(),
        )
    }
}
