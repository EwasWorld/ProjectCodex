package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
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
    val testTimeout: Timeout = Timeout.seconds(1060)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private var roundsInput = TestUtils.ROUNDS.take(3)
    private val subtypesInput = TestUtils.ROUND_SUB_TYPES
    private val arrowCountsInput = TestUtils.ROUND_ARROW_COUNTS
    private val shootInput = DatabaseShoot(
            1,
            Date(2019, 5, 10, 17, 12, 13).asCalendar(),
//            Calendar.Builder().setDate(2019, 5, 10).setTimeOfDay(17, 12, 13).build().time,
            1,
            true,
    )
    private val shootRoundInput = DatabaseShootRound(
            shootId = 1,
            roundId = 1,
            roundSubTypeId = 2,
    )

    private val distancesInput = TestUtils.ROUND_DISTANCES

    private suspend fun getCurrentArcherRounds() = db.shootDao().getAllFullShootInfo().first()

    private suspend fun getArcherRounds(archerRoundId: Int) =
            db.shootDao().getFullShootInfo(archerRoundId).first()

    /**
     * Set up [scenario] with desired fragment in the resumed state, and [db] with all desired information
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
                roundsInput.forEach { db.roundDao().insert(it.copy(isMetric = true)) }
                subtypesInput.forEach { db.roundSubTypeDao().insert(it) }
                arrowCountsInput.forEach { db.roundArrowCountDao().insert(it) }
                distancesInput.forEach { db.roundDistanceDao().insert(it) }
                db.shootDao().insert(shootInput)
                db.shootRoundDao().insert(shootRoundInput)
            }
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
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
        assertEquals(2, currentArcherRounds[1].shoot.shootId)
        assertEquals(null, currentArcherRounds[1].round?.roundId)
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun addAnotherRound() = runTest {
        setup()
        val ar = DatabaseShoot(2, TestUtils.generateDate(2020), 1, true)
        db.shootDao().insert(ar)

        db.arrowScoreDao().insert(
                *List(2) { DatabaseArrowScore(it + 1, 1, 6 + it, false) }.toTypedArray()
        )

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)
                waitForHsg(0, "1/6/0")
                waitForHsg(1, "1/7/0")
                pressBack()
            }

            clickNewScore {
                clickSubmitNewScore {
                    checkIndicatorTable(0, 0)
                }
            }
        }

        runBlocking { delay(1000) }
        val roundsAfterCreate = getCurrentArcherRounds()
                .filterNot { it.shoot.shootId == ar.shootId }
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(shootInput, roundsAfterCreate[0].shoot)
        assert(roundsAfterCreate[1].shoot.shootId > ar.shootId)
        assertEquals(null, roundsAfterCreate[1].shootRound?.roundId)
        assertEquals(null, roundsAfterCreate[1].shootRound?.roundSubTypeId)
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
        db.shootDao().getAllFullShootInfo().takeWhile { it.isEmpty() }.collect()

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
        val roundsAfterCreate = getCurrentArcherRounds()
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(shootInput, roundsAfterCreate[0].shoot)
        assertEquals(selectedRound.roundId, roundsAfterCreate[1].shootRound?.roundId)
        assertEquals(selectedSubtype.subTypeId, roundsAfterCreate[1].shootRound?.roundSubTypeId)
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
            if (round.shootRound?.roundId == 1) continue
            assertEquals(2040, round.shoot.dateShot.get(Calendar.YEAR))
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

                    checkSelectedRound(roundsInput.find { it.roundId == shootRoundInput.roundId }!!.displayName)

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
        val updated = getArcherRounds(shootInput.shootId)
        assertEquals(
                DatabaseShoot(
                        shootInput.shootId,
                        calendar,
                        shootInput.archerId,
                        shootInput.countsTowardsHandicap,
                ),
                updated!!.shoot.copy(dateShot = calendar)
        )
        assertEquals(
                DatabaseShootRound(
                        shootId = shootInput.shootId,
                        roundId = selectedRound.roundId,
                        roundSubTypeId = 1,
                ),
                updated.shootRound,
        )
        val updatedDate = updated.shoot.dateShot
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
        val actual = getArcherRounds(shootInput.shootId)!!
        val actualDateShot = actual.shoot.dateShot
        assertEquals(
                DatabaseShoot(
                        shootId = shootInput.shootId,
                        dateShot = actualDateShot,
                        archerId = shootInput.archerId,
                        countsTowardsHandicap = shootInput.countsTowardsHandicap,
                ),
                actual.shoot,
        )
        assertEquals(
                null,
                actual.shootRound,
        )
        assertEquals(shootInput.dateShot.get(Calendar.YEAR), actualDateShot.get(Calendar.YEAR))
        assertEquals(shootInput.dateShot.get(Calendar.MONTH), actualDateShot.get(Calendar.MONTH))
        assertEquals(shootInput.dateShot.get(Calendar.DATE), actualDateShot.get(Calendar.DATE))
        assertEquals(shootInput.dateShot.get(Calendar.HOUR_OF_DAY), actualDateShot.get(Calendar.HOUR_OF_DAY))
        assertEquals(shootInput.dateShot.get(Calendar.MINUTE), actualDateShot.get(Calendar.MINUTE))
    }

    @Test
    fun testFaces_MultipleDistances() = runTest {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                longClickRow(0)
                clickEditDropdownMenuItem {
                    facesRobot.checkFaces("Full")

                    facesRobot.openDialog()
                    facesRobot.checkSwitchToMultiButtonIsShown()
                    facesRobot.checkSwitchToSingleButtonNotShown()
                    facesRobot.clickSingleOption("Half")
                    facesRobot.checkFaces("Half")

                    facesRobot.openDialog()
                    facesRobot.clickSwitchToMulti()
                    facesRobot.checkSwitchToSingleButtonIsShown()
                    facesRobot.checkSwitchToMultiButtonNotShown()
                    facesRobot.checkMultiOptions(listOf("80m: Half", "70m: Half", "60m: Half"))
                    facesRobot.clickMultiOption(0, "Full")
                    facesRobot.checkMultiOptions(listOf("80m: Full", "70m: Half", "60m: Half"))
                    facesRobot.clickMultiOption(2, "Triple")
                    facesRobot.checkMultiOptions(listOf("80m: Full", "70m: Half", "60m: Triple"))
                    facesRobot.clickConfirm()
                    facesRobot.checkFaces("Full, Half, Triple")

                    facesRobot.openDialog(false)
                    facesRobot.clickSwitchToSingle()
                    facesRobot.clickSingleOption("6-ring")
                    facesRobot.checkFaces("6-ring")
                }
            }
        }
    }

    @Test
    fun testFaces_SingleDistance() = runTest {
        setup()
        scenario.onActivity {
            runBlocking {
                db.shootDao().insert(
                        DatabaseShoot(
                                shootId = 2,
                                dateShot = Date(2020, 5, 10, 17, 12, 13).asCalendar(),
//            Calendar.Builder().setDate(2020, 5, 10).setTimeOfDay(17, 12, 13).build().time,
                                archerId = 1,
                                countsTowardsHandicap = true,
                        )
                )
                db.shootRoundDao().insert(DatabaseShootRound(shootId = 2, roundId = 2))
            }
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                longClickRow(0)
                clickEditDropdownMenuItem {
                    facesRobot.checkFaces("Full")

                    facesRobot.openDialog()
                    facesRobot.checkSwitchToMultiButtonNotShown()
                    facesRobot.checkSwitchToSingleButtonNotShown()
                    facesRobot.clickSingleOption("Half")
                    facesRobot.checkFaces("Half")
                }
            }
        }
    }
}
