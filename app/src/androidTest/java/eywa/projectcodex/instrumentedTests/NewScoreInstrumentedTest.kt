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
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.selectRound.SelectRoundRobot
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
    val testTimeout: Timeout = Timeout.seconds(30)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private var roundsInput = TestUtils.ROUNDS
    private var shootInput = ShootPreviewHelperDsl.create {
        shoot = shoot.copy(dateShot = DateTimeFormat.SHORT_DATE_TIME.parse("10/6/19 17:12"))
        round = roundsInput[0]
        roundSubTypeId = 2
    }

    private suspend fun getCurrentShoots() = db.shootsRepo().getFullShootInfo().first()

    private suspend fun getShoots(shootId: Int) =
            db.shootDao().getFullShootInfo(shootId).first()

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
                roundsInput.forEach { db.add(it) }
                db.add(shootInput)
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
    fun testAddRoundNoType() = runTest {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore()
            }
        }

        runBlocking { delay(1000) }
        val currentShoots = getCurrentShoots()
        assertEquals(2, currentShoots.size)
        assertEquals(2, currentShoots[1].shoot.shootId)
        assertEquals(null, currentShoots[1].round?.roundId)
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun testAddAnotherRound() = runTest {
        setup()
        val ar = DatabaseShoot(2, TestUtils.generateDate(2020))
        db.shootDao().insert(ar)

        db.arrowScoreDao().insert(
                *List(2) { DatabaseArrowScore(it + 1, 1, 6 + it, false) }.toTypedArray()
        )

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)
                waitForHsg(0, "1/7/0")
                waitForHsg(1, "1/6/0")
                pressBack()
            }

            clickNewScore {
                clickSubmitNewScore {
                    checkIndicatorTable(0, 0)
                }
            }
        }

        runBlocking { delay(1000) }
        val roundsAfterCreate = getCurrentShoots()
                .filterNot { it.shoot.shootId == ar.shootId }
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(shootInput.shoot, roundsAfterCreate[0].shoot)
        assert(roundsAfterCreate[1].shoot.shootId > ar.shootId)
        assertEquals(null, roundsAfterCreate[1].shootRound?.roundId)
        assertEquals(null, roundsAfterCreate[1].shootRound?.roundSubTypeId)
    }

    @Test
    fun testNoRoundButton() = runTest {
        roundsInput = List(20) {
            FullRoundInfo(
                    round = Round(it + 1, "$it", "$it", false, false),
                    roundArrowCounts = listOf(RoundArrowCount(it + 1, 1, 1.0, 1)),
                    roundDistances = listOf(RoundDistance(it + 1, 1, 1, 1)),
            )
        }
        shootInput = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = DateTimeFormat.SHORT_DATE_TIME.parse("10/6/19 17:12"))
            round = roundsInput[0]
        }
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                selectRoundsRobot.clickSelectedRound {
                    clickRound("1")
                }
                selectRoundsRobot.checkSelectedRound("1")

                selectRoundsRobot.clickSelectedRound {
                    clickNoRound()
                }
                selectRoundsRobot.checkSelectedRound("No Round")
            }
        }
    }

    /**
     * Test row added to archer_round
     * Test round and subtype id are correct
     */
    @Test
    fun testAddRoundWithSubtype() = runTest {
        setup()
        db.shootsRepo().getFullShootInfo().takeWhile { it.isEmpty() }.collect()

        val roundsBeforeCreate = getCurrentShoots()
        assertEquals(1, roundsBeforeCreate.size)

        val selectedRound = roundsInput[0]
        val selectedSubtype = selectedRound.roundSubTypes!![1]
        composeTestRule.mainMenuRobot {
            clickNewScore {
                selectRoundsRobot.clickSelectedRound {
                    clickRound(selectedRound.round.displayName)
                }
                selectRoundsRobot.clickSelectedSubtype {
                    clickSubtypeDialogSubtype(selectedSubtype.name!!)
                }

                clickSubmitNewScore()
            }
        }

        runBlocking { delay(1000) }
        val roundsAfterCreate = getCurrentShoots()
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(shootInput.shoot, roundsAfterCreate[0].shoot)
        assertEquals(selectedRound.round.roundId, roundsAfterCreate[1].shootRound?.roundId)
        assertEquals(selectedSubtype.subTypeId, roundsAfterCreate[1].shootRound?.roundSubTypeId)
    }

    @Test
    fun testCustomDateTime() = runTest {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                setTime(20, 22)
                checkTime("20:22")

                setDate(30, 10, 2040)
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
        val roundsAfterCreate = getCurrentShoots().toMutableList()
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

                    selectRoundsRobot.checkSelectedRound(shootInput.round!!.displayName)

                    /*
                     * Change some stuff
                     */
                    setTime(calendar)
                    setDate(calendar)
                    checkTime("13:15")
                    checkDate("30 Oct 40")

                    selectRoundsRobot.clickSelectedRound() {
                        clickRound(selectedRound.round.displayName)
                    }

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

                    selectRoundsRobot.clickSelectedRound {
                        clickRound(selectedRound.round.displayName)
                    }

                    /*
                     * Save
                     */
                    clickSubmitEditScore()
                }
            }
        }

        runBlocking { delay(1000) }
        val updated = getShoots(shootInput.shoot.shootId)
        assertEquals(
                DatabaseShoot(
                        shootInput.shoot.shootId,
                        calendar,
                        shootInput.shoot.archerId,
                        shootInput.shoot.countsTowardsHandicap,
                ),
                updated!!.shoot.copy(dateShot = calendar)
        )
        assertEquals(
                DatabaseShootRound(
                        shootId = shootInput.shoot.shootId,
                        roundId = selectedRound.round.roundId,
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
                    selectRoundsRobot.clickSelectedRound {
                        clickNoRound()
                    }
                    clickSubmitEditScore()
                }
            }
        }

        runBlocking { delay(1000) }
        val actual = getShoots(shootInput.shoot.shootId)!!
        val actualDateShot = actual.shoot.dateShot
        assertEquals(
                DatabaseShoot(
                        shootId = shootInput.shoot.shootId,
                        dateShot = actualDateShot,
                        archerId = shootInput.shoot.archerId,
                        countsTowardsHandicap = shootInput.shoot.countsTowardsHandicap,
                ),
                actual.shoot,
        )
        assertEquals(
                null,
                actual.shootRound,
        )
        assertEquals(shootInput.shoot.dateShot.get(Calendar.YEAR), actualDateShot.get(Calendar.YEAR))
        assertEquals(shootInput.shoot.dateShot.get(Calendar.MONTH), actualDateShot.get(Calendar.MONTH))
        assertEquals(shootInput.shoot.dateShot.get(Calendar.DATE), actualDateShot.get(Calendar.DATE))
        assertEquals(shootInput.shoot.dateShot.get(Calendar.HOUR_OF_DAY), actualDateShot.get(Calendar.HOUR_OF_DAY))
        assertEquals(shootInput.shoot.dateShot.get(Calendar.MINUTE), actualDateShot.get(Calendar.MINUTE))
    }

    @Test
    fun testFaces_MultipleDistances() = runTest {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                longClickRow(0)
                clickEditDropdownMenuItem {
                    with(selectFaceBaseRobot) {
                        checkFaces("Full")

                        openSingleSelectDialog {
                            checkSwitchButton()
                            clickOption("Half")
                        }
                        checkFaces("Half")

                        openSingleSelectDialog {
                            clickSwitchButton {
                                checkSwitchButton()
                                checkOptions(listOf("80m: Half", "70m: Half", "60m: Half"))
                                clickOption(0, "Full")
                                checkOptions(listOf("80m: Full", "70m: Half", "60m: Half"))
                                clickOption(2, "Triple")
                                checkOptions(listOf("80m: Full", "70m: Half", "60m: Triple"))
                                clickConfirm()
                            }
                        }
                        checkFaces("Full, Half, Triple")

                        openMultiSelectDialog {
                            clickSwitchButton {
                                clickOption("6-ring")
                            }
                        }
                        checkFaces("6-ring")
                    }
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
                        )
                )
            }
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                longClickRow(0)
                clickEditDropdownMenuItem {
                    with(selectFaceBaseRobot) {
                        checkFaces("Full")

                        openSingleSelectDialog {
                            checkSwitchButton(false)
                            clickOption("Half")
                        }
                        checkFaces("Half")
                    }
                }
            }
        }
    }

    @Test
    fun testSelectRoundDialog() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                selectRoundsRobot.clickSelectedRound {
                    checkRoundOptions(listOf("WA 1440", "St. George", "Portsmouth", "WA 25"))
                    checkNoFiltersAreOn()

                    clickFilter(SelectRoundRobot.Filter.METRIC)
                    checkRoundOptions(listOf("WA 1440", "WA 25"))
                    checkRoundOptionsNotExist(listOf("St. George", "Portsmouth"))
                    clickFilter(SelectRoundRobot.Filter.IMPERIAL)
                    checkRoundOptions(listOf("St. George", "Portsmouth"))
                    checkRoundOptionsNotExist(listOf("WA 1440", "WA 25"))
                    clickFilter(SelectRoundRobot.Filter.IMPERIAL, false)
                    checkRoundOptions(listOf("WA 1440", "St. George", "Portsmouth", "WA 25"))
                    checkNoFiltersAreOn()

                    clickFilter(SelectRoundRobot.Filter.INDOOR)
                    checkRoundOptions(listOf("Portsmouth", "WA 25"))
                    checkRoundOptionsNotExist(listOf("WA 1440", "St. George"))
                    clickFilter(SelectRoundRobot.Filter.OUTDOOR)
                    checkRoundOptions(listOf("WA 1440", "St. George"))
                    checkRoundOptionsNotExist(listOf("Portsmouth", "WA 25"))
                    clickFilter(SelectRoundRobot.Filter.OUTDOOR, false)
                    checkRoundOptions(listOf("WA 1440", "St. George", "Portsmouth", "WA 25"))
                    checkNoFiltersAreOn()

                    clickFilter(SelectRoundRobot.Filter.INDOOR)
                    clickFilter(SelectRoundRobot.Filter.METRIC)
                    checkRoundOptions(listOf("WA 25"))
                    checkRoundOptionsNotExist(listOf("WA 1440", "St. George", "Portsmouth"))
                }
            }
        }
    }
}
