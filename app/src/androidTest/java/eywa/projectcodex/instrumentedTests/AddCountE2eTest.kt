package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddCountE2eTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(30)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

    private fun setup() {
        CommonSetupTeardownFns.generalSetup()
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!
            runBlocking {
                TestUtils.ROUNDS.forEach { db.add(it) }
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testNoRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                checkType()
                clickType(false)
                clickSubmitNewScoreCount {
                    checkRound(null)
                    checkShotCount(0)
                    checkSightersCount(null)
                    checkTotalCount(null)
                    checkRemainingArrowsNotShown()

                    /*
                     * Increase/decrease buttons
                     */
                    checkInput(6)
                    clickIncreaseInput()
                    checkInput(7)
                    clickIncreaseInput()
                    checkInput(8)
                    clickDecreaseInput()
                    checkInput(7)
                    clickDecreaseInput()
                    checkInput(6)

                    /*
                     * Input bounds
                     */
                    setInputAmount(1)
                    setInputAmount(0)
                    setInputAmount(-1, "Must be between 0 and 3000 (inclusive)")
                    setInputAmount(3000)
                    setInputAmount(3001, "Must be between 0 and 3000 (inclusive)")

                    /*
                     * Input arrows
                     */
                    setInputAmount(6)
                    clickAdd()
                    checkShotCount(6)

                    setInputAmount(12)
                    clickAdd()
                    checkShotCount(18)

                    setInputAmount(-6)
                    clickAdd()
                    checkShotCount(12)

                    /*
                     * Input bounds
                     */
                    setInputAmount(-12)
                    setInputAmount(-13, "Must be between -12 and 2988 (inclusive)")
                    setInputAmount(3000 - 12)
                    setInputAmount(3001 - 12, "Must be between -12 and 2988 (inclusive)")

                    /*
                     * Edit
                     */
                    clickEditRoundData {
                        Espresso.pressBack()
                    }
                    checkScreenIsShown()
                }
            }
        }
    }

    @Test
    fun testWithRound() {
        val maxArrowCount = 108
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                selectRoundsRobot.clickSelectedRound {
                    clickRound("WA 1440")
                }
                checkType()
                clickType(false)
                clickSubmitNewScoreCount {
                    checkRound("1-1")
                    checkRemainingArrows("48 at 90m", "36 at 80m, 24 at 70m")
                    checkSightersCount(null)
                    checkShotCount(0)
                    checkTotalCount(null)

                    /*
                     * Input bounds
                     */
                    setInputAmount(1)
                    setInputAmount(0)
                    setInputAmount(-1, "Must be between 0 and $maxArrowCount (inclusive)")
                    setInputAmount(maxArrowCount)
                    setInputAmount(maxArrowCount + 1, "Must be between 0 and $maxArrowCount (inclusive)")

                    setInputAmount(6)
                    Espresso.closeSoftKeyboard()
                    clickAdd()
                    checkRemainingArrows("42 at 90m", "36 at 80m, 24 at 70m")
                    checkSightersCount(null)
                    checkShotCount(6)
                    checkTotalCount(null)

                    setInputAmount(maxArrowCount - 6)
                    Espresso.closeSoftKeyboard()
                    clickAdd()
                    checkSightersCount(null)
                    checkShotCount(maxArrowCount)
                    checkTotalCount(null)
                    checkRoundComplete()
                    checkAddNotExist()
                }
            }
        }
    }

    @Test
    fun testEditAndSwapToScoring() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickType(false)
                clickSubmitNewScoreCount {

                    clickEditRoundData {
                        clickType()

                        // Changing type opens up the add end screen
                        clickSubmitEditScoreChangeToScore {
                            Espresso.pressBack()
                        }
                    }
                }
            }
            // New score and such should all have been removed from the backstack
            checkScreenIsShown()
        }
    }

    /**
     * When the remaining arrows in a round is less than 6, it should adjust the default amount to the remaining arrows
     */
    @Test
    fun testAlmostCompleteRoundWithSighters() {
        val maxArrowCount = 108
        val shotCount = maxArrowCount - 3
        val sightersCount = 6
        val shoot = ShootPreviewHelperDsl.create {
            addRound(TestUtils.ROUNDS[0], sightersCount)
            addArrowCounter(shotCount)
        }

        setup()
        scenario.onActivity {
            runBlocking {
                db.add(shoot)
            }
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                clickRowCount(0) {
                    checkRound("1-1")
                    checkRemainingArrows("3 at 70m", null)

                    checkSightersCount(sightersCount)
                    checkShotCount(shotCount)
                    checkTotalCount(shotCount + sightersCount)

                    checkInput(maxArrowCount - shotCount)

                    setInputAmount(1)
                    setInputAmount(6, "Must be between -105 and 3 (inclusive)")
                }
            }
        }
    }
}
