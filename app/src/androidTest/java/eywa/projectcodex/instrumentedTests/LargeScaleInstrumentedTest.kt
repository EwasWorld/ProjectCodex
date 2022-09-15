package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.R
import eywa.projectcodex.common.*
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.components.about.AboutFragment
import eywa.projectcodex.components.archerRoundScore.archerRoundStats.ArcherRoundStatsFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.EditEndFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.InputEndFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.InsertEndFragment
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.mainMenu.MainMenuComposeFragment
import eywa.projectcodex.components.newScore.NewScoreFragment
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresFragment
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.instrumentedTests.daggerObjects.DatabaseDaggerTestModule
import eywa.projectcodex.instrumentedTests.robots.composeHelpRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.rules.Timeout
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * These tests span more than one screen and require an [ActivityScenario]
 */
class LargeScaleInstrumentedTest {
    companion object {
        init {
            SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(120)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private lateinit var navController: NavController

    private val closeHelpString = "Close help"
    private val nextHelpString = "Next"
    private val helpFadeTime = 300L

    @Before
    fun setup() {
        scenario = composeTestRule.activityRule.scenario

        // Note clearing the database instance after launching the activity causes issues with live data
        // (as DAOs are inconsistent)
        scenario.onActivity {
            db = DatabaseDaggerTestModule.scoresRoomDatabase
            navController = it.navHostFragment.navController
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(composeTestRule.activityRule)
        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
    }

    private fun addSimpleTestDataToDb() {
        val arrows = List(12) { i -> TestUtils.ARROWS[5].toArrowValue(1, i) }
        val archerRound = ArcherRound(1, TestUtils.generateDate(), 1, false)
        scenario.onActivity {
            runBlocking {
                for (arrow in arrows) {
                    db.arrowValueDao().insert(arrow)
                }
                db.archerRoundDao().insert(archerRound)
            }
        }
    }

    /**
     * Try to run through as much of the functionality as possible
     */
    @Test
    fun mainTest() {
        // TODO Add edit round info
        // TODO Add checks to make sure each step worked as intended
        ConditionWatcher.setTimeoutLimit(15000)
        composeTestRule.mainMenuRobot {
            logMessage(this::class, "View rounds (nothing inputted)")
            clickViewScores()
        }
        clickAlertDialog(CommonStrings.Dialogs.emptyTable)

        composeTestRule.mainMenuRobot {
            logMessage(this::class, "Start score A - default date, with round")
            clickNewScore()
        }

        CustomConditionWaiter.waitForUpdateRoundsTaskToFinish(scenario)
        R.id.spinner_create_round__round.clickSpinnerItem("Short Metric")
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))


        logMessage(this::class, "Score A - open score pad - no arrows entered")
        R.id.scorePadFragment.click()
        clickAlertDialog(CommonStrings.Dialogs.emptyTable)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))


        logMessage(this::class, "Score A - open stats - no arrows entered")
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class))
        R.id.inputEndFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))


        logMessage(this::class, "Score A - enter 3 ends")
        completeEnd(R.id.button_arrow_inputs__score_1, activityScenario = scenario)
        completeEnd(R.id.button_arrow_inputs__score_2, activityScenario = scenario)
        completeEnd(R.id.button_arrow_inputs__score_3, activityScenario = scenario)


        logMessage(this::class, "Score A - input end help button")
        R.id.action_bar__help.click()
        onView(withText(closeHelpString)).perform(click())
        CustomConditionWaiter.waitFor(helpFadeTime)


        logMessage(this::class, "Score A - open stats - some arrows entered")
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class))


        logMessage(this::class, "Score A - open score pad - some arrows entered")
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))


        logMessage(this::class, "Score A - Score pad insert end - 1")
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InsertEndFragment::class))
        completeEnd(
                R.id.button_arrow_inputs__score_4,
                activityScenario = scenario,
                submitButtonId = R.id.button_insert_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))


        logMessage(this::class, "Score A - Score pad edit end - 1")
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class))
        repeat(4) { R.id.button_end_inputs__backspace.click() }
        completeEnd(
                R.id.button_arrow_inputs__score_5,
                activityScenario = scenario,
                submitButtonId = R.id.button_edit_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))


        logMessage(this::class, "Score A - Score pad delete end - 1")
        onView(withText("4-4-4-4-4-4")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadDeleteEnd)).perform(click())


        logMessage(this::class, "Score A - Score pad insert end - 2")
        onView(withText("5-5-5-5-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InsertEndFragment::class))
        completeEnd(
                R.id.button_arrow_inputs__score_6,
                activityScenario = scenario,
                submitButtonId = R.id.button_insert_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))


        logMessage(this::class, "Score A - Score pad edit end - 2")
        onView(withText("6-6-6-6-6-6")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class))
        R.id.button_end_inputs__clear.click()
        completeEnd(
                R.id.button_arrow_inputs__score_7,
                activityScenario = scenario,
                submitButtonId = R.id.button_edit_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))


        logMessage(this::class, "Score A - score pad help button")
        R.id.action_bar__help.click()
        onView(withText(closeHelpString)).perform(click())
        CustomConditionWaiter.waitFor(helpFadeTime)


        logMessage(this::class, "Score A - Score pad delete end - 2")
        onView(withText("7-7-7-7-7-7")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadDeleteEnd)).perform(click())


        logMessage(this::class, "Score A - return to main menu")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        composeTestRule.composeHelpRobot {
            logMessage(this::class, "Main menu help button 1")
            clickHelpIcon()

            logMessage(this::class, "Main menu help button 2")
            clickClose()
        }


        logMessage(this::class, "View rounds (score A inputted)")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }


        logMessage(this::class, "Score A - View round insert end - 1")
        CustomConditionWaiter.waitForTextToAppear("18/48/0", CustomConditionWaiter.Companion.ClickType.CLICK)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        R.id.inputEndFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        completeEnd(R.id.button_arrow_inputs__score_1, activityScenario = scenario)


        logMessage(this::class, "Score A - View round edit end - 1")
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        onView(withIndex(withText("1-1-1-1-1-1"), 0)).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class))
        R.id.button_end_inputs__clear.click()
        completeEnd(
                R.id.button_arrow_inputs__score_2,
                activityScenario = scenario,
                submitButtonId = R.id.button_edit_end__complete
        )


        logMessage(this::class, "Score A - View round delete end - 1")
        onView(withText("3-3-3-3-3-3")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadDeleteEnd)).perform(click())


        logMessage(this::class, "Score A - View round insert end - 2")
        R.id.inputEndFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        completeEnd(R.id.button_arrow_inputs__score_8, activityScenario = scenario)


        logMessage(this::class, "Score A - View round edit end - 2")
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        onView(withIndex(withText("8-8-8-8-8-8"), 0)).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class))
        R.id.button_end_inputs__clear.click()
        completeEnd(
                R.id.button_arrow_inputs__score_9,
                activityScenario = scenario,
                submitButtonId = R.id.button_edit_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))


        logMessage(this::class, "Score A - View round delete end - 2")
        onView(withText("9-9-9-9-9-9")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadDeleteEnd)).perform(click())


        logMessage(this::class, "Score A - Complete round")
        R.id.inputEndFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        repeat(9) { completeEnd(R.id.button_arrow_inputs__score_x, activityScenario = scenario) }
        clickAlertDialog(CommonStrings.Dialogs.inputEndRoundComplete)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class))


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
        touchEveryScreen(listOf(ArcherRoundStatsFragment::class, AboutFragment::class)) { fragmentClass ->
            if (fragmentClass in listOf(
                        MainMenuComposeFragment::class
                )
            ) {
                cycleThroughComposeHelpDialogs()
                return@touchEveryScreen
            }

            cycleThroughHelpDialogs()

            if (fragmentClass == ViewScoresFragment::class) {
                R.id.button_view_scores__start_multi_select.click()
                cycleThroughHelpDialogs()
            }
        }
    }

    private fun cycleThroughComposeHelpDialogs() {
        composeTestRule.composeHelpRobot {
            clickHelpIcon()
            while (true) {
                checkHelpIsDisplayed()
                try {
                    clickNext()
                }
                catch (e: AssertionError) {
                    clickClose()
                    break
                }
            }
        }
    }

    private fun cycleThroughHelpDialogs() {
        R.id.action_bar__help.click()
        try {
            while (true) {
                CustomConditionWaiter.waitFor(helpFadeTime)
                onViewWithClassName(MaterialShowcaseView::class.java).check(matches(isDisplayed()))
                onView(withText(nextHelpString)).perform(click())
            }
        }
        catch (e: NoMatchingViewException) {
        }
        CustomConditionWaiter.waitFor(helpFadeTime)
        onView(withText(closeHelpString)).perform(click())
        CustomConditionWaiter.waitFor(helpFadeTime)
    }

    /**
     * Navigate to every screen and check that landscape mode doesn't crash the app (rotate back to portrait before
     * transitioning as not all buttons will be visible in landscape)
     */
    @Test
    fun testLandscape() {
        ConditionWatcher.setTimeoutLimit(5000)
        touchEveryScreen {
            onView(isRoot()).perform(OrientationChangeAction(scenario, OrientationChangeAction.Orientation.LANDSCAPE))
            onView(isRoot()).perform(OrientationChangeAction(scenario, OrientationChangeAction.Orientation.PORTRAIT))
        }
    }

    /**
     * @param action run once on every single fragment
     * @param TestList do not run [action] on these fragments
     */
    private fun touchEveryScreen(
            TestList: List<KClass<out Fragment>> = listOf(),
            action: (KClass<out Fragment>) -> Unit
    ) {
        fun performAction(current: KClass<out Fragment>) {
            if (TestList.contains(current)) {
                logMessage(this::class, "Testd: ${current.jvmName}")
                return
            }
            logMessage(this::class, "Performing on: ${current.jvmName}")
            CustomConditionWaiter.waitForFragmentToShow(scenario, (current))
            action(current)
        }

        addSimpleTestDataToDb()

        performAction(MainMenuComposeFragment::class)

        logMessage(this::class, "Navigating to: About")
        R.id.action_bar__about.click()
        performAction(AboutFragment::class)

        logMessage(this::class, "Navigating to: New score")
        R.id.action_bar__home.click()
        composeTestRule.mainMenuRobot {
            clickNewScore()
        }
        performAction(NewScoreFragment::class)

        logMessage(this::class, "Navigating to: Input end")
        R.id.button_create_round__submit.click()
        performAction(InputEndFragment::class)

        logMessage(this::class, "Navigating to: Score pad")
        for (i in 0 until 6) {
            R.id.button_arrow_inputs__score_2.click()
        }
        R.id.button_input_end__next_end.click()
        R.id.scorePadFragment.click()
        performAction(ScorePadFragment::class)

        logMessage(this::class, "Navigating to: Edit end")
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class))
        performAction(EditEndFragment::class)

        logMessage(this::class, "Navigating to: Insert end")
        R.id.button_edit_end__cancel.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        performAction(InsertEndFragment::class)

        logMessage(this::class, "Navigating to: Score stats")
        R.id.button_insert_end__cancel.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        R.id.archerRoundStatsFragment.click()
        performAction(ArcherRoundStatsFragment::class)

        logMessage(this::class, "Navigating to: View rounds")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        pressBack()
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        performAction(ViewScoresFragment::class)

        logMessage(this::class, "Navigating to: Email")
        onView(withIndex(withId(R.id.layout_vs_round_item), 0)).perform(longClick())
        CustomConditionWaiter.waitForMenuItemAndPerform(CommonStrings.Menus.viewRoundsEmail)
        performAction(EmailScoresFragment::class)
    }

    /**
     * Testing the transitions between Input End, Score Pad, and Stats
     */
    @Test
    fun testArcherRoundScoreBottomNavigationRoundComplete() {
        testArcherRoundScoreBottomNavigationAux(true)
    }


    /**
     * Testing the transitions between Input End, Score Pad, and Stats
     */
    @Test
    fun testArcherRoundScoreBottomNavigationRoundIncomplete() {
        testArcherRoundScoreBottomNavigationAux(false)
    }

    private fun testArcherRoundScoreBottomNavigationAux(roundComplete: Boolean) {
        fun moveTo(destinationId: Int, expectedToWork: Boolean) {
            val expected = if (expectedToWork) destinationId else navController.currentDestination!!.id
            destinationId.click()
            Assert.assertEquals(expected, navController.currentDestination?.id)
        }

        val arrowsInRound = 36
        val arrowScore = 5

        val arrowCount = if (roundComplete) arrowsInRound else arrowsInRound - 6
        scenario.onActivity {
            runBlocking {
                List(arrowCount) { index -> ArrowValue(1, index + 1, arrowScore, false) }.forEach { arrow ->
                    db.arrowValueDao().insert(arrow)
                }
                db.archerRoundDao().insert(
                        ArcherRound(1, TestUtils.generateDate(), 1, true, roundId = 1)
                )
                db.roundDao().insert(Round(1, "test", "test", false, false, listOf()))
                db.roundArrowCountDao().insert(RoundArrowCount(1, 1, 10.0, arrowsInRound))
                db.roundDistanceDao().insert(RoundDistance(1, 1, 1, 20))
            }
        }
        CustomConditionWaiter.waitForScorePadToOpen(
                composeTestRule,
                "%d/%d/%d".format(arrowCount, arrowScore * arrowCount, 0)
        )
        var tableView: TableView? = null
        scenario.onActivity {
            tableView = it.findViewById(R.id.table_view_score_pad)
        }
        CustomConditionWaiter.waitForRowToAppear(tableView!!, (0))

        // Score Pad -> Input End
        moveTo(R.id.inputEndFragment, !roundComplete)

        if (!roundComplete) {
            // Input End -> Score Pad
            moveTo(R.id.scorePadFragment, true)
        }

        // Score Pad -> Stats
        moveTo(R.id.archerRoundStatsFragment, true)

        // Stats -> Input End
        moveTo(R.id.inputEndFragment, !roundComplete)

        if (!roundComplete) {
            // Input End -> Stats
            moveTo(R.id.archerRoundStatsFragment, true)
        }

        // Stats -> Score Pad
        moveTo(R.id.scorePadFragment, true)
    }

    /**
     * Navigate to every screen and check the back button returns to the correct page and eventually, the main menu
     */
    @Test
    fun testBackButton() {
        ConditionWatcher.setTimeoutLimit(3000)


        logMessage(this::class, "Main menu 1")
        pressBack()
        pressBack()
        composeTestRule.mainMenuRobot {
            checkExitDialogShowing()
            clickCancelOnExitDialog()
        }


        logMessage(this::class, "About")
        R.id.action_bar__about.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (AboutFragment::class))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "New score")
        composeTestRule.mainMenuRobot {
            clickNewScore()
        }
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "New score -> Input end")
        composeTestRule.mainMenuRobot {
            clickNewScore()
        }
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "New score -> Score pad")
        composeTestRule.mainMenuRobot {
            clickNewScore()
        }
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        repeat(6) {
            R.id.button_arrow_inputs__score_2.click()
        }
        R.id.button_input_end__next_end.click()
        CustomConditionWaiter.waitFor(500)
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "Main menu 2")
        pressBack()
        composeTestRule.mainMenuRobot {
            checkExitDialogShowing()
            clickCancelOnExitDialog()
        }


        logMessage(this::class, "View rounds")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "Email score")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(longClick())
        CustomConditionWaiter.waitForMenuItemAndPerform(CommonStrings.Menus.viewRoundsEmail)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EmailScoresFragment::class))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewScoresFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Score pad")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewScoresFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Score pad -> Score stats")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewScoresFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Score pad -> Many")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class))
        repeat(6) {
            R.id.scorePadFragment.click()
            CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
            R.id.inputEndFragment.click()
            CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        }
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewScoresFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Edit end")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InsertEndFragment::class))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewScoresFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Insert end")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewScoresFragment::class))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "Main menu 3")
        pressBack()
        composeTestRule.mainMenuRobot {
            checkExitDialogShowing()
            clickCancelOnExitDialog()
        }

        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
    }

    /**
     * Navigate to every screen and check the home button returns to the main menu
     */
    @Test
    fun testHomeButton() {
        ConditionWatcher.setTimeoutLimit(2000)


        logMessage(this::class, "Main menu 1")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "About")
        R.id.action_bar__about.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (AboutFragment::class))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "New score")
        composeTestRule.mainMenuRobot {
            clickNewScore()
        }
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "New score -> Input end")
        composeTestRule.mainMenuRobot {
            clickNewScore()
        }
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "New score -> Score pad")
        composeTestRule.mainMenuRobot {
            clickNewScore()
        }
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        repeat(6) {
            R.id.button_arrow_inputs__score_2.click()
        }
        R.id.button_input_end__next_end.click()
        CustomConditionWaiter.waitFor(500)
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "Main menu 2")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "Email score")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(longClick())
        CustomConditionWaiter.waitForMenuItemAndPerform(CommonStrings.Menus.viewRoundsEmail)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EmailScoresFragment::class))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Score pad")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Score pad -> Score stats")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Score pad -> Many")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class))
        repeat(6) {
            R.id.scorePadFragment.click()
            CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
            R.id.inputEndFragment.click()
            CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
        }
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Edit end")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InsertEndFragment::class))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "View rounds -> Insert end")
        composeTestRule.mainMenuRobot {
            clickViewScores()
        }
        onView(withText("6/12/0")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))


        logMessage(this::class, "Main menu 3")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuComposeFragment::class))

        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
    }
}