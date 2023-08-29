package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.*
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.sightMarks.DatabaseSightMark
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.instrumentedTests.robots.*
import eywa.projectcodex.instrumentedTests.robots.shootDetails.*
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ScorePadRobot.ExpectedRowData
import eywa.projectcodex.model.SightMark
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName


/**
 * These tests span more than one screen and require an [ActivityScenario]
 */
@HiltAndroidTest
class LargeScaleInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(120)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

    private val sightMark = DatabaseSightMark(1, DEFAULT_BOW_ID, 50, true, Calendar.getInstance(), 2f)

    @Before
    fun setup() {
        hiltRule.inject()
        scenario = composeTestRule.activityRule.scenario

        // Note clearing the database instance after launching the activity causes issues with live data
        // (as DAOs are inconsistent)
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(composeTestRule.activityRule)
//        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
    }

    private fun addSimpleTestDataToDb() {
        val arrows = List(12) { i -> TestUtils.ARROWS[5].asArrowScore(1, i) }
        val shoot = DatabaseShoot(1, TestUtils.generateDate(), 1, false)
        scenario.onActivity {
            runBlocking {
                db.shootDao().insert(shoot)
                for (arrow in arrows) {
                    db.arrowScoreDao().insert(arrow)
                }
                addSightMarkToDb()
            }
        }
    }

    private suspend fun addSightMarkToDb() {
        db.insertDefaults()
        db.sightMarkDao().insert(sightMark)
    }

    /**
     * Try to run through as much of the functionality as possible
     */
    @Test
    fun mainTest() {
        scenario.onActivity {
            runBlocking {
                db.roundDao().insert(Round(1, "RoundName1", "Round Name 1", true, true))
                db.roundArrowCountDao().insert(RoundArrowCount(1, 1, 1.0, 18))
                db.roundArrowCountDao().insert(RoundArrowCount(1, 2, 1.0, 18))
                db.roundDistanceDao().insert(RoundDistance(1, 1, 1, 60))
                db.roundDistanceDao().insert(RoundDistance(1, 2, 1, 50))
            }
        }

        // TODO Add edit round info
        // TODO Add checks to make sure each step worked as intended
        composeTestRule.mainMenuRobot {
            logMessage(this::class, "View rounds (nothing inputted)")
            clickViewScores {
                clickOkOnEmptyTableDialog()
            }

            logMessage(this::class, "Start score A - default date, with round")
            clickNewScore {
                clickSelectedRound()
                clickRoundDialogRound("Round Name 1")
                clickSubmitNewScore {
                    logMessage(this::class, "Score A - open score pad - no arrows entered")
                    clickNavBarScorePad {
                        clickOkOnNoDataDialog()
                    }

                    logMessage(this::class, "Score A - open stats - no arrows entered")
                    clickNavBarStats {
                        checkRound("Round Name 1")
                        checkScore(0)
                    }

                    clickNavBarAddEnd {
                        logMessage(this::class, "Score A - enter 3 ends")
                        checkIndicatorTable(0, 0)
                        completeEnd("1")
                        completeEnd("2")
                        completeEnd("3")
                        checkIndicatorTable(36, 18)

                        logMessage(this::class, "Score A - input end help button")
                        clickHelpIcon()
                        clickHelpShowcaseClose()
                    }

                    logMessage(this::class, "Score A - open stats - some arrows entered")
                    clickNavBarStats {
                        checkRound("Round Name 1")
                        checkScore(36)
                    }

                    logMessage(this::class, "Score A - open score pad - some arrows entered")
                    clickNavBarScorePad {
                        checkScorePadData(
                                listOf(
                                        ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                        ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                        ExpectedRowData("2", "2-2-2-2-2-2", 6, 12, 0, 18),
                                        ExpectedRowData("3", "3-3-3-3-3-3", 6, 18, 0, 36),
                                        ExpectedRowData("T", "Total at 60m", 18, 36, 0, null),
                                        ExpectedRowData("GT", "Grand Total", 18, 36, 0, null),
                                )
                        )

                        logMessage(this::class, "Score A - Score pad insert end - 1")
                        clickRow(2)
                        clickInsertDropdownMenuItem {
                            checkInsertEndBefore(2)
                            completeEnd("4")
                        }

                        checkScorePadData(
                                listOf(
                                        ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                        ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                        ExpectedRowData("2", "4-4-4-4-4-4", 6, 24, 0, 30),
                                        ExpectedRowData("3", "2-2-2-2-2-2", 6, 12, 0, 42),
                                        ExpectedRowData("T", "Total at 60m", 18, 42, 0, null),
                                        ExpectedRowData("4", "3-3-3-3-3-3", 6, 18, 0, 60),
                                        ExpectedRowData("T", "Total at 50m", 6, 18, 0, null),
                                        ExpectedRowData("GT", "Grand Total", 24, 60, 0, null),
                                )
                        )

                        logMessage(this::class, "Score A - Score pad edit end - 1")
                        clickRow(3)
                        clickEditDropdownMenuItem {
                            checkEditEnd(3)
                            checkInputtedArrows(List(6) { 2 })
                            repeat(4) {
                                clickBackspace()
                            }
                            completeEnd("5", 4)
                        }

                        checkScorePadData(
                                listOf(
                                        ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                        ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                        ExpectedRowData("2", "4-4-4-4-4-4", 6, 24, 0, 30),
                                        ExpectedRowData("3", "2-2-5-5-5-5", 6, 24, 0, 54),
                                        ExpectedRowData("T", "Total at 60m", 18, 54, 0, null),
                                        ExpectedRowData("4", "3-3-3-3-3-3", 6, 18, 0, 72),
                                        ExpectedRowData("T", "Total at 50m", 6, 18, 0, null),
                                        ExpectedRowData("GT", "Grand Total", 24, 72, 0, null),
                                )
                        )

                        logMessage(this::class, "Score A - Score pad delete end - 1")
                        clickRow(2)
                        clickDeleteDropdownMenuItem(true, 2)

                        checkScorePadData(
                                listOf(
                                        ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                        ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                        ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 30),
                                        ExpectedRowData("3", "3-3-3-3-3-3", 6, 18, 0, 48),
                                        ExpectedRowData("T", "Total at 60m", 18, 48, 0, null),
                                        ExpectedRowData("GT", "Grand Total", 18, 48, 0, null),
                                )
                        )

                        logMessage(this::class, "Score A - Score pad insert end - 2")
                        clickRow(2)
                        clickInsertDropdownMenuItem {
                            checkInsertEndBefore(2)
                            completeEnd("6")
                        }

                        checkScorePadData(
                                listOf(
                                        ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                        ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                        ExpectedRowData("2", "6-6-6-6-6-6", 6, 36, 0, 42),
                                        ExpectedRowData("3", "2-2-5-5-5-5", 6, 24, 0, 66),
                                        ExpectedRowData("T", "Total at 60m", 18, 66, 0, null),
                                        ExpectedRowData("4", "3-3-3-3-3-3", 6, 18, 0, 84),
                                        ExpectedRowData("T", "Total at 50m", 6, 18, 0, null),
                                        ExpectedRowData("GT", "Grand Total", 24, 84, 0, null),
                                )
                        )

                        logMessage(this::class, "Score A - Score pad edit end - 2")
                        clickRow(2)
                        clickEditDropdownMenuItem {
                            checkEditEnd(2)
                            checkInputtedArrows(List(6) { 6 })
                            clickClear()
                            completeEnd("7")
                        }

                        checkScorePadData(
                                listOf(
                                        ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                        ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                        ExpectedRowData("2", "7-7-7-7-7-7", 6, 42, 0, 48),
                                        ExpectedRowData("3", "2-2-5-5-5-5", 6, 24, 0, 72),
                                        ExpectedRowData("T", "Total at 60m", 18, 72, 0, null),
                                        ExpectedRowData("4", "3-3-3-3-3-3", 6, 18, 0, 90),
                                        ExpectedRowData("T", "Total at 50m", 6, 18, 0, null),
                                        ExpectedRowData("GT", "Grand Total", 24, 90, 0, null),
                                )
                        )

                        logMessage(this::class, "Score A - score pad help button")
                        clickHelpIcon()
                        clickHelpShowcaseClose()

                        logMessage(this::class, "Score A - Score pad delete end - 2")
                        clickRow(2)
                        clickDeleteDropdownMenuItem(true, 2)

                        checkScorePadData(
                                listOf(
                                        ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                        ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                        ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 30),
                                        ExpectedRowData("3", "3-3-3-3-3-3", 6, 18, 0, 48),
                                        ExpectedRowData("T", "Total at 60m", 18, 48, 0, null),
                                        ExpectedRowData("GT", "Grand Total", 18, 48, 0, null),
                                )
                        )

                        logMessage(this::class, "Score A - return to main menu")
                        clickHomeIcon()
                    }
                }
            }

            logMessage(this::class, "Main menu help button 1")
            clickHelpIcon()

            logMessage(this::class, "Main menu help button 2")
            clickHelpShowcaseClose()

            logMessage(this::class, "View rounds (score A inputted)")
            clickViewScores {
                waitForHsg(0, "18/48/0")

                clickRow(0) {
                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                    ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                    ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 30),
                                    ExpectedRowData("3", "3-3-3-3-3-3", 6, 18, 0, 48),
                                    ExpectedRowData("T", "Total at 60m", 18, 48, 0, null),
                                    ExpectedRowData("GT", "Grand Total", 18, 48, 0, null),
                            )
                    )

                    logMessage(this::class, "Score A - View round add end - 1")
                    clickNavBarAddEnd {
                        checkIndicatorTable(48, 18)
                        completeEnd("1")
                        checkIndicatorTable(54, 24)
                    }
                    clickNavBarScorePad()

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                    ExpectedRowData("1", "1-1-1-1-1-1", 6, 6, 0, 6),
                                    ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 30),
                                    ExpectedRowData("3", "3-3-3-3-3-3", 6, 18, 0, 48),
                                    ExpectedRowData("T", "Total at 60m", 18, 48, 0, null),
                                    ExpectedRowData("4", "1-1-1-1-1-1", 6, 6, 0, 54),
                                    ExpectedRowData("T", "Total at 50m", 6, 6, 0, null),
                                    ExpectedRowData("GT", "Grand Total", 24, 54, 0, null),
                            )
                    )

                    logMessage(this::class, "Score A - View round edit end - 1")
                    clickRow(1)
                    clickEditDropdownMenuItem {
                        checkEditEnd(1)

                        checkInputtedArrows(List(6) { 1 })
                        clickClear()

                        completeEnd("2")
                    }

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                    ExpectedRowData("1", "2-2-2-2-2-2", 6, 12, 0, 12),
                                    ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 36),
                                    ExpectedRowData("3", "3-3-3-3-3-3", 6, 18, 0, 54),
                                    ExpectedRowData("T", "Total at 60m", 18, 54, 0, null),
                                    ExpectedRowData("4", "1-1-1-1-1-1", 6, 6, 0, 60),
                                    ExpectedRowData("T", "Total at 50m", 6, 6, 0, null),
                                    ExpectedRowData("GT", "Grand Total", 24, 60, 0, null),
                            )
                    )

                    logMessage(this::class, "Score A - View round delete end - 1")
                    clickRow(3)
                    clickDeleteDropdownMenuItem(true, 3)

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                    ExpectedRowData("1", "2-2-2-2-2-2", 6, 12, 0, 12),
                                    ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 36),
                                    ExpectedRowData("3", "1-1-1-1-1-1", 6, 6, 0, 42),
                                    ExpectedRowData("T", "Total at 60m", 18, 42, 0, null),
                                    ExpectedRowData("GT", "Grand Total", 18, 42, 0, null),
                            )
                    )

                    logMessage(this::class, "Score A - View round add end - 2")
                    clickNavBarAddEnd {
                        checkIndicatorTable(42, 18)
                        completeEnd("8")
                        checkIndicatorTable(90, 24)
                    }
                    clickNavBarScorePad()

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                    ExpectedRowData("1", "2-2-2-2-2-2", 6, 12, 0, 12),
                                    ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 36),
                                    ExpectedRowData("3", "1-1-1-1-1-1", 6, 6, 0, 42),
                                    ExpectedRowData("T", "Total at 60m", 18, 42, 0, null),
                                    ExpectedRowData("4", "8-8-8-8-8-8", 6, 48, 0, 90),
                                    ExpectedRowData("T", "Total at 50m", 6, 48, 0, null),
                                    ExpectedRowData("GT", "Grand Total", 24, 90, 0, null),
                            )
                    )

                    logMessage(this::class, "Score A - View round edit end - 2")
                    clickRow(5)
                    clickEditDropdownMenuItem {
                        checkEditEnd(4)
                        checkInputtedArrows(List(6) { 8 })
                        clickClear()
                        completeEnd("10")
                    }

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                    ExpectedRowData("1", "2-2-2-2-2-2", 6, 12, 0, 12),
                                    ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 36),
                                    ExpectedRowData("3", "1-1-1-1-1-1", 6, 6, 0, 42),
                                    ExpectedRowData("T", "Total at 60m", 18, 42, 0, null),
                                    ExpectedRowData("4", "10-10-10-10-10-10", 6, 60, 6, 102),
                                    ExpectedRowData("T", "Total at 50m", 6, 60, 6, null),
                                    ExpectedRowData("GT", "Grand Total", 24, 102, 6, null),
                            )
                    )

                    logMessage(this::class, "Score A - View round delete end - 2")
                    clickRow(5)
                    clickDeleteDropdownMenuItem(true, 4)

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                    ExpectedRowData("1", "2-2-2-2-2-2", 6, 12, 0, 12),
                                    ExpectedRowData("2", "2-2-5-5-5-5", 6, 24, 0, 36),
                                    ExpectedRowData("3", "1-1-1-1-1-1", 6, 6, 0, 42),
                                    ExpectedRowData("T", "Total at 60m", 18, 42, 0, null),
                                    ExpectedRowData("GT", "Grand Total", 18, 42, 0, null),
                            )
                    )

                    logMessage(this::class, "Score A - Complete round")
                    clickNavBarAddEnd {
                        repeat(3) {
                            completeEnd("X")
                        }

                        clickRoundCompleteOk {
                            checkScore(222)
                        }
                    }
                }
            }
        }

        logMessage(this::class, "Score A - Continue round (completed)")
        // TODO Try to continue the round from view rounds, score pad - insert, and input end (nav from score pad and stats), email score


//        logMessage(this::class, "Score A - Convert round Xs")
//        onView(withText("Score A current score")).perform(longClick())
//        onView(withText(CommonStrings.Menus.viewRoundsConvert)).perform(click())
//
//
//
//        logMessage(this::class, "Start score B - change date, no round")
//        R.id.button_main_menu__start_new_score.click()
//        R.id.text_create_round__date.click()
//        val calendar = Calendar.getInstance()
//        // Use a different hour/minute to ensure it's not overwriting the time
//        calendar.set(2040, 9, 30, 13, 15, 0)
//        onViewWithClassName(DatePicker::class.java).perform(setDatePickerValue(calendar))
//        R.id.text_create_round__time.click()
//        onViewWithClassName(TimePicker::class.java).perform(setTimePickerValue(20, 22))
//        R.id.button_create_round__submit.click()
//
//
//        logMessage(this::class, "View rounds (scores A and B inputted)")
//        pressBack()
//        R.id.button_main_menu__view_scores.click()
//        // TODO Check scores A and B inputted
//
//
//        logMessage(this::class, "Score B - Continue round")
//        onView(withText("0")).perform(longClick())
//        CustomConditionWaiter.waitFor(500)
//        onView(withText(CommonStrings.Menus.viewRoundsContinue)).perform(click())
//        completeEnd(R.id.button_arrow_inputs__score_1, activityScenario = scenario)
    }

    /**
     * Navigate to every screen and check the help button works
     */
    @Test
    fun testHelpDialogs() {
        touchEveryScreen { fragmentClass ->
            val noHelpInfo = listOf(
                    ShootDetailsStatsRobot::class,
                    AboutRobot::class,
                    ViewScoresRobot::class,
            )
            if (fragmentClass in noHelpInfo) {
                return@touchEveryScreen
            }

            cycleThroughComposeHelpDialogs()
        }
    }

    /**
     * Navigate to every screen and check that landscape mode doesn't crash the app (rotate back to portrait before
     * transitioning as not all buttons will be visible in landscape)
     */
    @Test
    fun testLandscape() {
        touchEveryScreen {
            onView(isRoot()).perform(OrientationChangeAction(scenario, OrientationChangeAction.Orientation.LANDSCAPE))
            CustomConditionWaiter.waitFor(200)
            onView(isRoot()).perform(OrientationChangeAction(scenario, OrientationChangeAction.Orientation.PORTRAIT))
            CustomConditionWaiter.waitFor(200)
        }
    }

    /**
     * @param action run once on every single fragment
     */
    private fun touchEveryScreen(
            action: BaseRobot.(KClass<out BaseRobot>) -> Unit
    ) {
        fun BaseRobot.performAction(current: KClass<out BaseRobot>) {
            logMessage(this::class, "Performing on: ${current.jvmName}")
            action(current)
        }

        addSimpleTestDataToDb()

        composeTestRule.mainMenuRobot {
            performAction(MainMenuRobot::class)

            logMessage(this::class, "Navigating to: About")
            clickAboutIcon {
                performAction(AboutRobot::class)
            }

            logMessage(this::class, "Navigating to: New score")
            clickHomeIcon()
            clickNewScore {
                performAction(NewScoreRobot::class)

                logMessage(this::class, "Navigating to: Input end")
                clickSubmitNewScore {
                    performAction(AddEndRobot::class)

                    logMessage(this::class, "Navigating to: Score pad")
                    completeEnd("2")
                    clickNavBarScorePad {
                        performAction(ScorePadRobot::class)

                        logMessage(this::class, "Navigating to: Edit end")
                        clickRow(1)
                        clickEditDropdownMenuItem {
                            performAction(EditEndRobot::class)

                            logMessage(this::class, "Navigating to: Insert end")
                            clickCancel()
                        }

                        clickRow(1)
                        clickInsertDropdownMenuItem {
                            performAction(InsertEndRobot::class)

                            logMessage(this::class, "Navigating to: Score stats")
                            clickCancel()
                        }
                    }

                    clickNavBarStats {
                        performAction(ShootDetailsStatsRobot::class)
                    }

                    logMessage(this::class, "Navigating to: Score settings")
                    clickNavBarSettings {
                        performAction(ShootDetailsSettingsRobot::class)
                    }

                    logMessage(this::class, "Navigating to: View rounds")
                    clickHomeIcon()
                }
            }

            clickViewScores {
                performAction(ViewScoresRobot::class)

                logMessage(this::class, "Navigating to: Email")
                longClickRow(0)
                clickEmailDropdownMenuItem {
                    performAction(EmailScoreRobot::class)

                    logMessage(this::class, "Navigating to: Handicap tables")
                    clickHomeIcon()
                }
            }

            clickHandicapTables {
                performAction(HandicapTablesRobot::class)

                logMessage(this::class, "Navigating to: Sight marks")
                clickHomeIcon()
            }

            clickSightMarks().apply {
                performAction(SightMarksRobot::class)

                logMessage(this::class, "Navigating to: Sight mark detail")
                clickSightMark(SightMark(sightMark))
            }.apply {
                performAction(SightMarkDetailRobot::class)
            }
        }
    }

    /**
     * Navigate to every screen and check the back button returns to the correct page and eventually, the main menu
     */
    @Test
    fun testBackButton() {
        scenario.onActivity {
            runBlocking {
                addSightMarkToDb()
            }
        }

        composeTestRule.mainMenuRobot {
            logMessage(this::class, "Main menu 1")
            pressBack()
            clickCancelOnExitDialog()

            logMessage(this::class, "About")
            clickAboutIcon {
                logMessage(this::class, " -> press back")
                pressBack()
            }
            checkScreenIsShown()

            logMessage(this::class, "New score")
            clickNewScore {
                setDate(1, 1, 2022)
                logMessage(this::class, " -> press back")
                pressBack()
            }
            checkScreenIsShown()

            logMessage(this::class, "New score -> Input end")
            clickNewScore {
                setDate(2, 1, 2022)
                clickSubmitNewScore {
                    logMessage(this::class, " -> press back")
                    pressBack()
                }
            }
            checkScreenIsShown()
            CustomConditionWaiter.waitFor(1000)


            logMessage(this::class, "New score -> Score pad")
            clickNewScore {
                clickSubmitNewScore {
                    completeEnd("2")

                    clickNavBarScorePad {
                        logMessage(this::class, " -> press back")
                        pressBack()
                    }
                }
            }
            checkScreenIsShown()

            logMessage(this::class, "Main menu 2")
            pressBack()
            clickCancelOnExitDialog()

            logMessage(this::class, "View rounds")
            clickViewScores {
                logMessage(this::class, " -> press back")
                pressBack()
            }
            checkScreenIsShown()


            val rowIndex = 0
            logMessage(this::class, "Email score")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                longClickRow(rowIndex)
                clickEmailDropdownMenuItem {
                    logMessage(this::class, " -> press back")
                    pressBack()
                }
                checkScreenIsShown()
                pressBack()
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Score pad")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    logMessage(this::class, " -> press back")
                    pressBack()
                }
                checkScreenIsShown()
                pressBack()
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Score pad -> Score stats")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    clickNavBarStats {
                        logMessage(this::class, " -> press back")
                        pressBack()
                    }
                }
                checkScreenIsShown()
                pressBack()
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Score pad -> Many")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    clickNavBarStats()
                    repeat(6) {
                        clickNavBarScorePad()
                        clickNavBarAddEnd()
                    }
                    logMessage(this::class, " -> press back")
                    pressBack()
                }
                checkScreenIsShown()
                pressBack()
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Insert end")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    waitForLoad()
                    clickRow(1)
                    clickInsertDropdownMenuItem {
                        logMessage(this::class, " -> press back")
                        pressBack()
                    }
                    waitForLoad()
                    pressBack()
                }
                checkScreenIsShown()
                pressBack()
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Edit end")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    clickRow(1)
                    clickEditDropdownMenuItem {
                        logMessage(this::class, " -> press back")
                        pressBack()
                    }
                    waitForLoad()
                    pressBack()
                }
                checkScreenIsShown()
                pressBack()
            }
            checkScreenIsShown()


            logMessage(this::class, "Handicap tables")
            clickHandicapTables {
                logMessage(this::class, " -> press back")
                pressBack()
            }


            logMessage(this::class, "Sight mark detail")
            clickSightMarks().apply {
                clickSightMark(SightMark(sightMark))
            }.apply {
                logMessage(this::class, " -> press back")
                clickAndroidBack<SightMarksRobot>()
            }.apply {
                clickAndroidBack<MainMenuRobot>()
            }


            logMessage(this::class, "Main menu 3")
            pressBack()
            clickCancelOnExitDialog()
        }
    }

    /**
     * Navigate to every screen and check the home button returns to the main menu
     */
    @Test
    fun testHomeButton() {
        scenario.onActivity {
            runBlocking {
                addSightMarkToDb()
            }
        }

        composeTestRule.mainMenuRobot {
            logMessage(this::class, "Main menu 1")
            clickHomeIcon()
            checkScreenIsShown()


            logMessage(this::class, "About")
            clickAboutIcon {
                logMessage(this::class, " -> press home")
                clickHomeIcon()
            }
            checkScreenIsShown()


            logMessage(this::class, "New score")
            clickNewScore {
                setDate(1, 1, 2022)
                logMessage(this::class, " -> press home")
                clickHomeIcon()
            }
            checkScreenIsShown()


            logMessage(this::class, "New score -> Input end")
            clickNewScore {
                setDate(2, 1, 2022)
                clickSubmitNewScore {
                    logMessage(this::class, " -> press home")
                    clickHomeIcon()
                }
            }
            checkScreenIsShown()
            CustomConditionWaiter.waitFor(1000)


            logMessage(this::class, "New score -> Score pad")
            clickNewScore {
                clickSubmitNewScore {
                    completeEnd("2")

                    clickNavBarScorePad {
                        logMessage(this::class, " -> press home")
                        clickHomeIcon()
                    }
                }
            }
            checkScreenIsShown()


            logMessage(this::class, "Main menu 2")
            clickHomeIcon()
            checkScreenIsShown()


            val rowIndex = 0
            logMessage(this::class, "View rounds")
            clickViewScores {
                logMessage(this::class, " -> press home")
                clickHomeIcon()
            }
            checkScreenIsShown()


            logMessage(this::class, "Email score")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                longClickRow(rowIndex)
                clickEmailDropdownMenuItem {
                    logMessage(this::class, " -> press home")
                    clickHomeIcon()
                }
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Score pad")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    logMessage(this::class, " -> press home")
                    clickHomeIcon()
                }
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Score pad -> Score stats")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    clickNavBarStats {
                        logMessage(this::class, " -> press home")
                        clickHomeIcon()
                    }
                }
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Score pad -> Many")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    clickNavBarStats()
                    repeat(6) {
                        clickNavBarScorePad()
                        clickNavBarAddEnd()
                    }
                    logMessage(this::class, " -> press home")
                    clickHomeIcon()
                }
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Insert end")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    clickRow(1)
                    clickInsertDropdownMenuItem {
                        logMessage(this::class, " -> press home")
                        clickHomeIcon()
                    }
                }
            }
            checkScreenIsShown()


            logMessage(this::class, "View rounds -> Edit end")
            clickViewScores {
                waitForHsg(rowIndex, "6/12/0")
                clickRow(rowIndex) {
                    clickRow(1)
                    clickEditDropdownMenuItem {
                        logMessage(this::class, " -> press home")
                        clickHomeIcon()
                    }
                }
            }
            checkScreenIsShown()


            logMessage(this::class, "Handicap tables")
            clickHandicapTables {
                logMessage(this::class, " -> press home")
                clickHomeIcon()
            }


            logMessage(this::class, "Sight mark")
            clickSightMarks().apply {
                logMessage(this::class, " -> press home")
                clickHomeIcon()
            }


            logMessage(this::class, "Sight mark detail")
            clickSightMarks().apply {
                clickSightMark(SightMark(sightMark))
            }.apply {
                logMessage(this::class, " -> press home")
                clickHomeIcon()
            }


            logMessage(this::class, "Main menu 3")
            clickHomeIcon()
            checkScreenIsShown()
        }
    }
}
