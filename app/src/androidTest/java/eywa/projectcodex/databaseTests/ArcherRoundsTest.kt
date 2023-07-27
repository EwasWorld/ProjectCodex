package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.*
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValueDao
import eywa.projectcodex.database.rounds.RoundArrowCountDao
import eywa.projectcodex.database.rounds.RoundDao
import eywa.projectcodex.database.rounds.RoundSubTypeDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ArcherRoundsTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase
    private lateinit var archerRoundDao: ArcherRoundDao
    private lateinit var roundDao: RoundDao
    private lateinit var roundSubTypeDao: RoundSubTypeDao
    private lateinit var roundArrowCountsDao: RoundArrowCountDao
    private lateinit var arrowValueDao: ArrowValueDao

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
        archerRoundDao = db.archerRoundDao()
        roundDao = db.roundDao()
        roundSubTypeDao = db.roundSubTypeDao()
        roundArrowCountsDao = db.roundArrowCountDao()
        arrowValueDao = db.arrowValueDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    /**
     * Check inserted values are the same as retrieved
     */
    @Test
    fun basicTest() = runTest(dispatchTimeoutMs = 2000) {
        val retrievedArcherRounds = archerRoundDao.getAllFullArcherRoundInfo()

        /*
         * Add and retrieve
         */
        val archerRounds = listOf(
                ArcherRound(1, TestUtils.generateDate(), 1, false),
                ArcherRound(2, TestUtils.generateDate(), 2, false),
                ArcherRound(3, TestUtils.generateDate(), 1, false),
                ArcherRound(4, TestUtils.generateDate(), 1, false),
                ArcherRound(5, TestUtils.generateDate(), 2, false),
                ArcherRound(6, TestUtils.generateDate(), 1, false),
        ).map {
            DatabaseFullArcherRoundInfo(
                    archerRound = it,
                    arrows = listOf(),
                    roundArrowCounts = listOf(),
                    allRoundSubTypes = listOf(),
                    allRoundDistances = listOf(),
                    isPersonalBest = false,
                    joinedDate = it.dateShot,
            )
        }

        for (archerRound in archerRounds) {
            archerRoundDao.insert(archerRound.archerRound.copy(archerRoundId = 0))
        }
        assertEquals(
                archerRounds.toSet(),
                retrievedArcherRounds.first().toSet(),
        )

        /*
         * Delete
         */
        archerRoundDao.deleteRound(1)
        archerRoundDao.deleteRound(2)
        assertEquals(
                archerRounds.subList(2, archerRounds.size).toSet(),
                retrievedArcherRounds.first().toSet(),
        )
    }

    /**
     * Check the correct round info is retrieved for the given archer round
     */
    @Test
    fun getRoundInfoTest() = runTest {
        /*
         * Create data and populate tables
         */
        val archerRounds = listOf(
                ArcherRound(1, TestUtils.generateDate(), 1, false, roundId = 1),
                ArcherRound(2, TestUtils.generateDate(), 2, false, roundId = 2),
                ArcherRound(3, TestUtils.generateDate(), 1, false),
                ArcherRound(4, TestUtils.generateDate(), 1, false, roundId = 1),
                ArcherRound(5, TestUtils.generateDate(), 2, false, roundId = 2),
                ArcherRound(6, TestUtils.generateDate(), 1, false),
        )
        val rounds = TestUtils.ROUNDS.take(3)

        for (round in rounds) {
            roundDao.insert(round)
        }
        for (archerRound in archerRounds) {
            archerRoundDao.insert(archerRound)
        }

        /*
         * Check the correct round info is retrieved
         */
        for (archerRound in archerRounds) {
            val retrievedRoundInfo = archerRoundDao.getFullArcherRoundInfo(archerRound.archerRoundId).first()
            assertEquals(archerRound.roundId, retrievedRoundInfo!!.round?.roundId)
            if (archerRound.roundId != null) {
                assertEquals(rounds[archerRound.roundId!! - 1], retrievedRoundInfo.round)
            }
        }
    }

    /**
     * Check the correct round name info is retrieved for the given archer round
     */
    @Test
    fun getArcherRoundsWithNamesTest() = runTest {
        /*
         * Create data and populate tables
         */
        val archerRounds = listOf(
                ArcherRound(1, TestUtils.generateDate(), 1, false, roundId = 1, roundSubTypeId = 1),
                ArcherRound(2, TestUtils.generateDate(), 2, false, roundId = 2),
                ArcherRound(3, TestUtils.generateDate(), 1, false),
                ArcherRound(4, TestUtils.generateDate(), 1, false, roundId = 1, roundSubTypeId = 1),
                ArcherRound(5, TestUtils.generateDate(), 2, false, roundId = 2),
                ArcherRound(6, TestUtils.generateDate(), 1, false),
        )
        val rounds = TestUtils.ROUNDS.take(3)
        val roundSubTypes = TestUtils.ROUND_SUB_TYPES

        for (round in rounds) {
            roundDao.insert(round)
        }
        for (roundSubType in roundSubTypes) {
            roundSubTypeDao.insert(roundSubType)
        }
        for (archerRound in archerRounds) {
            archerRoundDao.insert(archerRound)
        }

        /*
         * Check the correct round info is retrieved
         */
        val retrievedRoundInfo = archerRoundDao.getAllFullArcherRoundInfo().first()
        for (actual in retrievedRoundInfo) {
            val expected = archerRounds[actual.archerRound.archerRoundId - 1]
            assertEquals(expected, actual.archerRound)

            val expectedRoundName = rounds.find { it.roundId == expected.roundId }
            assert(expectedRoundName == actual.round)

            val expectedRoundSubTypeName =
                    roundSubTypes.find {
                        it.roundId == expected.roundId && it.subTypeId == expected.roundSubTypeId
                    }?.name
            assertEquals(expectedRoundSubTypeName, actual.roundSubType?.name)
        }
        assertEquals(archerRounds.size, retrievedRoundInfo.size)
    }

    /**
     * Check that:
     * - [ArcherRound.roundSubTypeId]s of 1 and null are treated the same
     * - If a PB has been matched, all are returned
     * - Incomplete rounds are ignored
     * - Tied PBs are marked correctly
     */
    @Test
    fun testPersonalBests() = runTest {
        val archerRounds = listOf(
                // Incomplete round (max score)
                ArcherRound(1, TestUtils.generateDate(), 1, false, roundId = 1, roundSubTypeId = 1),
                // Different round type (max score)
                ArcherRound(2, TestUtils.generateDate(), 1, false, roundId = 2),
                // Actual PB
                ArcherRound(3, TestUtils.generateDate(), 1, false, roundId = 1),
                // Duplicate PB
                ArcherRound(4, TestUtils.generateDate(), 1, false, roundId = 1, roundSubTypeId = 1),
                // Different round type (non-max score)
                ArcherRound(5, TestUtils.generateDate(), 1, false, roundId = 2),
                // No round
                ArcherRound(6, TestUtils.generateDate(), 1, false),
                // Lower than PB
                ArcherRound(7, TestUtils.generateDate(), 1, false, roundId = 1),
        )
        val rounds = TestUtils.ROUNDS.take(3)
        val arrowCounts = TestUtils.ROUND_ARROW_COUNTS

        val roundOneArrowCount = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == 1 }.sumOf { it.arrowCount }
        val roundTwoArrowCount = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == 2 }.sumOf { it.arrowCount }
        val arrows = listOf(
                List(roundOneArrowCount - 3) { ArrowValue(1, it, 10, false) },
                List(roundTwoArrowCount) { ArrowValue(2, it, 10, false) },
                List(roundOneArrowCount) { ArrowValue(3, it, 5, false) },
                List(roundOneArrowCount) { ArrowValue(4, it, 5, false) },
                List(roundTwoArrowCount) { ArrowValue(5, it, 5, false) },
                List(36) { ArrowValue(6, it, 10, false) },
                List(roundOneArrowCount) { ArrowValue(7, it, 1, false) },
        ).flatten()

        for (round in rounds) {
            roundDao.insert(round)
        }
        for (roundSubType in TestUtils.ROUND_SUB_TYPES) {
            roundSubTypeDao.insert(roundSubType)
        }
        for (roundArrowCount in arrowCounts) {
            roundArrowCountsDao.insert(roundArrowCount)
        }
        for (archerRound in archerRounds) {
            archerRoundDao.insert(archerRound)
        }
        for (arrow in arrows) {
            arrowValueDao.insert(arrow)
        }

        assertEquals(
                setOf(2 to false, 3 to true, 4 to true),
                ArcherRoundsRepo(archerRoundDao)
                        .getFullArcherRoundInfo()
                        .first()
                        .filter { it.isPersonalBest ?: false }
                        .map { it.archerRound.archerRoundId to it.isTiedPersonalBest }
                        .toSet()
        )
    }

    @Test
    fun testFilters() = runTest {
        val archerRounds = listOf(
                ArcherRound(1, TestUtils.generateDate(2011, 3), 1, false, roundId = 1, roundSubTypeId = 1),
                ArcherRound(2, TestUtils.generateDate(2012, 3), 1, false, roundId = 2),
                ArcherRound(3, TestUtils.generateDate(2013, 3), 1, false, roundId = 1),
                ArcherRound(4, TestUtils.generateDate(2014, 3), 1, false, roundId = 1, roundSubTypeId = 1),
                ArcherRound(5, TestUtils.generateDate(2015, 3), 1, false, roundId = 2),
                ArcherRound(6, TestUtils.generateDate(2016, 3), 1, false),
                ArcherRound(7, TestUtils.generateDate(2017, 3), 1, false, roundId = 1, roundSubTypeId = 2),
                ArcherRound(8, TestUtils.generateDate(2017, 3), 1, false, roundId = 1),
        )
        val rounds = TestUtils.ROUNDS.take(3)
        val arrowCounts = TestUtils.ROUND_ARROW_COUNTS

        val roundOneArrowCount = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == 1 }.sumOf { it.arrowCount }
        val roundTwoArrowCount = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == 2 }.sumOf { it.arrowCount }
        val arrows = listOf(
                List(roundOneArrowCount - 3) { ArrowValue(1, it, 10, false) },
                List(roundTwoArrowCount) { ArrowValue(2, it, 10, false) },
                List(roundOneArrowCount) { ArrowValue(3, it, 5, false) },
                List(roundOneArrowCount) { ArrowValue(4, it, 5, false) },
                List(roundTwoArrowCount) { ArrowValue(5, it, 5, false) },
                List(36) { ArrowValue(6, it, 10, false) },
                List(roundOneArrowCount) { ArrowValue(7, it, 1, false) },
                List(roundOneArrowCount) { ArrowValue(8, it, 1, false) },
        ).flatten()

        for (round in rounds) {
            roundDao.insert(round)
        }
        for (roundSubType in TestUtils.ROUND_SUB_TYPES) {
            roundSubTypeDao.insert(roundSubType)
        }
        for (roundArrowCount in arrowCounts) {
            roundArrowCountsDao.insert(roundArrowCount)
        }
        for (archerRound in archerRounds) {
            archerRoundDao.insert(archerRound)
        }
        for (arrow in arrows) {
            arrowValueDao.insert(arrow)
        }

        suspend fun check(expectedIds: Set<Int>, filters: List<ArcherRoundsFilter>) {
            assertEquals(
                    expectedIds,
                    ArcherRoundsRepo(archerRoundDao)
                            .getFullArcherRoundInfo(Filters(filters))
                            .first()
                            .map { it.archerRound.archerRoundId }
                            .toSet()
            )
        }

        // PBs
        check(setOf(2, 3, 4, 7), listOf(ArcherRoundsFilter.PersonalBests))

        // Round
        check(setOf(1, 3, 4, 8), listOf(ArcherRoundsFilter.Round(1, null)))
        check(setOf(1, 3, 4, 8), listOf(ArcherRoundsFilter.Round(1, 1)))
        check(setOf(7), listOf(ArcherRoundsFilter.Round(1, 2)))
        check(setOf(2, 5), listOf(ArcherRoundsFilter.Round(2, null)))

        // Date
        fun getDate(year: Int) = Calendar.getInstance().apply { set(year, 1, 1) }
        check((5..8).toSet(), listOf(ArcherRoundsFilter.DateRange(from = getDate(2015))))
        check((1..4).toSet(), listOf(ArcherRoundsFilter.DateRange(to = getDate(2015))))
        check((2..4).toSet(), listOf(ArcherRoundsFilter.DateRange(from = getDate(2012), to = getDate(2015))))

        // All three
        check(
                setOf(4),
                listOf(
                        ArcherRoundsFilter.DateRange(from = getDate(2014)),
                        ArcherRoundsFilter.Round(1, null),
                        ArcherRoundsFilter.PersonalBests
                ),
        )
    }

    @Test
    fun testGetJoinedArcherRoundIds() = runTest {
        val archerRounds = List(12) {
            ArcherRound(
                    archerRoundId = 1 + it,
                    dateShot = TestUtils.generateDate(2020, 1 + it),
                    archerId = 1,
                    countsTowardsHandicap = true,
                    joinWithPrevious = (it + 1) in 3..5
            )
        }
        archerRounds.forEach { archerRoundDao.insert(it) }

        archerRounds.forEach {
            assertEquals(
                    if (it.archerRoundId in 2..5) (2..5).toList() else listOf(it.archerRoundId),
                    archerRoundDao
                            .getJoinedFullArcherRounds(it.archerRoundId)
                            .first()
                            .map { dbFar -> dbFar.archerRound.archerRoundId }
            )
        }
    }

    @Test
    fun testGetAllFullArcherRoundInfoWithFiltersAndJoinedRounds() = runTest {
        val archerRounds = List(12) {
            ArcherRound(
                    archerRoundId = 1 + it,
                    dateShot = TestUtils.generateDate(2020, 1 + it),
                    archerId = 1,
                    countsTowardsHandicap = true,
                    joinWithPrevious = (it + 1) in 3..5
            )
        }
        archerRounds.forEach { archerRoundDao.insert(it) }

        archerRounds.forEach {
            val start = if (it.archerRoundId in 2..5) 2 else it.archerRoundId
            assertEquals(
                    (start..12).toList(),
                    archerRoundDao
                            .getAllFullArcherRoundInfo(fromDate = it.dateShot)
                            .first()
                            .map { dbFar -> dbFar.archerRound.archerRoundId }
            )
        }
    }
}
