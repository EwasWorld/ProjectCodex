package eywa.projectcodex.instrumentedTests

import android.os.Bundle
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.R
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.hiltModules.LocalDatabaseDaggerModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.util.*


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NewScoreInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(60)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController
    private lateinit var db: ScoresRoomDatabase
    private var currentArcherRounds: List<ArcherRound> = listOf()
    private val roundsInput = TestUtils.ROUNDS.take(3)
    private val subtypesInput = TestUtils.ROUND_SUB_TYPES
    private val arrowCountsInput = TestUtils.ROUND_ARROW_COUNTS
    private val archerRoundInput = ArcherRound(
            1,
            Calendar.Builder().setDate(2019, 5, 10).setTimeOfDay(17, 12, 13).build().time,
            1,
            true,
            roundId = 1,
            roundSubTypeId = 2
    )

    private val distancesInput = TestUtils.ROUND_DISTANCES

    /**
     * Set up [scenario] with desired fragment in the resumed state, [navController] to allow transitions, and [db]
     * with all desired information
     */
    private fun setup(archerRoundId: Int = -1) {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        val args = Bundle()
        args.putInt("archerRoundId", archerRoundId)

        hiltRule.inject()
        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseDaggerModule.scoresRoomDatabase!!

            /*
             * Fill default rounds
             */
            runBlocking {
                roundsInput.forEach {
                    db.roundDao().insert(it)
                }
                subtypesInput.forEach {
                    db.roundSubTypeDao().insert(it)
                }
                arrowCountsInput.forEach {
                    db.roundArrowCountDao().insert(it)
                }
                distancesInput.forEach {
                    db.roundDistanceDao().insert(it)
                }
                db.archerRoundDao().insert(archerRoundInput)
            }
        }


        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            navController = it.navHostFragment.navController

            db.archerRoundDao().getAllArcherRounds().observe(it) { obArcherRounds ->
                // TODO Remove and use runTestBlocking or something
                currentArcherRounds = obArcherRounds
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun addRoundNoType() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore()
            }
        }

        runBlocking { delay(1000) }
        assertEquals(2, currentArcherRounds.size)
        assertEquals(2, currentArcherRounds[1].archerRoundId)
        assertEquals(null, currentArcherRounds[1].roundId)
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun addAnotherRound() {
        setup()
        val ar = ArcherRound(2, TestUtils.generateDate(), 1, true)
        scenario.onActivity {
            runBlocking {
                db.archerRoundDao().insert(ar)
            }
        }

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore()
            }
        }

        runBlocking { delay(1000) }
        val roundsAfterCreate = currentArcherRounds.toMutableList()
        assertEquals(3, roundsAfterCreate.size)

        roundsAfterCreate.remove(ar)
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(archerRoundInput, roundsAfterCreate[0])
        assert(roundsAfterCreate[1].archerRoundId > ar.archerRoundId)
        assertEquals(null, roundsAfterCreate[1].roundId)
        assertEquals(null, roundsAfterCreate[1].roundSubTypeId)

        assertEquals(R.id.archerRoundFragment, navController.currentDestination?.id)
        assertEquals(3, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    /**
     * Test row added to archer_round
     * Test round and subtype id are correct
     */
    @Test
    fun addRoundWithSubtype() {
        setup()
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "Wait for currentArcherRounds to be populated"
            }

            override fun checkCondition(): Boolean {
                return currentArcherRounds.isNotEmpty()
            }
        })
        val roundsBeforeCreate = currentArcherRounds
        assertEquals(1, roundsBeforeCreate.size)

        val selectedRound = roundsInput[0]
        val selectedSubtype = subtypesInput.filter { it.roundId == selectedRound.roundId }[1]
        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSelectedRound()
                clickRoundDialogRound(selectedRound.displayName)

                clickSelectedSubtype()
                clickSubtypeDialogSubtype(selectedSubtype.name!!)

                clickSubmitNewScore()
            }
        }

        runBlocking { delay(1000) }
        val roundsAfterCreate = currentArcherRounds.toMutableList()
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(archerRoundInput, roundsAfterCreate[0])
        assertEquals(selectedRound.roundId, roundsAfterCreate[1].roundId)
        assertEquals(selectedSubtype.subTypeId, roundsAfterCreate[1].roundSubTypeId)
    }

    @Test
    fun testCustomDateTime() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                setTime(20, 22)
                checkTime("20:22")

                setDate(30, 9, 2040)
                checkTime("20:22")
                checkDate("30 Oct 40")

                clickSubmitNewScore {
                    clickNavBarStats {
                        checkDate("30 Oct 40 20:22")
                    }
                }
            }
        }

        runBlocking { delay(1000) }
        val roundsAfterCreate = currentArcherRounds.toMutableList()
        for (round in roundsAfterCreate) {
            if (round.roundId == 1) continue
            // Date returns year -1900
            val dateShot = Calendar.Builder().setInstant(round.dateShot).build()
            assertEquals(2040, dateShot.get(Calendar.YEAR))
        }
    }

    @Test
    fun testEditInfo() {
        setup(archerRoundInput.archerRoundId)

        val selectedRound = roundsInput[1]
        val calendar = Calendar.getInstance()
        calendar.set(2040, 9, 30, 13, 15, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        composeTestRule.mainMenuRobot {
            clickViewScores {
                longClickRow(0)
                clickEditDropdownMenuItem {
                    runBlocking { delay(1000) }

                    checkTime("17:12")
                    checkDate("10 Jun 19")

                    checkSelectedRound(roundsInput.find { it.roundId == archerRoundInput.roundId }!!.displayName)

                    /*
                     * Change some stuff
                     */
                    setTime(calendar)
                    setDate(calendar)
                    checkTime("13:15")
                    checkDate("30 Oct 40")

                    clickSelectedRound()
                    clickRoundDialogRound(selectedRound.displayName)

                    /*
                     * Reset
                     */
                    clickReset()
                    checkTime("17:12")
                    checkDate("10 Jun 19")

                    /*
                     * Change again
                     */
                    setTime(calendar)
                    setDate(calendar)
                    checkTime("13:15")
                    checkDate("30 Oct 40")

                    clickSelectedRound()
                    clickRoundDialogRound(selectedRound.displayName)

                    /*
                     * Save
                     */
                    clickSubmitEditScore()
                }
            }

            runBlocking { delay(1000) }
            val updated = currentArcherRounds.find { it.archerRoundId == archerRoundInput.archerRoundId }
            assertEquals(
                    ArcherRound(
                            archerRoundInput.archerRoundId,
                            calendar.time,
                            archerRoundInput.archerId,
                            archerRoundInput.countsTowardsHandicap,
                            roundId = selectedRound.roundId,
                            roundSubTypeId = 1
                    ),
                    updated?.copy(dateShot = calendar.time)
            )
            assertEquals(2040 - 1900, updated?.dateShot?.year)
            assertEquals(9, updated?.dateShot?.month)
            assertEquals(30, updated?.dateShot?.date)
            assertEquals(13, updated?.dateShot?.hours)
            assertEquals(15, updated?.dateShot?.minutes)
        }
    }

    @Test
    fun testEditInfoToNoRound() {
        setup(archerRoundInput.archerRoundId)

        composeTestRule.mainMenuRobot {
            clickViewScores {
                longClickRow(0)
                clickEditDropdownMenuItem {
                    clickSelectedRound()
                    clickRoundDialogNoRound()
                    clickSubmitEditScore()
                }
            }
        }

        runBlocking { delay(1000) }
        assertEquals(
                ArcherRound(
                        archerRoundInput.archerRoundId,
                        archerRoundInput.dateShot,
                        archerRoundInput.archerId,
                        archerRoundInput.countsTowardsHandicap,
                        roundId = null,
                        roundSubTypeId = null
                ),
                currentArcherRounds.find { it.archerRoundId == archerRoundInput.archerRoundId }
        )
    }
}
