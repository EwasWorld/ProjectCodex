package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils.parseDate
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.GridEndDsl
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadAddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadScorePadRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadStatsRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadStatsRobot.NumbersBreakdownRobot.Column.*
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadStatsRobot.NumbersBreakdownRobot.HandicapType
import eywa.projectcodex.model.SightMark
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
    val testTimeout: Timeout = Timeout.seconds(120)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

    private val sightMark70m = SightMarksPreviewHelper.sightMarks.find { it.distance == 70 && it.isMetric }

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
                listOf(RoundPreviewHelper.wa18RoundData, RoundPreviewHelper.wa70RoundData)
                        .forEach { db.add(it) }

                sightMark70m?.let { db.sightMarkRepo().insert(it) }
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testIndividualStandardFormatSetPointsWithRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                val date = "10/11/2020 10:15".parseDate()

                selectRoundsRobot.clickSelectedRound {
                    clickRound("WA 70")
                }
                setDate(date)
                setTime(date)
                clickType(NewScoreRobot.Type.COUNT)
                clickType(NewScoreRobot.Type.HEAD_TO_HEAD)

                checkIsH2hSetPoints(true)
                checkIsH2hStandardFormat(true)
                setHeadToHeadFields(1, 2, 60)

                clickSubmitNewScoreHeadToHead {
                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        clickEmptyScreenAddMatchButton()

                        clickNavBarItem<HeadToHeadStatsRobot> {
                            checkNoHeats()
                            checkDate("10/11/2020 10:15")
                            checkRound("H2h WA 70")
                            checkH2hInfo("Individual, Set points, Rank 2 of 60")
                            checkFaces("Full")
                            checkNumbersBreakdownNoData()
                        }
                        clickNavBarItem<HeadToHeadAddEndRobot> {}
                    }

                    /*
                     * Match 1: Quarter final 1/4 Bye
                     */
                    sightMarkIndicatorRobot.checkSightMarkIndicator("70m", "1.1")
                    sightMarkIndicatorRobot.clickAllSightMarks {
                        pressBack()
                    }
                    sightMarkIndicatorRobot.clickEditSightMark {
                        checkInfo(SightMark(sightMark70m!!), false)
                        pressBack()
                    }
                    checkIsBye(true)
                    clickStartMatch()
                    checkHeat(2)
                    clickStartMatch {
                        sightMarkIndicatorRobot.checkSightMarkIndicator("70m", "1.1")
                        checkSighters(0)
                        clickSighters {
                            checkRound("H2H")
                            checkInput(3)
                            setInputAmount(18)
                            clickAdd()
                            pressBack()
                        }
                        checkSighters(18)
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkMatchIsBye(1)
                        checkMatchDetails(1, 2, 18, null, null)
                        clickNavBarItem<HeadToHeadAddEndRobot> {}
                    }
                    checkScreenIsShown()

                    /*
                     * Match 2: Semi-final 1/2
                     */
//                    setOpponent("Amy Baker", 3)
                    checkHeat(1)
                    checkOpponentRank(3)
                    setOpponent("Claire Davids", -7)
                    checkOpponentRank(-7)
                    checkOpponentRankIsError()
                    setOpponent("Claire Davids", 3)
                    clickStartMatch {
                        clickNavBarItem<HeadToHeadScorePadRobot> {
                            checkNoGrid(2)
                            checkMatchDetails(2, 1, 0, "Claire Davids", 3)
                            clickNavBarItem<HeadToHeadAddEndRobot> {}
                        }

                        sightMarkIndicatorRobot.checkSightMarkIndicator("70m", "1.1")
                        checkOpponent("Claire Davids", 7)
                        checkSetResult(HeadToHeadResult.INCOMPLETE)

                        checkRows(
                                GridEndDsl.DEFAULT_ARCHER_NAME to false,
                                GridEndDsl.DEFAULT_OPPONENT_NAME to true,
                        )

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
                        checkMatchIsBye(1)
                        checkMatchDetails(1, 2, 0, null, null)
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

                    /*
                     * Match 3: Final (1/1)
                     */
                    checkHeat(0)
                    checkOpponentRank(1)
                    setOpponent("Emma Fitzgerald", 1)
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
                        checkMatchIsBye(1)
                        checkMatchDetails(1, 2, 0, null, null)

                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(6, HeadToHeadResult.LOSS, "5-6") {
                                checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10", 10, null, 0)
                                checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 10, null, 1)
                            }
                        }

                        checkGrid(3, HeadToHeadResult.INCOMPLETE) {
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

                    clickNavBarItem<HeadToHeadAddEndRobot> {
                        checkRows(
                                GridEndDsl.DEFAULT_ARCHER_NAME to false,
                                GridEndDsl.DEFAULT_OPPONENT_NAME to true,
                        )

                        clickEditRows {
                            checkEditRowsDialog(
                                    "Self" to "Arrows",
                                    "Opponent" to "Total",
                                    "Result" to "Auto",
                            )
                            clickEditRowsDialogRow("Opponent")
                            checkEditRowsDialog(
                                    "Self" to "Arrows",
                                    "Opponent" to "Off",
                                    "Result" to "Off",
                            )
                            clickCancel()
                        }
                        checkRows(
                                GridEndDsl.DEFAULT_ARCHER_NAME to false,
                                GridEndDsl.DEFAULT_OPPONENT_NAME to true,
                        )

                        clickEditRows {
                            checkEditRowsDialog(
                                    "Self" to "Arrows",
                                    "Opponent" to "Total",
                                    "Result" to "Auto",
                            )
                            checkEditRowsDialogUnknownResultWarningShown(false)
                            clickEditRowsDialogRow("Opponent")
                            clickEditRowsDialogRow("Self")
                            checkEditRowsDialog(
                                    "Self" to "Total",
                                    "Opponent" to "Off",
                                    "Result" to "Off",
                            )
                            checkEditRowsDialogUnknownResultWarningShown()
                            clickOk()
                        }
                        checkRows(GridEndDsl.DEFAULT_ARCHER_NAME to true)

                        setTotalRow(0, 30)
                        clickNextEnd()
                        checkNoRunningTotals()

                        clickNavBarItem<HeadToHeadScorePadRobot> {
                            checkGrid(3, HeadToHeadResult.UNKNOWN) {
                                checkEnd(1, HeadToHeadResult.WIN, "2-0") {
                                    checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                    checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                                }
                                checkEnd(2, HeadToHeadResult.WIN, "4-0") {
                                    checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                    checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                                }
                                checkEnd(3, HeadToHeadResult.UNKNOWN, "-") {
                                    checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, null, 30, null, null)
                                }
                            }

                            openEditEnd(3, 3) {
                                checkRows(GridEndDsl.DEFAULT_ARCHER_NAME to true)
                                checkTotalRow(0, 30)

                                clickEditRows {
                                    checkEditRowsDialog(
                                            "Self" to "Total",
                                            "Opponent" to "Off",
                                            "Result" to "Off",
                                    )
                                    clickEditRowsDialogRow("Result")
                                    checkEditRowsDialogUnknownResultWarningShown(false)
                                    checkEditRowsDialog(
                                            "Self" to "Total",
                                            "Opponent" to "Off",
                                            "Result" to "On",
                                    )
                                    clickOk()
                                }

                                checkRows(GridEndDsl.DEFAULT_ARCHER_NAME to true, "Result" to true)
                                checkTotalRow(0, 30)
                                checkResultRow(1, "Loss")
                                clickResultRow(1, "Win")
                                clickConfirmEdit()
                            }

                            checkGrid(3, HeadToHeadResult.WIN) {
                                checkEnd(1, HeadToHeadResult.WIN, "2-0") {
                                    checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                    checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                                }
                                checkEnd(2, HeadToHeadResult.WIN, "4-0") {
                                    checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, "10-10-10", 30, null, 2)
                                    checkRow(1, GridEndDsl.DEFAULT_OPPONENT_NAME, null, 29, null, 0)
                                }
                                checkEnd(3, HeadToHeadResult.WIN, "6-0") {
                                    checkRow(0, GridEndDsl.DEFAULT_ARCHER_NAME, null, 30, null, 2)
                                    checkResultsRow(1, "Win", 2)
                                }
                            }
                        }

                        checkMatchComplete()

                        clickNavBarItem<HeadToHeadStatsRobot> {
                            checkDate("10/11/2020 10:15")
                            checkRound("H2h WA 70")
                            checkH2hInfo("Individual, Set points, Rank 2 of 60")
                            checkFaces("Full")

                            checkMatchRow(0, "1/4", "-", "-", "Bye")
                            checkMatchRow(1, "Semi", "Claire Davids", "3", "5-6 Loss")
                            checkMatchRow(2, "Final", "Emma Fitzgerald", "1", "6-0 Win")

                            handicapAndClassificationRobot.checkClassification(Classification.MASTER_BOWMAN, false)
                            handicapAndClassificationRobot.checkHandicap(0)

                            checkNumbersBreakdown(HandicapType.SELF, listOf(SELF, OPPONENT, DIFFERENCE)) {
                                checkRow(0, "Semi", 0f)
                                checkEndAverages(0, SELF to 0.0f, OPPONENT to 0.0f, DIFFERENCE to 0.0f)
                                checkArrowAverages(0, SELF to 0.0f, OPPONENT to 0.0f, DIFFERENCE to 0.0f)

                                checkRow(1, "Final", 0f)
                                checkEndAverages(1, SELF to 0.0f, OPPONENT to 0.0f, DIFFERENCE to 0.0f)
                                checkArrowAverages(1, SELF to 0.0f, OPPONENT to 0.0f, DIFFERENCE to 0.0f)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testTeamStandardFormatSetPointsNoRound() {
        // No total archers
        // No distance - sight marks
        // Set rows
        // Check edit rows
        // Check score pad edit (end done)
        // Edit match details
        // Edit main details - prevent editing of standard format
        TODO()
    }

    @Test
    fun testIndividualCompound() {
        TODO()
    }
}
