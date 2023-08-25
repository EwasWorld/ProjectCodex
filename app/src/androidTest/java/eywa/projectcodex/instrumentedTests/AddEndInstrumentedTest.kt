package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.checkContainsToast
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.WORCESTER_DEFAULT_ID
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootDetail
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddEndInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private val arrowsPerArrowCount = 12
    private val roundsInput = listOf(
            Round(1, "metric", "Metric", true, true),
            Round(2, "imperial", "Imperial", true, false),
            Round(3, "worcester", "Worcester", true, false, defaultRoundId = WORCESTER_DEFAULT_ID),
    )
    private val arrowCountsInput = listOf(
            RoundArrowCount(1, 1, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 2, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 3, 1.0, arrowsPerArrowCount),
            RoundArrowCount(2, 1, 1.0, arrowsPerArrowCount),
            RoundArrowCount(2, 2, 1.0, arrowsPerArrowCount),
            RoundArrowCount(2, 3, 1.0, arrowsPerArrowCount),
            RoundArrowCount(3, 1, 1.0, arrowsPerArrowCount),
    )
    private val distancesInput = listOf(
            RoundDistance(1, 1, 1, 90),
            RoundDistance(1, 2, 1, 70),
            RoundDistance(1, 3, 1, 50),
            RoundDistance(2, 1, 1, 90),
            RoundDistance(2, 2, 1, 70),
            RoundDistance(2, 3, 1, 50),
            RoundDistance(3, 1, 1, 1),
    )
    private val shoots = listOf(
            DatabaseShoot(1, TestUtils.generateDate(2020), 1, true),
            DatabaseShoot(2, TestUtils.generateDate(2019), 1, true),
            DatabaseShoot(3, TestUtils.generateDate(2018), 1, true),
    )
    private val shootRounds = listOf(
            DatabaseShootRound(2, roundId = 1),
            DatabaseShootRound(3, roundId = 2),
    )

    /**
     * Set up [scenario] with desired fragment in the resumed state, and [db] with all desired information
     */
    private fun setup() {
        hiltRule.inject()

        // Start initialised so we can add to the database before the onCreate methods are called
        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!

            /*
             * Fill default rounds
             */
            runBlocking {
                roundsInput.forEach { item -> db.roundDao().insert(item) }
                arrowCountsInput.forEach { item -> db.roundArrowCountDao().insert(item) }
                distancesInput.forEach { item -> db.roundDistanceDao().insert(item) }
                shoots.forEach { item -> db.shootDao().insert(item) }
                shootRounds.forEach { item -> db.shootRoundDao().insert(item) }
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testScoreButtonPressed() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore {
                    /*
                     * Pressing each button
                     */
                    val buttons = listOf("m", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "X")
                    for (buttonText in buttons) {
                        val expected: Int = when (buttonText) {
                            "m" -> 0
                            "X" -> 10
                            else -> Integer.parseInt(buttonText)
                        }

                        clickScoreButton(buttonText)
                        checkInputtedArrows(listOf(buttonText))
                        checkEndTotal(expected)

                        clickScoreButton(buttonText)
                        checkInputtedArrows(listOf(buttonText, buttonText))
                        checkEndTotal(expected * 2)

                        clickClear()
                        checkInputtedArrows()
                        checkEndTotal(0)
                    }

                    /*
                     * Filling an end
                     */
                    clickScoreButton(3)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(3, 7))
                    checkEndTotal(10)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3))
                    checkEndTotal(13)
                    clickScoreButton(1)
                    clickScoreButton(1)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)

                    /*
                     * Too many arrows
                     */
                    clickScoreButton(7)
                    checkContainsToast("Arrows already added", composeTestRule)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)
                }
            }
        }
    }

    @Test
    fun testClearAndBackspace() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore {
                    /*
                     * Clear
                     */
                    // Full score
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(1)
                    clickScoreButton(1)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)
                    clickClear()
                    checkInputtedArrows()
                    checkEndTotal(0)

                    // Partial score
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(1)
                    checkInputtedArrows(listOf(3, 7, 3, 1))
                    checkEndTotal(14)
                    clickClear()
                    checkInputtedArrows()
                    checkEndTotal(0)

                    // No score
                    clickClear()
                    checkInputtedArrows()
                    checkEndTotal(0)

                    /*
                     * Backspace
                     */
                    // Full score
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(1)
                    clickScoreButton(1)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)

                    clickBackspace()
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1))
                    checkEndTotal(15)

                    clickBackspace()
                    checkInputtedArrows(listOf(3, 7, 3, 1))
                    checkEndTotal(14)

                    clickBackspace()
                    clickBackspace()
                    checkInputtedArrows(listOf(3, 7))
                    checkEndTotal(10)

                    clickBackspace()
                    clickBackspace()
                    checkInputtedArrows()
                    checkEndTotal(0)

                    clickBackspace()
                    checkContainsToast("No arrows entered", composeTestRule)
                    checkInputtedArrows()
                    checkEndTotal(0)
                }
            }
        }
    }

    @Test
    fun testNextEnd() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore {
                    checkIndicatorTable(0, 0)

                    // End 1
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(1)
                    clickScoreButton(1)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)

                    clickNextEnd()
                    checkIndicatorTable(18, 6)
                    checkInputtedArrows()
                    checkEndTotal(0)

                    // End 2
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(6)
                    clickScoreButton(6)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3, 6, 6, 3))
                    checkEndTotal(28)

                    clickNextEnd()
                    checkIndicatorTable(46, 12)
                    checkInputtedArrows()
                    checkEndTotal(0)

                    // No arrows
                    clickNextEnd()
                    checkContainsToast("Please enter all arrows for this end", composeTestRule)
                    CustomConditionWaiter.waitForToastToDisappear()
                    checkIndicatorTable(46, 12)
                    checkInputtedArrows()
                    checkEndTotal(0)

                    // Some arrows
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(6)
                    clickScoreButton(6)
                    checkInputtedArrows(listOf(3, 7, 3, 6, 6))

                    clickNextEnd()
                    checkContainsToast("Please enter all arrows for this end", composeTestRule)
                    checkIndicatorTable(46, 12)
                    checkInputtedArrows(listOf(3, 7, 3, 6, 6))
                }
            }
        }
    }

    @Test
    fun testRemainingArrowsIndicatorAndCompleteRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                longClickRow(1)

                clickContinueDropdownMenuItem {
                    waitForRemainingArrows()

                    checkRemainingArrows("12 at 90m", "12 at 70m, 12 at 50m")

                    completeEnd("1")
                    checkRemainingArrows("6 at 90m", "12 at 70m, 12 at 50m")

                    completeEnd("1")
                    checkRemainingArrows("12 at 70m", "12 at 50m")

                    completeEnd("1")
                    checkRemainingArrows("6 at 70m", "12 at 50m")

                    completeEnd("1")
                    checkRemainingArrows("12 at 50m", "")

                    completeEnd("1")
                    checkRemainingArrows("6 at 50m", "")

                    completeEnd("1")
                    clickRoundCompleteOk { }

                    clickNavBarAddEndWhileRoundComplete()
                }
            }
        }
    }

    @Test
    fun testOddEndSize() {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                longClickRow(1)

                clickContinueDropdownMenuItem {
                    checkInputtedArrows()
                    clickNavBarSettings {
                        setAddEndSize(5)
                        clickNavBarAddEnd { }
                    }

                    checkInputtedArrows(5)
                    completeEnd("1", 5)
                    checkInputtedArrows(5)
                    completeEnd("1", 5)
                    checkInputtedArrows(2)
                    completeEnd("1", 2)
                    checkInputtedArrows(5)
                }
            }
        }
    }

    @Test
    fun scoreButtonsChange() {
        setup()

        scenario.onActivity {
            runBlocking {
                listOf(
                        RoundFace.FULL,
                        RoundFace.TRIPLE,
                        RoundFace.HALF,
                        RoundFace.FITA_SIX,
                ).forEachIndexed { index, face ->
                    db.shootDao().insert(
                            DatabaseShoot(
                                    shootId = 4 + index,
                                    dateShot = TestUtils.generateDate(2021, 1 + index),
                                    archerId = 1,
                                    countsTowardsHandicap = true,
                            ),
                    )
                    db.shootDetailDao().insert(
                            DatabaseShootDetail(
                                    shootId = 4 + index,
                                    face = face,
                            )
                    )
                }

                db.shootDao().insert(
                        DatabaseShoot(
                                shootId = 8,
                                dateShot = TestUtils.generateDate(2021, 5),
                                archerId = 1,
                                countsTowardsHandicap = true,
                        ),
                )
                db.shootRoundDao().insert(
                        DatabaseShootRound(
                                shootId = 8,
                                faces = listOf(RoundFace.WORCESTER_FIVE),
                                roundId = 3,
                        )
                )
                db.shootDao().insert(
                        DatabaseShoot(
                                shootId = 9,
                                dateShot = TestUtils.generateDate(2021, 6),
                                archerId = 1,
                                countsTowardsHandicap = true,
                        ),
                )
                db.shootRoundDao().insert(
                        DatabaseShootRound(
                                shootId = 9,
                                roundId = 3,
                        )
                )
            }
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()

                // Worcester full
                longClickRow(0)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("6")
                    clickScoreButton(2)
                    clickScoreButton(4)
                    clickScoreButton(5)
                    checkInputtedArrows(listOf(2, 4, 5))
                    pressBack()
                }

                // Worcester five
                longClickRow(1)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("3")
                    checkScoreButtonNotDisplayed("6")
                    clickScoreButton(4)
                    clickScoreButton(5)
                    checkInputtedArrows(listOf(4, 5))
                    pressBack()
                }

                // Fita six
                longClickRow(2)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("4")
                    clickScoreButton(5)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(5, 7))
                    pressBack()
                }

                // Half
                longClickRow(3)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("5")
                    clickScoreButton(6)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(6, 7))
                    pressBack()
                }

                // Half
                longClickRow(4)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("5")
                    clickScoreButton(6)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(6, 7))
                    pressBack()
                }

                // Full
                longClickRow(5)
                clickContinueDropdownMenuItem {
                    clickScoreButton(1)
                    clickScoreButton(2)
                    clickScoreButton(7)
                    clickScoreButton(8)
                    checkInputtedArrows(listOf(1, 2, 7, 8))
                    pressBack()
                }

                // Metric
                longClickRow(7)
                clickContinueDropdownMenuItem {
                    clickScoreButton(1)
                    clickScoreButton(2)
                    clickScoreButton(7)
                    clickScoreButton(8)
                    checkInputtedArrows(listOf(1, 2, 7, 8))
                    pressBack()
                }

                // Imperial
                longClickRow(8)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("2")
                    checkScoreButtonNotDisplayed("8")
                    clickScoreButton(1)
                    clickScoreButton(3)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(1, 3, 7))
                    pressBack()
                }
            }
        }
    }
}
