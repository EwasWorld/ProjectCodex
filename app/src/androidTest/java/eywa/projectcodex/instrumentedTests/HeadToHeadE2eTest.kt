package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.GridEndDsl
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadAddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadScorePadRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HeadToHeadE2eTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

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
                listOf(
                        RoundPreviewHelper.wa18RoundData,
                        RoundPreviewHelper.wa70RoundData,
                ).forEach { db.add(it) }
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testIndividualRecurveWithEdit() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickType(NewScoreRobot.Type.COUNT)
                clickType(NewScoreRobot.Type.HEAD_TO_HEAD)

                checkIsSetPoints(true)
                setHeadToHeadFields(1, 2)

                clickSubmitNewScoreHeadToHead {
                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        clickEmptyScreenAddHeatButton()
                        clickNavBarItem<HeadToHeadAddEndRobot> {}
                    }

                    clickStartMatch()
                    checkMatchNotSelectedIsError()
                    selectMatch(3)
                    setOpponent("Amy Baker", -3)
                    checkOpponentRankIsError()
                    setOpponent("Amy Baker", 3)
                    setIsBye(true)
                    clickStartMatch()
                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkHeatIsBye(3)
                        checkHeatDetails(3, 0, null, null)
                        clickNavBarItem<HeadToHeadAddEndRobot> {}
                    }

                    checkMatch(2)
                    setOpponent("Claire Davids", 7)
                    clickStartMatch {
                        clickNavBarItem<HeadToHeadScorePadRobot> {
                            checkNoGrid(2)
                            checkHeatDetails(2, 0, "Claire Davids", 7)
                            clickNavBarItem<HeadToHeadAddEndRobot> {}
                        }

                        checkSightMark()
                        checkOpponent("Claire Davids", 7)
                        checkSetResult(HeadToHeadResult.INCOMPLETE)

                        checkRunningTotals(0, 0)
                        clickNextEnd()
                        checkArrowRowError(0, "Required")
                        checkTotalRowError(1, "Required")

                        setArrowRow(0, listOf(10, 10, 10))
                        setTotalRow(1, 29)
                        checkArrowRowError(0, null)
                        checkTotalRowError(1, null)
                        setTotalRow(1, 31)
                        checkTotalRowError(1, "Too high")
                        setTotalRow(1, 29)
                        checkTotalRowError(1, null)

                        checkSetResult(HeadToHeadResult.WIN)
                        clickNextEnd()
                        checkRunningTotals(2, 0)

                        checkSetResult(HeadToHeadResult.INCOMPLETE)
                        setArrowRow(0, listOf(1, 10, 10))
                        checkSetResult(HeadToHeadResult.INCOMPLETE)
                        setTotalRow(1, 29)
                        checkSetResult(HeadToHeadResult.LOSS)
                        clickNextEnd()
                        checkRunningTotals(2, 2)

                        setArrowRow(0, listOf(1, 10, 10))
                        setTotalRow(1, 29)
                        checkSetResult(HeadToHeadResult.LOSS)
                        clickNextEnd()
                        checkRunningTotals(2, 4)

                        setArrowRow(0, listOf(9, 10, 10))
                        setTotalRow(1, 29)
                        checkSetResult(HeadToHeadResult.TIE)
                        clickNextEnd()
                        checkRunningTotals(3, 5)

                        setArrowRow(0, listOf(10, 10, 10))
                        setTotalRow(1, 29)
                        checkSetResult(HeadToHeadResult.WIN)
                        clickNextEnd()
                        checkRunningTotals(5, 5)

                        checkShootOffWin(false)
                        setArrowRow(0, listOf(10))
                        setTotalRow(1, 9)
                        checkShootOffWin(true)
                        checkSetResult(HeadToHeadResult.WIN)

                        setTotalRow(1, 10)
                        checkSetResult(HeadToHeadResult.WIN)
                        tapIsShootOffWin(false)
                        checkSetResult(HeadToHeadResult.LOSS)
                        clickNextEnd { }
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkHeatIsBye(3)
                        checkHeatDetails(3, 0, null, null)
                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "1-10-10", 21, null, 0)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 2)
                            }
                            checkEnd(3, HeadToHeadResult.LOSS, "2-4") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "1-10-10", 21, null, 0)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 2)
                            }
                            checkEnd(4, HeadToHeadResult.TIE, "3-5") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "9-10-10", 29, null, 1)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 1)
                            }
                            checkEnd(5, HeadToHeadResult.WIN, "5-5") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                            }
                            checkEnd(6, HeadToHeadResult.LOSS, "5-6") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10", 10, null, 0)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 10, null, 1)
                            }
                        }
                    }

                    checkMatch(1)
                    setOpponent("Emma Fitzgerald", 3)
                    clickStartMatch {
                        setArrowRow(0, listOf(10, 10, 10))
                        setTotalRow(1, 29)
                        clickNextEnd()
                        checkRunningTotals(2, 0)

                        setArrowRow(0, listOf(10, 10, 10))
                        setTotalRow(1, 29)
                        clickNextEnd()
                        checkRunningTotals(4, 0)
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkHeatIsBye(3)
                        checkHeatDetails(3, 0, null, null)


                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "1-10-10", 21, null, 0)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 2)
                            }
                            checkEnd(3, HeadToHeadResult.LOSS, "2-4") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "1-10-10", 21, null, 0)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 2)
                            }
                            checkEnd(4, HeadToHeadResult.TIE, "3-5") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "9-10-10", 29, null, 1)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 1)
                            }
                            checkEnd(5, HeadToHeadResult.WIN, "5-5") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                            }
                            checkEnd(6, HeadToHeadResult.LOSS, "5-6") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10", 10, null, 0)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 10, null, 1)
                            }
                        }

                        checkGrid(1, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testTeamRecurveWithSightersAndEdit() {
        TODO()
    }

    @Test
    fun testIndividualCompound() {
        TODO()
    }
}
