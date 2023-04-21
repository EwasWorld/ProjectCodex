package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.*
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
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
class InputEndInstrumentedTest {
    companion object {
        init {
            SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController
    private lateinit var db: ScoresRoomDatabase
    private val arrowsPerArrowCount = 12
    private val roundsInput = listOf(
            Round(1, "test", "Test", true, true, listOf()),
            Round(2, "test2", "Test2", true, false, listOf())
    )
    private val arrowCountsInput = listOf(
            RoundArrowCount(1, 1, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 2, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 3, 1.0, arrowsPerArrowCount),
            RoundArrowCount(2, 1, 1.0, arrowsPerArrowCount),
            RoundArrowCount(2, 2, 1.0, arrowsPerArrowCount),
            RoundArrowCount(2, 3, 1.0, arrowsPerArrowCount)
    )
    private val distancesInput = listOf(
            RoundDistance(1, 1, 1, 90),
            RoundDistance(1, 2, 1, 70),
            RoundDistance(1, 3, 1, 50),
            RoundDistance(2, 1, 1, 90),
            RoundDistance(2, 2, 1, 70),
            RoundDistance(2, 3, 1, 50)
    )
    private val archerRounds = listOf(
            ArcherRound(1, TestUtils.generateDate(2020), 1, true),
            ArcherRound(2, TestUtils.generateDate(2019), 1, true, roundId = 1),
            ArcherRound(3, TestUtils.generateDate(2018), 1, true, roundId = 2),
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
            for (i in distancesInput.indices) {
                runBlocking {
                    if (i < roundsInput.size) {
                        db.roundDao().insert(roundsInput[i])
                    }
                    if (i < arrowCountsInput.size) {
                        db.roundArrowCountDao().insert(arrowCountsInput[i])
                    }
                    if (i < distancesInput.size) {
                        db.roundDistanceDao().insert(distancesInput[i])
                    }
                    if (i < archerRounds.size) {
                        db.archerRoundDao().insert(archerRounds[i])
                    }
                }
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

                    clickNavBarInputEndWhileRoundComplete()
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
                        setInputEndSize(5)
                        clickNavBarInputEnd { }
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

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                longClickRow(2)

                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("2")

                    checkInputtedArrows()
                    clickScoreButton(1)
                    checkInputtedArrows(listOf(1))
                }
            }
        }
    }
}
