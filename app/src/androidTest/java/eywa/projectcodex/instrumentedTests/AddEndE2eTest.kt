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
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.WORCESTER_DEFAULT_ID
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.model.SightMark
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddEndE2eTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(30)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private val arrowsPerArrowCount = 12
    val rounds = listOf(
            FullRoundInfo(
                    round = Round(1, "metric", "Metric", true, true),
                    roundSubTypes = listOf(),
                    roundArrowCounts = listOf(
                            RoundArrowCount(1, 1, 1.0, arrowsPerArrowCount),
                            RoundArrowCount(1, 2, 1.0, arrowsPerArrowCount),
                            RoundArrowCount(1, 3, 1.0, arrowsPerArrowCount),
                    ),
                    roundDistances = listOf(
                            RoundDistance(1, 1, 1, 90),
                            RoundDistance(1, 2, 1, 70),
                            RoundDistance(1, 3, 1, 50),
                    ),
            ),
            FullRoundInfo(
                    round = Round(2, "imperial", "Imperial", true, false),
                    roundSubTypes = listOf(),
                    roundArrowCounts = listOf(
                            RoundArrowCount(2, 1, 1.0, arrowsPerArrowCount),
                            RoundArrowCount(2, 2, 1.0, arrowsPerArrowCount),
                            RoundArrowCount(2, 3, 1.0, arrowsPerArrowCount),
                    ),
                    roundDistances = listOf(
                            RoundDistance(2, 1, 1, 90),
                            RoundDistance(2, 2, 1, 70),
                            RoundDistance(2, 3, 1, 50),
                    ),
            ),
            FullRoundInfo(
                    round = Round(3, "worcester", "Worcester", true, false, defaultRoundId = WORCESTER_DEFAULT_ID),
                    roundSubTypes = listOf(),
                    roundArrowCounts = listOf(
                            RoundArrowCount(3, 1, 1.0, arrowsPerArrowCount),
                    ),
                    roundDistances = listOf(
                            RoundDistance(3, 1, 1, 1),
                    ),
            ),
    )

    private val shoots = listOf(
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(shootId = 1, dateShot = TestUtils.generateDate(2020))
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(shootId = 2, dateShot = TestUtils.generateDate(2019))
                round = rounds[0]
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(shootId = 3, dateShot = TestUtils.generateDate(2018))
                round = rounds[1]
            },
    )

    private val sightMarks = SightMarksPreviewHelper.sightMarks

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
                rounds.forEach { db.add(it) }
                shoots.forEach { db.add(it) }
                sightMarks.forEach { db.sightMarkRepo().insert(it) }
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testIndicatorTableAndNextEndButton() {
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
    fun testRemainingArrowsIndicator_SightMark_Sighters_AndCompleteRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                longClickRow(1)

                clickContinueDropdownMenuItem {
                    waitForRemainingArrows()

                    checkRemainingArrows("12 at 90m,", "12 at 70m, 12 at 50m")
                    checkSightMarkIndicator("90m", null)
                    checkIndicatorTable(0, 0)
                    clickAllSightMarks {
                        pressBack()
                    }
                    clickEditSightMark {
                        checkDistance(90, true)
                        pressBack()
                    }

                    checkSightersCount(0)
                    clickSighters {
                        setInputAmount(8)
                        clickAdd()
                        pressBack()
                    }
                    checkScreenIsShown()
                    checkSightersCount(8)
                    checkIndicatorTable(0, 0)

                    completeEnd("1")
                    checkRemainingArrows("6 at 90m,", "12 at 70m, 12 at 50m")
                    checkSightMarkIndicator("90m", null)
                    checkIndicatorTable(6, 6)

                    completeEnd("1")
                    checkRemainingArrows("12 at 70m,", "12 at 50m")
                    checkSightMarkIndicator("70m", "1.1")
                    clickEditSightMark {
                        checkInfo(SightMark(SightMarksPreviewHelper.sightMarks[5]), false)
                        pressBack()
                    }

                    completeEnd("1")
                    checkRemainingArrows("6 at 70m,", "12 at 50m")
                    checkSightMarkIndicator("70m", "1.1")

                    completeEnd("1")
                    checkRemainingArrows("12 at 50m", "")
                    checkSightMarkIndicator("50m", "1.75")

                    completeEnd("1")
                    checkRemainingArrows("6 at 50m", "")
                    checkSightMarkIndicator("50m", "1.75")

                    checkIndicatorTable(30, 30)
                    completeEnd("1")
                    clickRoundCompleteOk { }
                }
            }
        }
    }
}
