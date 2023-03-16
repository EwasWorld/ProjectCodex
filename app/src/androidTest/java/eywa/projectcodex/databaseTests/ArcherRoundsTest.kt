package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundDao
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
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
            assertEquals(archerRound.roundId, retrievedRoundInfo.round?.roundId)
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
        for (i in retrievedRoundInfo.indices) {
            assertEquals(archerRounds[i], retrievedRoundInfo[i].archerRound)

            val expectedRoundName = rounds.find { it.roundId == archerRounds[i].roundId }
            assert(expectedRoundName == retrievedRoundInfo[i].round)

            val expectedRoundSubTypeName =
                    roundSubTypes.find {
                        it.roundId == archerRounds[i].roundId && it.subTypeId == archerRounds[i].roundSubTypeId
                    }?.name
            assertEquals(expectedRoundSubTypeName, retrievedRoundInfo[i].roundSubType?.name)
        }
    }

    @Test
    fun testPersonalBests() = runTest {
        val archerRounds = listOf(
                ArcherRound(1, TestUtils.generateDate(), 1, false, roundId = 1, roundSubTypeId = 1),
                ArcherRound(2, TestUtils.generateDate(), 1, false, roundId = 2),
                ArcherRound(3, TestUtils.generateDate(), 1, false, roundId = 1),
                ArcherRound(4, TestUtils.generateDate(), 1, false, roundId = 1, roundSubTypeId = 1),
                ArcherRound(5, TestUtils.generateDate(), 1, false, roundId = 2),
                ArcherRound(6, TestUtils.generateDate(), 1, false),
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

        assertEquals(setOf(2, 3), archerRoundDao.getPersonalBests().first().map { it.archerRoundId }.toSet())
    }
}
