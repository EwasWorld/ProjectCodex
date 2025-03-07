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
    fun testIndividualSetPointsWithRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                val date = "10/11/2020 10:15".parseDate()
                val archerName = GridSetDsl.DEFAULT_ARCHER_NAME
                val opponentName = GridSetDsl.DEFAULT_OPPONENT_NAME
                val archerTag = GridSetDsl.DEFAULT_ARCHER_NAME
                val opponentTag = GridSetDsl.DEFAULT_OPPONENT_NAME

                selectRoundsRobot.clickSelectedRound {
                    clickRound("WA 70")
                }
                setDate(date)
                setTime(date)
                clickType(NewScoreRobot.Type.COUNT)
                clickType(NewScoreRobot.Type.HEAD_TO_HEAD)

                checkIsH2hSetPoints(true)
                checkIsH2hStandardFormat(true)
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

                        checkRows(3, archerName to false, opponentName to true)

                        checkRunningTotals(0, 0)
                        clickNextEnd()
                        checkArrowRowError("Required", archerName)
                        checkTotalRowError("Required", opponentName)

                        setArrowRow(0, archerName, archerTag, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkArrowRowError(null, archerName)
                        checkTotalRowError(null, opponentName)
                        setTotalRow(1, opponentName, opponentTag, 31)
                        checkTotalRowError("Must be between 0 and 30 (inclusive)", opponentName)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkTotalRowError(null, opponentName)

                        checkSetResult(1, HeadToHeadResult.WIN)
                        clickNextEnd()
                        checkRunningTotals(2, 0)

                        checkSetResult(2, HeadToHeadResult.INCOMPLETE)
                        setArrowRow(0, archerName, archerTag, listOf("1", "10", "10"), 21)
                        checkSetResult(2, HeadToHeadResult.INCOMPLETE)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkSetResult(2, HeadToHeadResult.LOSS)
                        clickNextEnd()
                        checkRunningTotals(2, 2)

                        setArrowRow(0, archerName, archerTag, listOf("1", "10", "10"), 21)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkSetResult(3, HeadToHeadResult.LOSS)
                        clickNextEnd()
                        checkRunningTotals(2, 4)

                        setArrowRow(0, archerName, archerTag, listOf("9", "10", "10"), 29)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkSetResult(4, HeadToHeadResult.TIE)
                        clickNextEnd()
                        checkRunningTotals(3, 5)

                        setArrowRow(0, archerName, archerTag, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkSetResult(5, HeadToHeadResult.WIN)
                        clickNextEnd()
                        checkRunningTotals(5, 5)

                        checkShootOffWinChip(2, null)
                        setArrowRow(0, archerName, archerTag, listOf("10"), 10)
                        setTotalRow(1, opponentName, opponentTag, 9)
                        checkShootOffWinChip(2, null)
                        checkShootOffSetResult(HeadToHeadResult.WIN)

                        setTotalRow(1, opponentName, opponentTag, 10)
                        checkShootOffWinChip(2, false)
                        checkShootOffSetResult(HeadToHeadResult.LOSS)
                        tapIsShootOffWin(2, true)
                        checkShootOffSetResult(HeadToHeadResult.WIN)
                        tapIsShootOffWin(2, null)
                        checkShootOffSetResult(HeadToHeadResult.TIE)
                        clickNextEnd()

                        setArrowRow(0, archerName, archerTag, listOf("10"), 10)
                        setTotalRow(1, opponentName, opponentTag, 10)
                        checkShootOffWinChip(2, false)
                        checkShootOffSetResult(HeadToHeadResult.LOSS)
                        clickNextEnd { }
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkMatchIsBye(1)
                        checkMatchDetails(1, "quarter-final (1/4)", 18, null, null)
                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2", 2) {
                                checkRow(0, archerName, archerTag, Value("1-10-10"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(2))
                            }
                            checkEnd(3, HeadToHeadResult.LOSS, "2-4", 2) {
                                checkRow(0, archerName, archerTag, Value("1-10-10"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(2))
                            }
                            checkEnd(4, HeadToHeadResult.TIE, "3-5", 2) {
                                checkRow(0, archerName, archerTag, Value("9-10-10"), 29, NoColumn, Value(1))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(1))
                            }
                            checkEnd(5, HeadToHeadResult.WIN, "5-5", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(6, HeadToHeadResult.TIE, "5-5", 3) {
                                checkRow(0, archerName, archerTag, Value("10"), 10, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 10, NoColumn, Value(0))
                                checkShootOffRow(2, null)
                            }
                            checkEnd(7, HeadToHeadResult.LOSS, "5-6", 3) {
                                checkRow(0, archerName, archerTag, Value("10"), 10, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 10, NoColumn, Value(1))
                                checkShootOffRow(2, false)
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
                        setArrowRow(0, archerName, archerTag, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        clickNextEnd()
                        checkRunningTotals(2, 0)

                        setArrowRow(0, archerName, archerTag, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        clickNextEnd()
                        checkRunningTotals(4, 0)
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkMatchIsBye(1)
                        checkMatchDetails(1, "quarter-final (1/4)", 18, null, null)

                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(7, HeadToHeadResult.LOSS, "5-6", 3) {
                                checkRow(0, archerName, archerTag, Value("10"), 10, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 10, NoColumn, Value(1))
                                checkShootOffRow(2, false)
                            }
                        }

                        checkMaxRank(3, 3)
                        checkGrid(3, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                        }
                    }

                    clickNavBarItem<HeadToHeadAddEndRobot> {
                        checkRows(3, archerName to false, opponentName to true)

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
                        checkRows(3, archerName to false, opponentName to true)

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

                        setTotalRow(0, archerName, archerTag, 30, arrows = NoColumn)
                        clickNextEnd()
                        checkNoRunningTotals()
                        checkRows(3, archerName to true)
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkGrid(3, HeadToHeadResult.UNKNOWN) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(3, HeadToHeadResult.UNKNOWN, "-", 1) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Empty)
                            }
                        }

                        openEditEnd(3, 3) {
                            checkRows(3, archerName to true)
                            gridSetDsl.checkRow(
                                    0,
                                    archerName,
                                    archerTag,
                                    NoColumn,
                                    30,
                                    NoColumn,
                                    NoColumn,
                                    isEditable = true
                            )

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
                            gridSetDsl.checkRow(
                                    0,
                                    archerName,
                                    archerTag,
                                    NoColumn,
                                    30,
                                    NoColumn,
                                    NoColumn,
                                    isEditable = true
                            )
                            checkResultRow(1, HeadToHeadResult.LOSS)
                            clickResultRow(1, HeadToHeadResult.TIE)
                            clickResultRow(1, HeadToHeadResult.WIN)
                            checkSetResult(3, HeadToHeadResult.WIN)
                            clickConfirmEdit()
                        }
                        checkScreenIsShown()

                        checkGrid(3, HeadToHeadResult.WIN) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(3, HeadToHeadResult.WIN, "6-0", 2) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win")
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

                        checkNumbersBreakdown(3, HandicapType.SELF, listOf(SELF, OPPONENT, DIFFERENCE)) {
                            checkRow(0, "Semi", 21.3f)
                            checkEndAverages(0, SELF to 26.6f, OPPONENT to 29.1f, DIFFERENCE to -2.5f)
                            checkArrowAverages(0, SELF to 8.9f, OPPONENT to 9.7f, DIFFERENCE to -0.8f)

                            checkRow(1, "Final", 0f)
                            checkEndAverages(1, SELF to 30.0f, OPPONENT to 29.0f, DIFFERENCE to 1.0f)
                            checkArrowAverages(1, SELF to 10.0f, OPPONENT to 9.7f, DIFFERENCE to 0.3f)

                            checkRow(2, "Total", 13.7f)
                            checkEndAverages(2, SELF to 27.8f, OPPONENT to 29.1f, DIFFERENCE to -1.3f)
                            checkArrowAverages(2, SELF to 9.3f, OPPONENT to 9.7f, DIFFERENCE to -0.4f)
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

                        checkCannotAddOrInsertEnd(3, 2)
                        deleteEnd(3, 2)
                        checkGrid(3, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win")
                            }
                        }

                        insertEnd(3, 2) {
                            checkRows(3, archerName to false, opponentName to true)
                            setArrowRow(0, archerName, archerTag, listOf("8", "7", "6"), 21)
                            setTotalRow(1, opponentName, opponentTag, 25)
                            clickInsertEnd()
                        }
                        checkScreenIsShown()

                        checkGrid(3, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2", 2) {
                                checkRow(0, archerName, archerTag, Value("8-7-6"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 25, NoColumn, Value(2))
                            }
                            checkEnd(3, HeadToHeadResult.WIN, "4-2", 2) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win")
                            }
                        }

                        clickEditMatchInfo(2) {
                            clickDelete()
                        }
                        checkScreenIsShown()

                        checkGrid(2, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2", 2) {
                                checkRow(0, archerName, archerTag, Value("8-7-6"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 25, NoColumn, Value(2))
                            }
                            checkEnd(3, HeadToHeadResult.WIN, "4-2", 2) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win")
                            }
                        }

                        checkMatchDetails(1, "1/8", 18, "Amy Baker", 5)
                        checkMatchCount(2)
                        clickDeleteMatch(2)
                        checkMatchCount(1)
                        checkMatchDetails(1, "1/8", 18, "Amy Baker", 5)

                        clickInsertMatch(1) {
                            setOpponent("Test Opponent", 13)
                            clickSaveEdit()
                        }
                        checkScreenIsShown()
                        checkMatchDetails(1, null, 0, "Test Opponent", 13)
                        checkMatchDetails(2, "1/8", 18, "Amy Baker", 5)
                    }
                }
            }
        }
    }

    /**
     * Team - total score - no round
     * No total archers
     * Remove heat from match 2 (shouldn't be auto set for match 3)
     * Check row change is carried through to next set and match
     * Edit main match info to add a round
     */
    @Test
    fun testTeamTotalScoreNoRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                val date = "10/11/2020 10:15".parseDate()
                val archerName = GridSetDsl.DEFAULT_ARCHER_NAME
                val teamName = GridSetDsl.DEFAULT_TEAM_MATE_NAME
                val opponentName = GridSetDsl.DEFAULT_OPPONENT_NAME
                val archerTag = GridSetDsl.DEFAULT_ARCHER_NAME
                val teamTag = GridSetDsl.TEAM_MATE_TAG
                val opponentTag = GridSetDsl.DEFAULT_OPPONENT_NAME

                selectRoundsRobot.checkSelectedRound("No Round")
                setDate(date)
                setTime(date)
                clickType(NewScoreRobot.Type.COUNT)
                clickType(NewScoreRobot.Type.HEAD_TO_HEAD)

                clickH2hSetPoints(false)
                checkIsH2hStandardFormat(true)
                setHeadToHeadFields(2, 2, null)

                clickSubmitNewScoreHeadToHead {
                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        clickEmptyScreenAddMatchButton()
                    }
                    checkScreenIsShown()

                    clickNavBarItem<HeadToHeadStatsRobot> {
                        checkDate("10 Nov 20 10:15")
                        checkRound("Head to head")
                        checkH2hInfo("Teams of 2, Total score, Rank 2")
                        checkFaces("Full")

                        checkNoMatches()
                        checkNumbersBreakdownNotShown()

                        clickNavBarItem<HeadToHeadAddMatchRobot> {}
                    }
                    checkScreenIsShown()

                    checkIsBye(false)
                    checkHeat(null)
                    checkOpponent(null)
                    checkOpponentRank(null)

                    setHeat("1/8")
                    clickStartMatch {
                        clickEditRows {
                            checkEditRowsDialog(
                                    GridSetDsl.DEFAULT_ARCHER_NAME to "Arrows",
                                    GridSetDsl.DEFAULT_TEAM_MATE_NAME to "Off",
                                    GridSetDsl.DEFAULT_TEAM_NAME to "Total",
                                    GridSetDsl.DEFAULT_OPPONENT_NAME to "Total",
                            )
                            clickEditRowsDialogRow(GridSetDsl.DEFAULT_TEAM_NAME)
                            clickEditRowsDialogRow(GridSetDsl.DEFAULT_TEAM_MATE_NAME)
                            clickEditRowsDialogRow(GridSetDsl.DEFAULT_TEAM_MATE_NAME)
                            checkEditRowsDialog(
                                    GridSetDsl.DEFAULT_ARCHER_NAME to "Arrows",
                                    GridSetDsl.DEFAULT_TEAM_MATE_NAME to "Total",
                                    GridSetDsl.DEFAULT_TEAM_NAME to "Auto",
                                    GridSetDsl.DEFAULT_OPPONENT_NAME to "Total",
                            )
                            clickOk()
                        }
                        checkRows(2, archerName to false, teamName to true, opponentName to true)
                        checkRunningTotals(0, 0)
                        setArrowRow(0, archerName, archerTag, listOf("10", "10"), 20, Value(20))
                        setTotalRow(1, teamName, teamTag, 19, Empty, NoColumn)
                        // Team total should have updated
                        checkArrowRow(0, archerName, archerTag, listOf("10", "10"), 20, Value(39))
                        setTotalRow(2, opponentName, opponentTag, 30, Empty, Value(30))
                        checkNoSetResult()
                        clickNextEnd()

                        checkRows(2, archerName to false, teamName to true, opponentName to true)
                        checkRunningTotals(39, 30)
                        setTotalRow(1, teamName, teamTag, 20, Empty, NoColumn)
                        setArrowRow(0, archerName, archerTag, listOf("9", "9"), 18, Value(38))
                        setTotalRow(2, opponentName, opponentTag, 29, Empty, Value(29))
                        clickNextEnd()

                        checkRunningTotals(77, 59)
                        setTotalRow(1, teamName, teamTag, 20, Empty, NoColumn)
                        setArrowRow(0, archerName, archerTag, listOf("9", "9"), 18, Value(38))
                        setTotalRow(2, opponentName, opponentTag, 29, Empty, Value(29))
                        clickNextEnd()

                        // Total score always goes to 4 sets
                        checkRunningTotals(115, 88)
                        setTotalRow(1, teamName, teamTag, 20, Empty, NoColumn)
                        setArrowRow(0, archerName, archerTag, listOf("5", "5"), 10, Value(30))
                        setTotalRow(2, opponentName, opponentTag, 40, Empty, Value(40))
                        clickNextEnd {
                            checkPrevious("Match 1: 1/8", "Win 145-128")
                            checkHeat("1/4")
                            setHeat(null)
                            clickStartMatch {}
                        }

                        checkRows(2, archerName to false, teamName to true, opponentName to true)
                        checkRunningTotals(0, 0)
                        setTotalRow(1, teamName, teamTag, 20, Empty, NoColumn)
                        setArrowRow(0, archerName, archerTag, listOf("10", "10"), 20, Value(40))
                        setTotalRow(2, opponentName, opponentTag, 40, Empty, Value(40))
                        clickNextEnd()

                        checkRunningTotals(40, 40)
                        setTotalRow(1, teamName, teamTag, 20, Empty, NoColumn)
                        setArrowRow(0, archerName, archerTag, listOf("10", "10"), 20, Value(40))
                        setTotalRow(2, opponentName, opponentTag, 40, Empty, Value(40))
                        clickNextEnd()

                        checkRunningTotals(80, 80)
                        setTotalRow(1, teamName, teamTag, 20, Empty, NoColumn)
                        setArrowRow(0, archerName, archerTag, listOf("10", "10"), 20, Value(40))
                        setTotalRow(2, opponentName, opponentTag, 40, Empty, Value(40))
                        clickNextEnd()

                        checkRunningTotals(120, 120)
                        setTotalRow(1, teamName, teamTag, 20, Empty, NoColumn)
                        setArrowRow(0, archerName, archerTag, listOf("10", "10"), 20, Value(40))
                        setTotalRow(2, opponentName, opponentTag, 40, Empty, Value(40))
                        clickNextEnd()

                        // Shoot off
                        checkRunningTotals(160, 160)
                        setTotalRow(1, teamName, teamTag, 10, Empty, NoColumn)
                        setArrowRow(0, archerName, archerTag, listOf("10"), 10, Value(20))
                        setTotalRow(2, opponentName, opponentTag, 19, Empty, Value(19))
                        checkShootOffWinChip(3, null)

                        setTotalRow(2, opponentName, opponentTag, 20, Empty, Value(20))
                        tapIsShootOffWin(3, true)
                        clickNextEnd {
                            checkPrevious("Match 2", "Win 180-180")
                            checkHeat(null)

                            clickNavBarItem<HeadToHeadScorePadRobot> {
                                checkGrid(1, HeadToHeadResult.WIN) {
                                    checkEnd(1, null, "39-30", 3) {
                                        checkRow(0, archerName, archerTag, Value("10-10"), 20, Value(39))
                                        checkRow(1, teamName, teamTag, Empty, 19, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 30, NoColumn)
                                    }
                                    checkEnd(2, null, "77-59", 3) {
                                        checkRow(0, archerName, archerTag, Value("9-9"), 18, Value(38))
                                        checkRow(1, teamName, teamTag, Empty, 20, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 29, NoColumn)
                                    }
                                    checkEnd(3, null, "115-88", 3) {
                                        checkRow(0, archerName, archerTag, Value("9-9"), 18, Value(38))
                                        checkRow(1, teamName, teamTag, Empty, 20, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 29, NoColumn)
                                    }
                                    checkEnd(4, null, "145-128", 3) {
                                        checkRow(0, archerName, archerTag, Value("5-5"), 10, Value(30))
                                        checkRow(1, teamName, teamTag, Empty, 20, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 40, NoColumn)
                                    }
                                }

                                checkGrid(2, HeadToHeadResult.WIN) {
                                    checkEnd(1, null, "40-40", 3) {
                                        checkRow(0, archerName, archerTag, Value("10-10"), 20, Value(40))
                                        checkRow(1, teamName, teamTag, Empty, 20, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 40, NoColumn)
                                    }
                                    checkEnd(2, null, "80-80", 3) {
                                        checkRow(0, archerName, archerTag, Value("10-10"), 20, Value(40))
                                        checkRow(1, teamName, teamTag, Empty, 20, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 40, NoColumn)
                                    }
                                    checkEnd(3, null, "120-120", 3) {
                                        checkRow(0, archerName, archerTag, Value("10-10"), 20, Value(40))
                                        checkRow(1, teamName, teamTag, Empty, 20, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 40, NoColumn)
                                    }
                                    checkEnd(4, null, "160-160", 3) {
                                        checkRow(0, archerName, archerTag, Value("10-10"), 20, Value(40))
                                        checkRow(1, teamName, teamTag, Empty, 20, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 40, NoColumn)
                                    }
                                    checkEnd(5, null, "180-180", 4) {
                                        checkRow(0, archerName, archerTag, Value("10"), 10, Value(20))
                                        checkRow(1, teamName, teamTag, Empty, 10, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 20, NoColumn)
                                        checkShootOffRow(3, true)
                                    }
                                }

                                openEditEnd(2, 5) {
                                    tapIsShootOffWin(3, null)
                                    tapIsShootOffWin(3, false)
                                    clickConfirmEdit()
                                }
                                checkGrid(2, HeadToHeadResult.LOSS) {
                                    checkEnd(5, null, "180-180", 4) {
                                        checkRow(0, archerName, archerTag, Value("10"), 10, Value(20))
                                        checkRow(1, teamName, teamTag, Empty, 10, NoCell)
                                        checkRow(2, opponentName, opponentTag, Empty, 20, NoColumn)
                                        checkShootOffRow(3, false)
                                    }
                                }
                            }

                            clickNavBarItem<HeadToHeadStatsRobot> {
                                checkDate("10 Nov 20 10:15")
                                checkRound("Head to head")
                                checkH2hInfo("Teams of 2, Total score, Rank 2")
                                checkFaces("Full")

                                checkMatchRow(0, "1/8", "-", "-", "145-128 Win")
                                checkMatchRow(1, "2", "-", "-", "180-180 Loss")

                                handicapAndClassificationRobot.checkClassificationDoesNotExist()
                                handicapAndClassificationRobot.checkHandicapDoesNotExist()

                                checkNumbersBreakdown(3, HandicapType.SELF, listOf(SELF, TEAM, OPPONENT, DIFFERENCE)) {
                                    checkRow(0, "1/8", null)
                                    checkEndAverages(0, SELF to 16.5f, OPPONENT to 16f, DIFFERENCE to 0.5f)
                                    checkArrowAverages(0, SELF to 8.3f, OPPONENT to 8f, DIFFERENCE to 0.3f)

                                    checkRow(1, "2", null)
                                    checkEndAverages(1, SELF to 20f, OPPONENT to 20f, DIFFERENCE to 0f)
                                    checkArrowAverages(1, SELF to 10f, OPPONENT to 10f, DIFFERENCE to 0f)

                                    checkRow(2, "Total", null)
                                    checkEndAverages(2, SELF to 18.4f, OPPONENT to 18.1f, DIFFERENCE to 0.2f)
                                    checkArrowAverages(2, SELF to 9.2f, OPPONENT to 9.1f, DIFFERENCE to 0.1f)
                                }

                                clickEditMainInfo {
                                    selectRoundsRobot.clickSelectedRound {
                                        clickRound("WA 70")
                                    }
                                    clickSubmitEditScore()
                                }
                                checkScreenIsShown()
                                checkRound("H2H: WA 70")


                                handicapAndClassificationRobot.checkClassification(
                                        Classification.ELITE_MASTER_BOWMAN,
                                        false
                                )
                                handicapAndClassificationRobot.checkHandicap(16)

                                checkNumbersBreakdown(3, HandicapType.SELF, listOf(SELF, TEAM, OPPONENT, DIFFERENCE)) {
                                    checkRow(0, "1/8", 30.5f)
                                    checkEndAverages(0, SELF to 16.5f, OPPONENT to 16f, DIFFERENCE to 0.5f)
                                    checkArrowAverages(0, SELF to 8.3f, OPPONENT to 8f, DIFFERENCE to 0.3f)

                                    checkRow(1, "2", 0f)
                                    checkEndAverages(1, SELF to 20f, OPPONENT to 20f, DIFFERENCE to 0f)
                                    checkArrowAverages(1, SELF to 10f, OPPONENT to 10f, DIFFERENCE to 0f)

                                    checkRow(2, "Total", 15.8f)
                                    checkEndAverages(2, SELF to 18.4f, OPPONENT to 18.1f, DIFFERENCE to 0.2f)
                                    checkArrowAverages(2, SELF to 9.2f, OPPONENT to 9.1f, DIFFERENCE to 0.1f)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Team - total score - with round
     * Check team HC shows correctly on stats screen
     */
    @Test
    fun testTeamTotalScoreRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                val date = "10/11/2020 10:15".parseDate()
                val teamName = GridSetDsl.DEFAULT_TEAM_NAME
                val opponentName = GridSetDsl.DEFAULT_OPPONENT_NAME
                val teamTag = GridSetDsl.DEFAULT_TEAM_NAME
                val opponentTag = GridSetDsl.DEFAULT_OPPONENT_NAME

                selectRoundsRobot.clickSelectedRound {
                    clickRound("WA 70")
                }
                setDate(date)
                setTime(date)
                clickType(NewScoreRobot.Type.COUNT)
                clickType(NewScoreRobot.Type.HEAD_TO_HEAD)

                clickH2hSetPoints(false)
                checkIsH2hStandardFormat(true)
                setHeadToHeadFields(2, 2, null)

                clickSubmitNewScoreHeadToHead {
                    clickStartMatch {
                        clickEditRows {
                            checkEditRowsDialog(
                                    GridSetDsl.DEFAULT_ARCHER_NAME to "Arrows",
                                    GridSetDsl.DEFAULT_TEAM_MATE_NAME to "Off",
                                    GridSetDsl.DEFAULT_TEAM_NAME to "Total",
                                    GridSetDsl.DEFAULT_OPPONENT_NAME to "Total",
                            )
                            clickEditRowsDialogRow(GridSetDsl.DEFAULT_ARCHER_NAME)
                            clickEditRowsDialogRow(GridSetDsl.DEFAULT_ARCHER_NAME)
                            checkEditRowsDialog(
                                    GridSetDsl.DEFAULT_ARCHER_NAME to "Off",
                                    GridSetDsl.DEFAULT_TEAM_MATE_NAME to "Off",
                                    GridSetDsl.DEFAULT_TEAM_NAME to "Total",
                                    GridSetDsl.DEFAULT_OPPONENT_NAME to "Total",
                            )
                            clickOk()
                        }

                        checkRows(2, teamName to true, opponentName to true)
                        checkRunningTotals(0, 0)
                        setTotalRow(0, teamName, teamTag, 32, NoColumn)
                        setTotalRow(1, opponentName, opponentTag, 36, NoColumn)
                        clickNextEnd()

                        clickNavBarItem<HeadToHeadStatsRobot> {
                            checkDate("10 Nov 20 10:15")
                            checkRound("H2H: WA 70")
                            checkH2hInfo("Teams of 2, Total score, Rank 2")
                            checkFaces("Full")

                            handicapAndClassificationRobot.checkClassification(
                                    Classification.BOWMAN_1ST_CLASS,
                                    false,
                            )
                            handicapAndClassificationRobot.checkHandicap(34)

                            checkNumbersBreakdown(1, HandicapType.TEAM, listOf(TEAM, OPPONENT, DIFFERENCE)) {
                                checkRow(0, "1", 33.5f)
                                checkEndAverages(0, TEAM to 16f, OPPONENT to 18f, DIFFERENCE to -2f)
                                checkArrowAverages(0, TEAM to 8f, OPPONENT to 9f, DIFFERENCE to -1f)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testIndividualNonStandardFormat() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                val date = "10/11/2020 10:15".parseDate()
                val archerName = GridSetDsl.DEFAULT_ARCHER_NAME
                val opponentName = GridSetDsl.DEFAULT_OPPONENT_NAME
                val archerTag = GridSetDsl.DEFAULT_ARCHER_NAME
                val opponentTag = GridSetDsl.DEFAULT_OPPONENT_NAME

                selectRoundsRobot.clickSelectedRound {
                    clickRound("WA 70")
                }
                setDate(date)
                setTime(date)
                clickType(NewScoreRobot.Type.COUNT)
                clickType(NewScoreRobot.Type.HEAD_TO_HEAD)

                checkIsH2hSetPoints(true)
                clickH2hStandardFormat(false)
                setHeadToHeadFields(1, 2, 6)

                clickSubmitNewScoreHeadToHead {
                    clickNavBarItem<HeadToHeadStatsRobot> {
                        checkDate("10 Nov 20 10:15")
                        checkRound("H2H: WA 70")
                        checkH2hInfo("Individual, Set points, Rank 2 of 6\nNon-standard format")
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

                        checkRows(3, archerName to false, opponentName to true)

                        checkRunningTotals(0, 0)
                        clickNextEnd()
                        checkArrowRowError("Required", archerName)
                        checkTotalRowError("Required", opponentName)

                        setArrowRow(0, archerName, archerTag, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkArrowRowError(null, archerName)
                        checkTotalRowError(null, opponentName)
                        setTotalRow(1, opponentName, opponentTag, 31)
                        checkTotalRowError("Must be between 0 and 30 (inclusive)", opponentName)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkTotalRowError(null, opponentName)

                        checkSetResult(1, HeadToHeadResult.WIN)
                        clickNextEnd()
                        checkRunningTotals(2, 0)

                        checkSetResult(2, HeadToHeadResult.INCOMPLETE)
                        setArrowRow(0, archerName, archerTag, listOf("1", "10", "10"), 21)
                        checkSetResult(2, HeadToHeadResult.INCOMPLETE)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkSetResult(2, HeadToHeadResult.LOSS)
                        clickNextEnd()
                        checkRunningTotals(2, 2)

                        setArrowRow(0, archerName, archerTag, listOf("1", "10", "10"), 21)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkSetResult(3, HeadToHeadResult.LOSS)
                        clickNextEnd()
                        checkRunningTotals(2, 4)

                        setArrowRow(0, archerName, archerTag, listOf("9", "10", "10"), 29)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkSetResult(4, HeadToHeadResult.TIE)
                        clickNextEnd()
                        checkRunningTotals(3, 5)

                        setArrowRow(0, archerName, archerTag, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        checkSetResult(5, HeadToHeadResult.WIN)
                        clickNextEnd()
                        checkRunningTotals(5, 5)

                        tapIsShootOff(true)
                        checkShootOffWinChip(2, null)
                        setArrowRow(0, archerName, archerTag, listOf("10"), 10)
                        setTotalRow(1, opponentName, opponentTag, 9)
                        checkShootOffWinChip(2, null)
                        checkShootOffSetResult(HeadToHeadResult.WIN)

                        setTotalRow(1, opponentName, opponentTag, 10)
                        checkShootOffWinChip(2, false)
                        checkShootOffSetResult(HeadToHeadResult.LOSS)
                        tapIsShootOffWin(2, true)
                        checkShootOffSetResult(HeadToHeadResult.WIN)
                        tapIsShootOffWin(2, null)
                        checkShootOffSetResult(HeadToHeadResult.TIE)
                        tapIsShootOffWin(2, false)
                        checkShootOffSetResult(HeadToHeadResult.LOSS)
                        clickNextEnd()
                        clickCreateNextMatch { }
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkMatchIsBye(1)
                        checkMatchDetails(1, "quarter-final (1/4)", 18, null, null)
                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2", 2) {
                                checkRow(0, archerName, archerTag, Value("1-10-10"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(2))
                            }
                            checkEnd(3, HeadToHeadResult.LOSS, "2-4", 2) {
                                checkRow(0, archerName, archerTag, Value("1-10-10"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(2))
                            }
                            checkEnd(4, HeadToHeadResult.TIE, "3-5", 2) {
                                checkRow(0, archerName, archerTag, Value("9-10-10"), 29, NoColumn, Value(1))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(1))
                            }
                            checkEnd(5, HeadToHeadResult.WIN, "5-5", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(6, HeadToHeadResult.LOSS, "5-6", 3) {
                                checkRow(0, archerName, archerTag, Value("10"), 10, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 10, NoColumn, Value(1))
                                checkShootOffRow(2, false)
                            }
                        }
                        clickNavBarItem<HeadToHeadAddEndRobot> {
                            clickCreateNextMatch { }
                        }
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
                        setArrowRow(0, archerName, archerTag, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        clickNextEnd()
                        checkRunningTotals(2, 0)

                        setArrowRow(0, archerName, archerTag, listOf("10", "10", "10"), 30)
                        setTotalRow(1, opponentName, opponentTag, 29)
                        clickNextEnd()
                        checkRunningTotals(4, 0)
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkMatchIsBye(1)
                        checkMatchDetails(1, "quarter-final (1/4)", 18, null, null)

                        checkGrid(2, HeadToHeadResult.LOSS) {
                            checkEnd(6, HeadToHeadResult.LOSS, "5-6", 3) {
                                checkRow(0, archerName, archerTag, Value("10"), 10, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 10, NoColumn, Value(1))
                                checkShootOffRow(2, false)
                            }
                        }

                        checkMaxRank(3, 3)
                        checkGrid(3, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                        }
                    }

                    clickNavBarItem<HeadToHeadAddEndRobot> {
                        checkRows(3, archerName to false, opponentName to true)

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
                        checkRows(3, archerName to false, opponentName to true)

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

                        setTotalRow(0, archerName, archerTag, 30, arrows = NoColumn)
                        clickNextEnd()
                        checkNoRunningTotals()
                        checkRows(3, archerName to true)
                    }

                    clickNavBarItem<HeadToHeadScorePadRobot> {
                        checkGrid(3, HeadToHeadResult.UNKNOWN) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(3, HeadToHeadResult.UNKNOWN, "-", 1) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Empty)
                            }
                        }

                        openEditEnd(3, 3) {
                            checkRows(3, archerName to true)
                            gridSetDsl.checkRow(
                                    0,
                                    archerName,
                                    archerTag,
                                    NoColumn,
                                    30,
                                    NoColumn,
                                    NoColumn,
                                    isEditable = true
                            )

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
                            gridSetDsl.checkRow(
                                    0,
                                    archerName,
                                    archerTag,
                                    NoColumn,
                                    30,
                                    NoColumn,
                                    NoColumn,
                                    isEditable = true
                            )
                            checkResultRow(1, HeadToHeadResult.LOSS)
                            clickResultRow(1, HeadToHeadResult.TIE)
                            clickResultRow(1, HeadToHeadResult.WIN)
                            checkSetResult(3, HeadToHeadResult.WIN)
                            clickConfirmEdit()
                        }
                        checkScreenIsShown()

                        checkGrid(3, HeadToHeadResult.WIN) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(3, HeadToHeadResult.WIN, "6-0", 2) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win")
                            }
                        }
                    }

                    clickNavBarItem<HeadToHeadStatsRobot> {
                        checkDate("10 Nov 20 10:15")
                        checkRound("H2H: WA 70")
                        checkH2hInfo("Individual, Set points, Rank 2 of 6\nNon-standard format")
                        checkFaces("Full")

                        checkMatchRow(0, "1/4", "-", "-", "Bye")
                        checkMatchRow(1, "Semi", "Claire Davids", "3", "5-6 Loss")
                        checkMatchRow(2, "Final", "Emma Fitzgerald", "1", "6-0 Win")

                        handicapAndClassificationRobot.checkClassification(
                                Classification.ELITE_MASTER_BOWMAN,
                                false
                        )
                        handicapAndClassificationRobot.checkHandicap(14)

                        checkNumbersBreakdown(3, HandicapType.SELF, listOf(SELF, OPPONENT, DIFFERENCE)) {
                            checkRow(0, "Semi", 22.5f)
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

                        // Non-standard format should always allow adding
                        checkAddEndOptionAvailable(3)
                        deleteEnd(3, 2)
                        checkGrid(3, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.WIN, "4-0", 2) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win")
                            }
                        }

                        insertEnd(3, 2) {
                            checkRows(3, archerName to false, opponentName to true)
                            setArrowRow(0, archerName, archerTag, listOf("8", "7", "6"), 21)
                            setTotalRow(1, opponentName, opponentTag, 25)
                            clickInsertEnd()
                        }
                        checkScreenIsShown()

                        checkGrid(3, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2", 2) {
                                checkRow(0, archerName, archerTag, Value("8-7-6"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 25, NoColumn, Value(2))
                            }
                            checkEnd(3, HeadToHeadResult.WIN, "4-2", 2) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win")
                            }
                        }

                        clickEditMatchInfo(2) {
                            clickDelete()
                        }
                        checkScreenIsShown()

                        checkGrid(2, HeadToHeadResult.INCOMPLETE) {
                            checkEnd(1, HeadToHeadResult.WIN, "2-0", 2) {
                                checkRow(0, archerName, archerTag, Value("10-10-10"), 30, NoColumn, Value(2))
                                checkRow(1, opponentName, opponentTag, Empty, 29, NoColumn, Value(0))
                            }
                            checkEnd(2, HeadToHeadResult.LOSS, "2-2", 2) {
                                checkRow(0, archerName, archerTag, Value("8-7-6"), 21, NoColumn, Value(0))
                                checkRow(1, opponentName, opponentTag, Empty, 25, NoColumn, Value(2))
                            }
                            checkEnd(3, HeadToHeadResult.WIN, "4-2", 2) {
                                checkRow(0, archerName, archerTag, Empty, 30, NoColumn, Value(2))
                                checkResultsRow(1, "Win")
                            }
                        }

                        checkMatchDetails(1, "1/8", 18, "Amy Baker", 5)
                        checkMatchCount(2)
                        clickDeleteMatch(2)
                        checkMatchCount(1)
                        checkMatchDetails(1, "1/8", 18, "Amy Baker", 5)

                        clickInsertMatch(1) {
                            setOpponent("Test Opponent", 13)
                            clickSaveEdit()
                        }
                        checkScreenIsShown()
                        checkMatchDetails(1, null, 0, "Test Opponent", 13)
                        checkMatchDetails(2, "1/8", 18, "Amy Baker", 5)
                    }
                }
            }
        }
    }
}
