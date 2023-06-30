package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.R
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.util.*


@OptIn(ExperimentalCoroutinesApi::class)
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
    private var roundsInput = TestUtils.ROUNDS.take(3)
    private val subtypesInput = TestUtils.ROUND_SUB_TYPES
    private val arrowCountsInput = TestUtils.ROUND_ARROW_COUNTS
    private val archerRoundInput = ArcherRound(
            1,
            Date(2019, 5, 10, 17, 12, 13).asCalendar(),
//            Calendar.Builder().setDate(2019, 5, 10).setTimeOfDay(17, 12, 13).build().time,
            1,
            true,
            roundId = 1,
            roundSubTypeId = 2
    )

    private val distancesInput = TestUtils.ROUND_DISTANCES

    private suspend fun getCurrentArcherRounds() = db.archerRoundDao().getAllFullArcherRoundInfo().first()

    private suspend fun getArcherRounds(archerRoundId: Int) =
            db.archerRoundDao().getFullArcherRoundInfo(archerRoundId).first()

    /**
     * Set up [scenario] with desired fragment in the resumed state, [navController] to allow transitions, and [db]
     * with all desired information
     */
    private fun setup() {
        hiltRule.inject()
        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!

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
            // TODO_CURRENT Fix get nav controller
            // https://developer.android.com/codelabs/basic-android-kotlin-compose-test-cupcake#3
            // https://stackoverflow.com/questions/75644786/jetpack-compose-how-to-test-navigation
//            navController = it.navHostFragment.navController
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
    fun addRoundNoType() = runTest {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore()
            }
        }

        runBlocking { delay(1000) }
        val currentArcherRounds = getCurrentArcherRounds()
        assertEquals(2, currentArcherRounds.size)
        assertEquals(2, currentArcherRounds[1].archerRound.archerRoundId)
        assertEquals(null, currentArcherRounds[1].round?.roundId)
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun addAnotherRound() = runTest {
        setup()
        val ar = ArcherRound(2, TestUtils.generateDate(), 1, true)
        db.archerRoundDao().insert(ar)

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore()
            }
        }

        runBlocking { delay(1000) }
        val roundsAfterCreate = getCurrentArcherRounds()
                .filterNot { it.archerRound.archerRoundId == ar.archerRoundId }
                .map { it.archerRound }
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(archerRoundInput, roundsAfterCreate[0])
        assert(roundsAfterCreate[1].archerRoundId > ar.archerRoundId)
        assertEquals(null, roundsAfterCreate[1].roundId)
        assertEquals(null, roundsAfterCreate[1].roundSubTypeId)

        assertEquals(R.id.archerRoundFragment, navController.currentDestination?.id)
        assertEquals(3, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    @Test
    fun testNoRoundButton() = runTest {
        roundsInput = List(20) { Round(it + 1, "$it", "$it", false, false) }
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSelectedRound()
                clickRoundDialogRound("1")
                checkSelectedRound("1")

                clickSelectedRound()
                clickRoundDialogNoRound()
                checkSelectedRound("No Round")
            }
        }
    }

    /**
     * Test row added to archer_round
     * Test round and subtype id are correct
     */
    @Test
    fun addRoundWithSubtype() = runTest {
        setup()
        db.archerRoundDao().getAllFullArcherRoundInfo().takeWhile { it.isEmpty() }.collect()

        val roundsBeforeCreate = getCurrentArcherRounds()
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
        val roundsAfterCreate = getCurrentArcherRounds().map { it.archerRound }
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(archerRoundInput, roundsAfterCreate[0])
        assertEquals(selectedRound.roundId, roundsAfterCreate[1].roundId)
        assertEquals(selectedSubtype.subTypeId, roundsAfterCreate[1].roundSubTypeId)
    }

    @Test
    fun testCustomDateTime() = runTest {
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
        val roundsAfterCreate = getCurrentArcherRounds().toMutableList()
        for (round in roundsAfterCreate) {
            if (round.archerRound.roundId == 1) continue
            assertEquals(2040, round.archerRound.dateShot.get(Calendar.YEAR))
        }
    }

    @Test
    fun testEditInfo() = runTest {
        setup()

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
        }

        runBlocking { delay(1000) }
        val updated = getArcherRounds(archerRoundInput.archerRoundId)
        assertEquals(
                ArcherRound(
                        archerRoundInput.archerRoundId,
                        calendar,
                        archerRoundInput.archerId,
                        archerRoundInput.countsTowardsHandicap,
                        roundId = selectedRound.roundId,
                        roundSubTypeId = 1
                ),
                updated!!.archerRound.copy(dateShot = calendar)
        )
        val updatedDate = updated.archerRound.dateShot
        assertEquals(2040, updatedDate.get(Calendar.YEAR))
        assertEquals(9, updatedDate.get(Calendar.MONTH))
        assertEquals(30, updatedDate.get(Calendar.DATE))
        assertEquals(13, updatedDate.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, updatedDate.get(Calendar.MINUTE))
    }

    @Test
    fun testEditInfoToNoRound() = runTest {
        setup()

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
        val actual = getArcherRounds(archerRoundInput.archerRoundId)!!.archerRound
        assertEquals(
                ArcherRound(
                        archerRoundInput.archerRoundId,
                        actual.dateShot,
                        archerRoundInput.archerId,
                        archerRoundInput.countsTowardsHandicap,
                        roundId = null,
                        roundSubTypeId = null
                ),
                actual
        )
        assertEquals(archerRoundInput.dateShot.get(Calendar.YEAR), actual.dateShot.get(Calendar.YEAR))
        assertEquals(archerRoundInput.dateShot.get(Calendar.MONTH), actual.dateShot.get(Calendar.MONTH))
        assertEquals(archerRoundInput.dateShot.get(Calendar.DATE), actual.dateShot.get(Calendar.DATE))
        assertEquals(archerRoundInput.dateShot.get(Calendar.HOUR_OF_DAY), actual.dateShot.get(Calendar.HOUR_OF_DAY))
        assertEquals(archerRoundInput.dateShot.get(Calendar.MINUTE), actual.dateShot.get(Calendar.MINUTE))
    }
}
