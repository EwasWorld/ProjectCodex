package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatastoreModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.util.*

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ArcherRoundStatsInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(60)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController
    private lateinit var db: ScoresRoomDatabase

    private lateinit var arrows: List<ArrowValue>
    private val arrowsPerArrowCount = 12
    private val roundsInput = listOf(
            Round(1, "round1", "Round1", true, false),
            Round(2, "round2", "Round2", true, false)
    )
    private val arrowCountsInput = listOf(
            RoundArrowCount(1, 1, 122f, arrowsPerArrowCount),
            RoundArrowCount(1, 2, 122f, arrowsPerArrowCount),
            RoundArrowCount(2, 1, 122f, arrowsPerArrowCount),
            RoundArrowCount(2, 2, 122f, arrowsPerArrowCount)
    )
    private val distancesInput = listOf(
            RoundDistance(1, 1, 1, 60),
            RoundDistance(1, 2, 1, 50),
            RoundDistance(2, 1, 1, 60),
            RoundDistance(2, 2, 1, 50),
            RoundDistance(2, 1, 2, 30),
            RoundDistance(2, 2, 2, 20)
    )
    private val subTypesInput = listOf(
            RoundSubType(2, 1, "Sub Type 1"),
            RoundSubType(2, 2, "Sub Type 2")
    )
    private val archerRounds = listOf(
            ArcherRound(
                    1,
                    Date(2014, 6, 17, 15, 21, 37).asCalendar(),
//                    Calendar.Builder().setDate(2014, 6, 17).setTimeOfDay(15, 21, 37).build().time,
                    1,
                    true
            ),
            ArcherRound(2, TestUtils.generateDate(2013), 1, true, roundId = 1),
            ArcherRound(3, TestUtils.generateDate(2012), 1, true, roundId = 2, roundSubTypeId = 1)
    )

    /**
     * Set up [scenario] with desired fragment in the resumed state, [navController] to allow transitions, and [db]
     * with all desired information
     */
    private fun setup() {
        hiltRule.inject()

        // Start initialised so we can add to the database before the onCreate methods are called
        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity { activity ->
            db = LocalDatabaseModule.scoresRoomDatabase!!
            navController = activity.navHostFragment.navController

            /*
             * Fill default rounds
             */
            runBlocking {
                roundsInput.forEach { db.roundDao().insert(it) }
                arrowCountsInput.forEach { db.roundArrowCountDao().insert(it) }
                subTypesInput.forEach { db.roundSubTypeDao().insert(it) }
                distancesInput.forEach { db.roundDistanceDao().insert(it) }
                archerRounds.forEach { db.archerRoundDao().insert(it) }
                arrows.forEach { db.arrowValueDao().insert(it) }
            }
        }

        CustomConditionWaiter.waitFor(500)
    }

    @After
    fun teardown() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testAllStatsNoRound() {
        val archerRoundId = archerRounds[ArcherRoundTypes.NO_ROUND.row].archerRoundId
        check(archerRounds.find { it.archerRoundId == archerRoundId } != null) { "Invalid archer round ID" }

        var arrowNumber = 1
        arrows = listOf(
                List(6) { TestUtils.ARROWS[10].toArrowValue(archerRoundId, arrowNumber++) },
                List(38) { TestUtils.ARROWS[5].toArrowValue(archerRoundId, arrowNumber++) },
                List(4) { TestUtils.ARROWS[0].toArrowValue(archerRoundId, arrowNumber++) }
        ).flatten()

        val expectedScore = 38 * 5 + 6 * 10

        setup()
        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(ArcherRoundTypes.NO_ROUND.row) {
                    waitForLoad()
                    clickNavBarStats {
                        checkDate("17 Jul 14 15:21")
                        checkHits("44 (of 48)")
                        checkScore(expectedScore)
                        checkGolds(6)
                        checkNoRound()
                        checkNoRemainingArrows()
                        checkNoHandicap()
                        checkNoPredictedScore()
                    }
                }
            }
        }
    }

    @Test
    fun testHasRound() {
        val archerRoundId = archerRounds[ArcherRoundTypes.ROUND.row].archerRoundId
        val archerRound = archerRounds.find { it.archerRoundId == archerRoundId }!!
        val round = roundsInput.find { it.roundId == archerRound.roundId }!!

        var arrowNumber = 1
        arrows = List(arrowsPerArrowCount) { TestUtils.ARROWS[8].toArrowValue(archerRoundId, arrowNumber++) }
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(ArcherRoundTypes.ROUND.row) {
                    waitForLoad()
                    clickNavBarStats {
                        checkRound(round.displayName)
                        checkRemainingArrows(arrowsPerArrowCount)
                        // Checked these values in the handicap tables (2023) - double and use score for 2 doz as only
                        // the first distance has been shot so this is what's being use to calculate the handicap
                        checkHandicap(36)
                        // divide by 2 because only one dozen was shot
                        checkPredictedScore((192 + 201) / 2)
                    }
                }
            }
        }
    }

    @Test
    fun testOldHandicapSystem() {
        LocalDatastoreModule.datastore.setValues(mapOf(DatastoreKey.Use2023HandicapSystem to false))

        val archerRoundId = archerRounds[ArcherRoundTypes.ROUND.row].archerRoundId
        val archerRound = archerRounds.find { it.archerRoundId == archerRoundId }!!
        val round = roundsInput.find { it.roundId == archerRound.roundId }!!

        var arrowNumber = 1
        arrows = List(arrowsPerArrowCount) { TestUtils.ARROWS[8].toArrowValue(archerRoundId, arrowNumber++) }
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(ArcherRoundTypes.ROUND.row) {
                    waitForLoad()
                    clickNavBarStats {
                        checkRound(round.displayName)
                        checkRemainingArrows(arrowsPerArrowCount)
                        // Checked these values in the handicap tables (1998) - double and use score for 2 doz as only
                        // the first distance has been shot so this is what's being use to calculate the handicap
                        checkHandicap(32)
                        // divide by 2 because only one dozen was shot
                        checkPredictedScore((192 + 201) / 2)
                    }
                }
            }
        }
    }

    @Test
    fun testRoundWithSubTypeEmptyScore() {
        val archerRoundId = archerRounds[ArcherRoundTypes.SUBTYPE.row].archerRoundId
        var arrowNumber = 1
        arrows = List(arrowsPerArrowCount) { TestUtils.ARROWS[8].toArrowValue(archerRoundId, arrowNumber++) }
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(ArcherRoundTypes.SUBTYPE.row) {
                    waitForLoad()
                    clickNavBarStats {
                        checkRound(subTypesInput[0].name!!)
                        checkRemainingArrows(arrowsPerArrowCount)
                        // Checked these values in the handicap tables (2023) - double and use score for 2 doz as only
                        // the first distance has been shot so this is what's being use to calculate the handicap
                        checkHandicap(36)
                        // divide by 2 because only one dozen was shot
                        checkPredictedScore((192 + 201) / 2)
                    }
                }
            }
        }
    }

    private enum class ArcherRoundTypes(val row: Int) {
        NO_ROUND(0), ROUND(1), SUBTYPE(2)
    }
}
