package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.arrows.ArrowCounterDao
import eywa.projectcodex.database.arrows.ArrowScoreDao
import eywa.projectcodex.database.arrows.DatabaseArrowCounter
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.RoundArrowCountDao
import eywa.projectcodex.database.rounds.RoundDao
import eywa.projectcodex.database.rounds.RoundSubTypeDao
import eywa.projectcodex.database.shootData.*
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
class ShootsTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase
    private lateinit var shootDao: ShootDao
    private lateinit var shootDetailDao: ShootDetailDao
    private lateinit var shootRoundDao: ShootRoundDao
    private lateinit var roundDao: RoundDao
    private lateinit var roundSubTypeDao: RoundSubTypeDao
    private lateinit var roundArrowCountsDao: RoundArrowCountDao
    private lateinit var arrowScoreDao: ArrowScoreDao
    private lateinit var arrowCounterDao: ArrowCounterDao

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
        shootDao = db.shootDao()
        roundDao = db.roundDao()
        roundSubTypeDao = db.roundSubTypeDao()
        roundArrowCountsDao = db.roundArrowCountDao()
        arrowScoreDao = db.arrowScoreDao()
        shootDetailDao = db.shootDetailDao()
        shootRoundDao = db.shootRoundDao()
        arrowCounterDao = db.arrowCounterDao()
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
        val retrievedShoots = shootDao.getAllFullShootInfo()

        /*
         * Add and retrieve
         */
        val shoots = listOf(
                DatabaseShoot(1, TestUtils.generateDate(), 1, false),
                DatabaseShoot(2, TestUtils.generateDate(), 2, false),
                DatabaseShoot(3, TestUtils.generateDate(), 1, false),
                DatabaseShoot(4, TestUtils.generateDate(), 1, false),
                DatabaseShoot(5, TestUtils.generateDate(), 2, false),
                DatabaseShoot(6, TestUtils.generateDate(), 1, false),
        ).map {
            DatabaseFullShootInfo(
                    shoot = it,
                    arrows = listOf(),
                    roundArrowCounts = listOf(),
                    allRoundSubTypes = listOf(),
                    allRoundDistances = listOf(),
                    isPersonalBest = false,
                    joinedDate = it.dateShot,
            )
        }

        for (shoot in shoots) {
            shootDao.insert(shoot.shoot.copy(shootId = 0))
        }
        assertEquals(
                shoots.toSet(),
                retrievedShoots.first().toSet(),
        )

        /*
         * Delete
         */
        shootDao.deleteRound(1)
        shootDao.deleteRound(2)
        assertEquals(
                shoots.subList(2, shoots.size).toSet(),
                retrievedShoots.first().toSet(),
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
        val shoots = listOf(
                DatabaseShoot(1, TestUtils.generateDate(), 1, false),
                DatabaseShoot(2, TestUtils.generateDate(), 2, false),
                DatabaseShoot(3, TestUtils.generateDate(), 1, false),
                DatabaseShoot(4, TestUtils.generateDate(), 1, false),
                DatabaseShoot(5, TestUtils.generateDate(), 2, false),
                DatabaseShoot(6, TestUtils.generateDate(), 1, false),
        )
        val shootRounds = listOf(
                DatabaseShootRound(1, roundId = 1),
                DatabaseShootRound(2, roundId = 2),
                DatabaseShootRound(4, roundId = 1),
                DatabaseShootRound(5, roundId = 2),
        )
        val rounds = TestUtils.ROUNDS.take(3)

        for (round in rounds) {
            roundDao.insert(round)
        }
        for (shoot in shoots) {
            shootDao.insert(shoot)
        }
        for (shootRound in shootRounds) {
            shootRoundDao.insert(shootRound)
        }

        /*
         * Check the correct round info is retrieved
         */
        for (shoot in shoots) {
            val retrievedRoundInfo = shootDao.getFullShootInfo(shoot.shootId).first()
            val expectedShootRound = shootRounds.find { it.shootId == shoot.shootId }
            assertEquals(expectedShootRound?.roundId, retrievedRoundInfo!!.round?.roundId)
            if (expectedShootRound?.roundId != null) {
                assertEquals(rounds[expectedShootRound.roundId - 1], retrievedRoundInfo.round)
            }
        }
    }

    /**
     * Check the correct round name info is retrieved for the given archer round
     */
    @Test
    fun getShootsWithNamesTest() = runTest {
        /*
         * Create data and populate tables
         */
        val shoots = listOf(
                DatabaseShoot(1, TestUtils.generateDate(), 1, false),
                DatabaseShoot(2, TestUtils.generateDate(), 2, false),
                DatabaseShoot(3, TestUtils.generateDate(), 1, false),
                DatabaseShoot(4, TestUtils.generateDate(), 1, false),
                DatabaseShoot(5, TestUtils.generateDate(), 2, false),
                DatabaseShoot(6, TestUtils.generateDate(), 1, false),
        )
        val shootRounds = listOf(
                DatabaseShootRound(1, roundId = 1, roundSubTypeId = 1),
                DatabaseShootRound(2, roundId = 2),
                DatabaseShootRound(4, roundId = 1, roundSubTypeId = 1),
                DatabaseShootRound(5, roundId = 2),
        )
        val rounds = TestUtils.ROUNDS.take(3)
        val roundSubTypes = TestUtils.ROUND_SUB_TYPES

        for (round in rounds) {
            roundDao.insert(round)
        }
        for (roundSubType in roundSubTypes) {
            roundSubTypeDao.insert(roundSubType)
        }
        for (shoot in shoots) {
            shootDao.insert(shoot)
        }
        for (shootRound in shootRounds) {
            shootRoundDao.insert(shootRound)
        }

        /*
         * Check the correct round info is retrieved
         */
        val retrievedRoundInfo = shootDao.getAllFullShootInfo().first()
        for (actual in retrievedRoundInfo) {
            val expectedShootRound = shootRounds.find { it.shootId == actual.shoot.shootId }
            val expected = shoots[actual.shoot.shootId - 1]
            assertEquals(expected, actual.shoot)

            val expectedRoundName = rounds.find { it.roundId == expectedShootRound?.roundId }
            assert(expectedRoundName == actual.round)

            val expectedRoundSubTypeName =
                    roundSubTypes.find {
                        it.roundId == expectedShootRound?.roundId && it.subTypeId == expectedShootRound.roundSubTypeId
                    }?.name
            assertEquals(expectedRoundSubTypeName, actual.roundSubType?.name)
        }
        assertEquals(shoots.size, retrievedRoundInfo.size)
    }

    /**
     * Check that:
     * - [DatabaseShootRound.roundSubTypeId]s of 1 and null are treated the same
     * - If a PB has been matched, all are returned
     * - Incomplete rounds are ignored
     * - Tied PBs are marked correctly
     */
    @Test
    fun testPersonalBests() = runTest {
        val shoots = listOf(
                // Incomplete round (max score)
                DatabaseShoot(1, TestUtils.generateDate(), 1, false),
                // Different round type (max score)
                DatabaseShoot(2, TestUtils.generateDate(), 1, false),
                // Actual PB
                DatabaseShoot(3, TestUtils.generateDate(), 1, false),
                // Duplicate PB
                DatabaseShoot(4, TestUtils.generateDate(), 1, false),
                // Different round type (non-max score)
                DatabaseShoot(5, TestUtils.generateDate(), 1, false),
                // No round
                DatabaseShoot(6, TestUtils.generateDate(), 1, false),
                // Lower than PB
                DatabaseShoot(7, TestUtils.generateDate(), 1, false),
        )
        val shootRounds = listOf(
                DatabaseShootRound(1, roundId = 1, roundSubTypeId = 1),
                DatabaseShootRound(2, roundId = 2),
                DatabaseShootRound(3, roundId = 1),
                DatabaseShootRound(4, roundId = 1, roundSubTypeId = 1),
                DatabaseShootRound(5, roundId = 2),
                DatabaseShootRound(7, roundId = 1),
        )
        val rounds = TestUtils.ROUNDS.take(3)
        val arrowCounts = TestUtils.ROUND_ARROW_COUNTS

        val roundOneArrowCount = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == 1 }.sumOf { it.arrowCount }
        val roundTwoArrowCount = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == 2 }.sumOf { it.arrowCount }
        val arrows = listOf(
                List(roundOneArrowCount - 3) { DatabaseArrowScore(1, it, 10, false) },
                List(roundTwoArrowCount) { DatabaseArrowScore(2, it, 10, false) },
                List(roundOneArrowCount) { DatabaseArrowScore(3, it, 5, false) },
                List(roundOneArrowCount) { DatabaseArrowScore(4, it, 5, false) },
                List(roundTwoArrowCount) { DatabaseArrowScore(5, it, 5, false) },
                List(36) { DatabaseArrowScore(6, it, 10, false) },
                List(roundOneArrowCount) { DatabaseArrowScore(7, it, 1, false) },
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
        for (shoot in shoots) {
            shootDao.insert(shoot)
        }
        for (arrow in arrows) {
            arrowScoreDao.insert(arrow)
        }
        for (shootRound in shootRounds) {
            shootRoundDao.insert(shootRound)
        }

        assertEquals(
                setOf(2 to false, 3 to true, 4 to true),
                ShootsRepo(shootDao, shootDetailDao, shootRoundDao)
                        .getFullShootInfo()
                        .first()
                        .filter { it.isPersonalBest ?: false }
                        .map { it.shoot.shootId to it.isTiedPersonalBest }
                        .toSet()
        )
    }

    @Test
    fun testFilters() = runTest {
        val shoots = listOf(
                DatabaseShoot(1, TestUtils.generateDate(2011, 3), 1, false),
                DatabaseShoot(2, TestUtils.generateDate(2012, 3), 1, false),
                DatabaseShoot(3, TestUtils.generateDate(2013, 3), 1, false),
                DatabaseShoot(4, TestUtils.generateDate(2014, 3), 1, false),
                DatabaseShoot(5, TestUtils.generateDate(2015, 3), 1, false),
                DatabaseShoot(6, TestUtils.generateDate(2016, 3), 1, false),
                DatabaseShoot(7, TestUtils.generateDate(2017, 3), 1, false),
                DatabaseShoot(8, TestUtils.generateDate(2017, 3), 1, false),
        )
        val shootRounds = listOf(
                DatabaseShootRound(1, roundId = 1, roundSubTypeId = 1),
                DatabaseShootRound(2, roundId = 2),
                DatabaseShootRound(3, roundId = 1),
                DatabaseShootRound(4, roundId = 1, roundSubTypeId = 1),
                DatabaseShootRound(5, roundId = 2),
                DatabaseShootRound(7, roundId = 1, roundSubTypeId = 2),
                DatabaseShootRound(8, roundId = 1),
        )
        val rounds = TestUtils.ROUNDS.take(3)
        val arrowCounts = TestUtils.ROUND_ARROW_COUNTS

        val roundOneArrowCount = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == 1 }.sumOf { it.arrowCount }
        val roundTwoArrowCount = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == 2 }.sumOf { it.arrowCount }
        val arrows = listOf(
                List(roundOneArrowCount - 3) { DatabaseArrowScore(1, it, 10, false) },
                List(roundTwoArrowCount) { DatabaseArrowScore(2, it, 10, false) },
                List(roundOneArrowCount) { DatabaseArrowScore(3, it, 5, false) },
                List(roundOneArrowCount) { DatabaseArrowScore(4, it, 5, false) },
                List(roundTwoArrowCount) { DatabaseArrowScore(5, it, 5, false) },
                List(36) { DatabaseArrowScore(6, it, 10, false) },
                List(roundOneArrowCount) { DatabaseArrowScore(7, it, 1, false) },
                List(roundOneArrowCount) { DatabaseArrowScore(8, it, 1, false) },
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
        for (shoot in shoots) {
            shootDao.insert(shoot)
        }
        for (arrow in arrows) {
            arrowScoreDao.insert(arrow)
        }
        for (shootRound in shootRounds) {
            shootRoundDao.insert(shootRound)
        }

        suspend fun check(expectedIds: Set<Int>, filters: List<ShootFilter>) {
            assertEquals(
                    expectedIds,
                    ShootsRepo(shootDao, shootDetailDao, shootRoundDao)
                            .getFullShootInfo(Filters(filters))
                            .first()
                            .map { it.shoot.shootId }
                            .toSet()
            )
        }

        // PBs
        check(setOf(2, 3, 4, 7), listOf(ShootFilter.PersonalBests))

        // Round
        check(setOf(1, 3, 4, 8), listOf(ShootFilter.Round(1, null)))
        check(setOf(1, 3, 4, 8), listOf(ShootFilter.Round(1, 1)))
        check(setOf(7), listOf(ShootFilter.Round(1, 2)))
        check(setOf(2, 5), listOf(ShootFilter.Round(2, null)))

        // Date
        fun getDate(year: Int) = Calendar.getInstance().apply { set(year, 1, 1) }
        check((5..8).toSet(), listOf(ShootFilter.DateRange(from = getDate(2015))))
        check((1..4).toSet(), listOf(ShootFilter.DateRange(to = getDate(2015))))
        check((2..4).toSet(), listOf(ShootFilter.DateRange(from = getDate(2012), to = getDate(2015))))

        // All three
        check(
                setOf(4),
                listOf(
                        ShootFilter.DateRange(from = getDate(2014)),
                        ShootFilter.Round(1, null),
                        ShootFilter.PersonalBests
                ),
        )
    }

    @Test
    fun testGetJoinedShootIds() = runTest {
        val shoots = List(12) {
            DatabaseShoot(
                    shootId = 1 + it,
                    dateShot = TestUtils.generateDate(2020, 1 + it),
                    archerId = 1,
                    countsTowardsHandicap = true,
                    joinWithPrevious = (it + 1) in 3..5
            )
        }
        shoots.forEach { shootDao.insert(it) }

        shoots.forEach {
            assertEquals(
                    if (it.shootId in 2..5) (2..5).toList() else listOf(it.shootId),
                    shootDao
                            .getJoinedFullShoots(it.shootId)
                            .first()
                            .map { dbFar -> dbFar.shoot.shootId }
            )
        }
    }

    @Test
    fun testGetAllFullShootInfoWithFiltersAndJoinedRounds() = runTest {
        val shoots = List(12) {
            DatabaseShoot(
                    shootId = 1 + it,
                    dateShot = TestUtils.generateDate(2020, 1 + it),
                    archerId = 1,
                    countsTowardsHandicap = true,
                    joinWithPrevious = (it + 1) in 3..5
            )
        }
        shoots.forEach { shootDao.insert(it) }

        shoots.forEach {
            val start = if (it.shootId in 2..5) 2 else it.shootId
            assertEquals(
                    (start..12).toList(),
                    shootDao
                            .getAllFullShootInfo(fromDate = it.dateShot)
                            .first()
                            .map { dbFar -> dbFar.shoot.shootId }
            )
        }
    }

    @Test
    fun testWithArrowCounts() = runTest {
        val shoot = DatabaseShoot(
                shootId = 1,
                dateShot = TestUtils.generateDate(),
                archerId = 1,
                countsTowardsHandicap = true,
        )
        shootDao.insert(shoot)
        val arrowCount = DatabaseArrowCounter(
                shootId = 1,
                12,
        )
        arrowCounterDao.insert(arrowCount)


        assertEquals(
                arrowCount,
                shootDao
                        .getFullShootInfo(1)
                        .first()!!
                        .arrowCount
        )
    }
}