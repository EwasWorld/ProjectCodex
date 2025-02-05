package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils.parseDate
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.GridSetDsl
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.GridSetDsl.CellValue.*
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadAddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadAddMatchRobot
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

                db.bowRepo().insertDefaultBowIfNotExist()
                sightMark70m?.let { db.sightMarkRepo().insert(it) }
            }
        }

        ConditionWatcher.setTimeoutLimit(10_000)
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
                val archerName = GridSetDsl.DEFAULT_ARCHER_NAME
                val opponentName = GridSetDsl.DEFAULT_OPPONENT_NAME

                selectRoundsRobot.clickSelectedRound {
                    clickRound("WA 70")
                }
                setDate(date)
                setTime(date)
                clickType(NewScoreRobot.Type.COUNT)
                clickType(NewScoreRobot.Type.HEAD_TO_HEAD)

                checkIsH2hSetPoints(true)
                checkIsH2hStandardFormat(true)
                // TODO Check separately
                setHeadToHeadFields(-1, -1, -1)
                checkHeadToHeadFieldsAreError()
                setHeadToHeadFields(1, 2, 6)

                clickSubmitNewScoreHeadToHead {
                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        clickEmptyScreenAddMatchButton()
                    }
                    checkScreenIsShown()

                    clickNavBarItem<HeadToHeadStatsRobot> {
                        checkDate("10 Nov 20 10:15")
                        checkRound("H2H: WA 70")
                        checkH2hInfo("Individual, Set points, Rank 2 of 6")
                        checkFaces("Full")

                        checkNoMatches()
                        checkNumbersBreakdownNotShown()

                        clickNavBarItem<HeadToHeadAddMatchRobot> {}
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
                    checkHeat("1/4")
                    checkOpponentRank(null)
                    checkMaxRank(1)

                    clickStartMatch {
                        sightMarkIndicatorRobot.checkSightMarkIndicator("70m", "1.1")
                        checkSighters(0)
                        clickSighters {
                            checkRound("H2H: WA 70")
                            checkInput(3)
                            setInputAmount(18)
                            clickAdd()
                            checkSightersCount(18)
                            pressBack()
                        }
                        checkSighters(18)

                        clickNavBarItem<HeadToHeadScorePadRobot> {
                            checkMatchIsBye(1)
                            checkMaxRank(1, null)
                            checkMatchDetails(1, "quarter-final (1/4)", 18, null, null)
                            clickNavBarItem<HeadToHeadAddEndRobot> {}
                        }
                        checkScreenIsShown()
                        clickCreateNextMatch {}
                    }

                    /*
                     * Match 2: Semi-final 1/2
                     */
                    checkPrevious("Match 1: 1/4", "Bye Result:")
                    checkHeat("Semi")
                    checkMaxRank(1)
                    checkOpponentRank(3)
                    setOpponent("Claire Davids", -7)
                    checkOpponentRank(-7)
                    checkOpponentRankIsError()
                    setOpponent("Claire Davids", 3)
                    clickStartMatch {
                        clickNavBarItem<HeadToHeadScorePadRobot> {
                            checkNoGrid(2)
                            checkMaxRank(2, null)
                            checkMatchDetails(2, "semi-final (1/2)", 0, "Claire Davids", 3)
                            clickNavBarItem<HeadToHeadAddEndRobot> {}
                        }

                        sightMarkIndicatorRobot.checkSightMarkIndicator("70m", "1.1")
                        checkOpponent("Claire Davids", 3)
                        checkSetResult(1, HeadToHeadResult.INCOMPLETE)

                        checkRows(
                                3,
                                archerName to false,
                                opponentName to true,
                        )

                        checkRunningTotals(0, 0)
                        clickNextEnd()
                        checkArrowRowError(0, "Required")
                        checkTotalRowError(1, "Required")

                        setArrowRow(0, archerName, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, 29)
                        checkArrowRowError(0, null)
                        checkTotalRowError(1, null)
                        setTotalRow(1, opponentName, 31)
                        checkTotalRowError(1, "Must be between 0 and 30 (inclusive)")
                        setTotalRow(1, opponentName, 29)
                        checkTotalRowError(1, null)

                        checkSetResult(1, HeadToHeadResult.WIN)
                        clickNextEnd()
                        checkRunningTotals(2, 0)

                        checkSetResult(2, HeadToHeadResult.INCOMPLETE)
                        setArrowRow(0, archerName, listOf("1", "10", "10"), 21)
                        checkSetResult(2, HeadToHeadResult.INCOMPLETE)
                        setTotalRow(1, opponentName, 29)
                        checkSetResult(2, HeadToHeadResult.LOSS)
                        clickNextEnd()
                        checkRunningTotals(2, 2)

                        setArrowRow(0, archerName, listOf("1", "10", "10"), 21)
                        setTotalRow(1, opponentName, 29)
                        checkSetResult(3, HeadToHeadResult.LOSS)
                        clickNextEnd()
                        checkRunningTotals(2, 4)

                        setArrowRow(0, archerName, listOf("9", "10", "10"), 29)
                        setTotalRow(1, opponentName, 29)
                        checkSetResult(4, HeadToHeadResult.TIE)
                        clickNextEnd()
                        checkRunningTotals(3, 5)

                        setArrowRow(0, archerName, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, 29)
                        checkSetResult(5, HeadToHeadResult.WIN)
                        clickNextEnd()
                        checkRunningTotals(5, 5)

                        checkShootOffWinChipNotShown()
                        setArrowRow(0, archerName, listOf("10"), 10)
                        setTotalRow(1, opponentName, 9)
                        checkShootOffWinChipNotShown()
                        checkShootOffSetResult(HeadToHeadResult.WIN)

                        setTotalRow(1, opponentName, 10)
                        checkShootOffWinChip(false)
                        checkShootOffSetResult(HeadToHeadResult.LOSS)
                        tapIsShootOffWin(true)
                        checkShootOffSetResult(HeadToHeadResult.WIN)
                        tapIsShootOffWin(false)
                        checkShootOffSetResult(HeadToHeadResult.LOSS)
                        clickNextEnd { }
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkMatchIsBye(1)
                        checkMatchDetails(1, "quarter-final (1/4)", 18, null, null)
                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2", 2) {
                                checkRow(0, archerName, Value("1-10-10"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(2))
                            }
                            checkEnd(3, HeadToHeadResult.LOSS, "2-4", 2) {
                                checkRow(0, archerName, Value("1-10-10"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(2))
                            }
                            checkEnd(4, HeadToHeadResult.TIE, "3-5", 2) {
                                checkRow(0, archerName, Value("9-10-10"), 29, NoColumn, Value(1))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(1))
                            }
                            checkEnd(5, HeadToHeadResult.WIN, "5-5", 2) {
                                checkRow(0, archerName, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(6, HeadToHeadResult.LOSS, "5-6", 2) {
                                checkRow(0, archerName, Value("10"), 10, NoColumn, Value(0))
                                checkRow(1, opponentName, Empty, 10, NoColumn, Value(1))
                            }
                        }
                        clickNavBarItem<HeadToHeadAddMatchRobot> {}
                    }

                    /*
                     * Match 3: Final (1/1)
                     */
                    checkPrevious("Match 2: Semi", "Loss 5-6")
                    checkHeat("Final")
                    checkMaxRank(3)
                    checkOpponentRank(1)
                    setOpponent("Emma Fitzgerald", 1)
                    clickStartMatch {
                        setArrowRow(0, archerName, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, 29)
                        clickNextEnd()
                        checkRunningTotals(2, 0)

                        setArrowRow(0, archerName, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, 29)
                        clickNextEnd()
                        checkRunningTotals(4, 0)
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkMatchIsBye(1)
                        checkMatchDetails(1, "quarter-final (1/4)", 18, null, null)

                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(6, HeadToHeadResult.LOSS, "5-6", 2) {
                                checkRow(0, archerName, Value("10"), 10, NoColumn, Value(0))
                                checkRow(1, opponentName, Empty, 10, NoColumn, Value(1))
                            }
                        }

                        checkMaxRank(3, 3)
                        checkGrid(3, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(0))
                            }
                        }
                    }

                    clickNavBarItem<HeadToHeadAddEndRobot> {
                        checkRows(
                                3,
                                archerName to false,
                                opponentName to true,
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
                                3,
                                archerName to false,
                                opponentName to true,
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
                        checkRows(3, archerName to true)

                        setTotalRow(0, archerName, 30, arrows = NoColumn)
                        clickNextEnd()
                        checkNoRunningTotals()
                        checkRows(3, archerName to true)
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkGrid(3, HeadToHeadResult.UNKNOWN) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(3, HeadToHeadResult.UNKNOWN, "-", 1) {
                                checkRow(0, archerName, Empty, 30, NoColumn, Empty)
                            }
                        }

                        openEditEnd(3, 3) {
                            checkRows(3, archerName to true)
                            gridSetDsl.checkRow(0, archerName, NoColumn, 30, NoColumn, NoColumn, isEditable = true)

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

                            checkRows(3, archerName to true, "Result" to true)
                            gridSetDsl.checkRow(0, archerName, NoColumn, 30, NoColumn, NoColumn, isEditable = true)
                            checkResultRow(1, HeadToHeadResult.LOSS)
                            clickResultRow(1, HeadToHeadResult.TIE)
                            clickResultRow(1, HeadToHeadResult.WIN)
                            checkSetResult(3, HeadToHeadResult.WIN)
                            clickConfirmEdit()
                        }
                        checkScreenIsShown()

                        checkGrid(3, HeadToHeadResult.WIN) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(3, HeadToHeadResult.WIN, "6-0", 2) {
                                checkRow(0, archerName, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win", Value(2))
                            }
                        }
                    }

                    clickNavBarItem<HeadToHeadStatsRobot> {
                        checkDate("10 Nov 20 10:15")
                        checkRound("H2H: WA 70")
                        checkH2hInfo("Individual, Set points, Rank 2 of 6")
                        checkFaces("Full")

                        checkMatchRow(0, "1/4", "-", "-", "Bye")
                        checkMatchRow(1, "Semi", "Claire Davids", "3", "5-6 Loss")
                        checkMatchRow(2, "Final", "Emma Fitzgerald", "1", "6-0 Win")

                        handicapAndClassificationRobot.checkClassification(
                                Classification.ELITE_MASTER_BOWMAN,
                                false
                        )
                        handicapAndClassificationRobot.checkHandicap(14)

                        checkNumbersBreakdown(HandicapType.SELF, listOf(SELF, OPPONENT, DIFFERENCE)) {
                            checkRow(0, "Semi", 23f)
                            // 2.6 isn't exactly correct, rounding error?
                            checkEndAverages(0, SELF to 26.4f, OPPONENT to 29.1f, DIFFERENCE to -2.6f)
                            checkArrowAverages(0, SELF to 8.8f, OPPONENT to 9.7f, DIFFERENCE to -0.9f)

                            checkRow(1, "Final", 0f)
                            checkEndAverages(1, SELF to 30.0f, OPPONENT to 29.0f, DIFFERENCE to 1.0f)
                            checkArrowAverages(1, SELF to 10.0f, OPPONENT to 9.7f, DIFFERENCE to 0.3f)

                            checkRow(2, "Total", 14f)
                            checkEndAverages(2, SELF to 27.7f, OPPONENT to 29.0f, DIFFERENCE to -1.3f)
                            // 0.4 isn't exactly correct, rounding error?
                            checkArrowAverages(2, SELF to 9.2f, OPPONENT to 9.7f, DIFFERENCE to -0.4f)
                        }
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkMatchDetails(1, "quarter-final (1/4)", 18, null, null)
                        clickEditMatchInfo(1) {
                            checkHeat("1/4")
                            checkOpponent(null)
                            checkOpponentRank(null)
                            checkIsBye(true)
                            checkByeWithSetsWarningShown(false)

                            setHeat("1/8")
                            setIsBye(false)
                            setOpponent("Amy Baker", 5)
                            checkByeWithSetsWarningShown(false)

                            clickResetEdit()

                            checkHeat("1/4")
                            checkOpponent(null)
                            checkOpponentRank(null)
                            checkIsBye(true)

                            setHeat("1/8")
                            setIsBye(false)
                            setOpponent("Amy Baker", 5)

                            clickSaveEdit()
                        }
                        checkMatchDetails(1, "1/8", 18, "Amy Baker", 5)
                        clickAddNewSet(1) {
                            checkOpponent("Amy Baker", 5)
                            clickNavBarItem<HeadToHeadScorePadRobot>()
                        }
                        clickSighters(1) {
                            checkSightersCount(18)
                            checkInput(3)
                            pressBack()
                        }
                        checkScreenIsShown()

                        clickEditMatchInfo(2) {
                            checkHeat("Semi")
                            checkOpponent("Claire Davids")
                            checkOpponentRank(3)
                            checkIsBye(false)
                            checkByeWithSetsWarningShown(false)

                            setIsBye(true)
                            checkByeWithSetsWarningShown(true)

                            clickSaveEdit()
                        }
                        checkMatchIsBye(2)
                    }
                }
            }
        }
    }

    @Test
    fun testTeamStandardFormatSetPointsNoRound() {
        // Set no heat
        // No total archers
        // Insert/delete end
        TODO()
    }

    @Test
    fun testIndividualCompound() {
        TODO()
    }
}
