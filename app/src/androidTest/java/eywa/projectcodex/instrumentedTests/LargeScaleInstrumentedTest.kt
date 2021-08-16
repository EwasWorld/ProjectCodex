package eywa.projectcodex.instrumentedTests

import android.content.pm.ActivityInfo
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.R
import eywa.projectcodex.TestData
import eywa.projectcodex.common.*
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.about.AboutFragment
import eywa.projectcodex.components.archerRoundScore.archerRoundStats.ArcherRoundStatsFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.EditEndFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.InputEndFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.InsertEndFragment
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.components.commonUtils.findInstanceOf
import eywa.projectcodex.components.mainMenu.MainMenuFragment
import eywa.projectcodex.components.newRound.NewRoundFragment
import eywa.projectcodex.components.viewRounds.ViewRoundsFragment
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlinx.coroutines.runBlocking
import org.junit.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * These tests span more than one screen and require an [ActivityScenario]
 */
class LargeScaleInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = CommonStrings.testDatabaseName
            SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
        }
    }

    @get:Rule
    var rule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private lateinit var navController: NavController

    private val closeHelpString = "Close help"
    private val nextHelpString = "Next"
    private val helpFadeTime = 300L

    @Before
    fun setup() {
        // Note clearing the database instance after launching the activity causes issues with live data
        // (as DAOs are inconsistent)
        scenario = rule.scenario
        scenario.onActivity {
            db = ScoresRoomDatabase.getDatabase(it.applicationContext)
            navController = it.navHostFragment.navController
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(rule)
        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
    }

    private fun addSimpleTestDataToDb() {
        val arrows = List(12) { i -> TestData.ARROWS[5].toArrowValue(1, i) }
        val archerRound = ArcherRound(1, TestData.generateDate(), 1, false)
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
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.jvmName))

        logMessage(this::class, "View rounds (nothing inputted)")
        R.id.button_main_menu__view_rounds.click()
        clickAlertDialogOk(CommonStrings.Dialogs.emptyTable)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.jvmName))


        logMessage(this::class, "Start score A - default date, with round")
        R.id.button_main_menu__start_new_round.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewRoundFragment::class.jvmName))
        CustomConditionWaiter.waitForUpdateRoundsTaskToFinish(scenario)
        R.id.spinner_create_round__round.clickSpinnerItem("Short Metric")
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.jvmName))


        logMessage(this::class, "Score A - open score pad - no arrows entered")
        R.id.scorePadFragment.click()
        clickAlertDialogOk(CommonStrings.Dialogs.emptyTable)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.jvmName))


        logMessage(this::class, "Score A - open stats - no arrows entered")
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class.jvmName))
        R.id.inputEndFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.jvmName))


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
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class.jvmName))


        logMessage(this::class, "Score A - open score pad - some arrows entered")
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))


        logMessage(this::class, "Score A - Score pad insert end - 1")
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InsertEndFragment::class.jvmName))
        completeEnd(
                R.id.button_arrow_inputs__score_4,
                activityScenario = scenario,
                submitButtonId = R.id.button_insert_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))


        logMessage(this::class, "Score A - Score pad edit end - 1")
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class.jvmName))
        repeat(4) { R.id.button_end_inputs__backspace.click() }
        completeEnd(
                R.id.button_arrow_inputs__score_5,
                activityScenario = scenario,
                submitButtonId = R.id.button_edit_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))


        logMessage(this::class, "Score A - Score pad delete end - 1")
        onView(withText("4-4-4-4-4-4")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadDeleteEnd)).perform(click())


        logMessage(this::class, "Score A - Score pad insert end - 2")
        onView(withText("5-5-5-5-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InsertEndFragment::class.jvmName))
        completeEnd(
                R.id.button_arrow_inputs__score_6,
                activityScenario = scenario,
                submitButtonId = R.id.button_insert_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))


        logMessage(this::class, "Score A - Score pad edit end - 2")
        onView(withText("6-6-6-6-6-6")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class.jvmName))
        R.id.button_end_inputs__clear.click()
        completeEnd(
                R.id.button_arrow_inputs__score_7,
                activityScenario = scenario,
                submitButtonId = R.id.button_edit_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))


        logMessage(this::class, "Score A - score pad help button")
        R.id.action_bar__help.click()
        onView(withText(closeHelpString)).perform(click())
        CustomConditionWaiter.waitFor(helpFadeTime)


        logMessage(this::class, "Score A - Score pad delete end - 2")
        onView(withText("7-7-7-7-7-7")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadDeleteEnd)).perform(click())


        logMessage(this::class, "Score A - return to main menu")
        var isOnMainMenu = false
        while (!isOnMainMenu) {
            pressBack()
            scenario.onActivity {
                if (findInstanceOf<MainMenuFragment>(it.navHostFragment) != null) {
                    isOnMainMenu = true
                }
            }
        }
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.jvmName))


        logMessage(this::class, "Main menu help button 1")
        R.id.action_bar__help.click()
        onView(withText(closeHelpString)).perform(click())
        CustomConditionWaiter.waitFor(helpFadeTime)


        logMessage(this::class, "Main menu help button 2")
        R.id.action_bar__help.click()
        onView(withText(closeHelpString)).perform(click())
        CustomConditionWaiter.waitFor(helpFadeTime)


        logMessage(this::class, "View rounds (score A inputted)")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.jvmName))


        logMessage(this::class, "Score A - View round insert end - 1")
        onView(withText("48")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))
        R.id.inputEndFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.jvmName))
        completeEnd(R.id.button_arrow_inputs__score_1, activityScenario = scenario)


        logMessage(this::class, "Score A - View round edit end - 1")
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))
        onView(withIndex(withText("1-1-1-1-1-1"), 0)).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class.jvmName))
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
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.jvmName))
        completeEnd(R.id.button_arrow_inputs__score_8, activityScenario = scenario)


        logMessage(this::class, "Score A - View round edit end - 2")
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))
        onView(withIndex(withText("8-8-8-8-8-8"), 0)).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class.jvmName))
        R.id.button_end_inputs__clear.click()
        completeEnd(
                R.id.button_arrow_inputs__score_9,
                activityScenario = scenario,
                submitButtonId = R.id.button_edit_end__complete
        )
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.jvmName))


        logMessage(this::class, "Score A - View round delete end - 2")
        onView(withText("9-9-9-9-9-9")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadDeleteEnd)).perform(click())


        logMessage(this::class, "Score A - Complete round")
        R.id.inputEndFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.jvmName))
        repeat(9) { completeEnd(R.id.button_arrow_inputs__score_x, activityScenario = scenario) }
        clickAlertDialogOk(CommonStrings.Dialogs.inputEndRoundComplete)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class.jvmName))


        logMessage(this::class, "Score A - Continue round (completed)")
        // TODO Try to continue the round from view rounds, score pad - insert, and input end (nav from score pad and stats)


//        logMessage(this::class, "Score A - Convert round Xs")
//        onView(withText("Score A current score")).perform(longClick())
//        onView(withText(CommonStrings.Menus.viewRoundsConvert)).perform(click())
//
//
//
//        logMessage(this::class, "Start score B - change date, no round")
//        R.id.button_main_menu__start_new_round.click()
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
//        R.id.button_main_menu__view_rounds.click()
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
        touchEveryScreen(listOf(ArcherRoundStatsFragment::class, AboutFragment::class)) {
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
    }

    /**
     * Navigate to every screen and check that landscape mode doesn't crash the app (rotate back to portrait before
     * transitioning as not all buttons will be visible in landscape)
     */
    @Ignore("Not currently working")
    fun testLandscape() {
        scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        touchEveryScreen {
            scenario.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            CustomConditionWaiter.waitFor(1000)
            scenario.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            CustomConditionWaiter.waitFor(1000)
        }
    }

    /**
     * @param action run once on every single fragment
     * @param ignoreList do not run [action] on these fragments
     */
    private fun touchEveryScreen(ignoreList: List<KClass<out Fragment>> = listOf(), action: () -> Unit) {
        fun performAction(current: KClass<out Fragment>) {
            if (ignoreList.contains(current)) {
                logMessage(this::class, "Ignored: ${current.jvmName}")
                return
            }
            logMessage(this::class, "Performing on: ${current.jvmName}")
            CustomConditionWaiter.waitForFragmentToShow(scenario, (current.jvmName))
            action()
        }

        addSimpleTestDataToDb()

        performAction(MainMenuFragment::class)

        logMessage(this::class, "Navigating to: About")
        R.id.action_bar__about.click()
        performAction(AboutFragment::class)

        logMessage(this::class, "Navigating to: New score")
        pressBack()
        R.id.button_main_menu__start_new_round.click()
        performAction(NewRoundFragment::class)

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
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class.java.name))
        performAction(EditEndFragment::class)

        logMessage(this::class, "Navigating to: Insert end")
        R.id.button_edit_end__cancel.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        performAction(InsertEndFragment::class)

        logMessage(this::class, "Navigating to: Score stats")
        R.id.button_insert_end__cancel.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        R.id.archerRoundStatsFragment.click()
        performAction(ArcherRoundStatsFragment::class)

        logMessage(this::class, "Navigating to: View rounds")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))
        R.id.button_main_menu__view_rounds.click()
        performAction(ViewRoundsFragment::class)

        logMessage(this::class, "Returning to main menu")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))
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
                        ArcherRound(1, TestData.generateDate(), 1, true, roundId = 1)
                )
                db.roundDao().insert(Round(1, "test", "test", false, false, listOf()))
                db.roundArrowCountDao().insert(RoundArrowCount(1, 1, 10.0, arrowsInRound))
                db.roundDistanceDao().insert(RoundDistance(1, 1, 1, 20))
            }
        }
        openScorePadFromMainMenu(arrowScore * arrowCount)
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
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
        ConditionWatcher.setTimeoutLimit(2000)


        logMessage(this::class, "Main menu 1")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "About")
        R.id.action_bar__about.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (AboutFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "New score")
        R.id.button_main_menu__start_new_round.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewRoundFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "New score -> Input end")
        R.id.button_main_menu__start_new_round.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewRoundFragment::class.java.name))
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "New score -> Score pad")
        R.id.button_main_menu__start_new_round.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewRoundFragment::class.java.name))
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.java.name))
        repeat(6) {
            R.id.button_arrow_inputs__score_2.click()
        }
        R.id.button_input_end__next_end.click()
        CustomConditionWaiter.waitFor(500)
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "Main menu 2")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad -> Score stats")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad -> Many")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class.java.name))
        repeat(6) {
            R.id.scorePadFragment.click()
            CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
            R.id.inputEndFragment.click()
            CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.java.name))
        }
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Edit end")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InsertEndFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Insert end")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class.java.name))
        logMessage(this::class, " -> press back")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "Main menu 3")
        pressBack()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))

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
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "About")
        R.id.action_bar__about.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (AboutFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "New score")
        R.id.button_main_menu__start_new_round.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewRoundFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "New score -> Input end")
        R.id.button_main_menu__start_new_round.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewRoundFragment::class.java.name))
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "New score -> Score pad")
        R.id.button_main_menu__start_new_round.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewRoundFragment::class.java.name))
        R.id.button_create_round__submit.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.java.name))
        repeat(6) {
            R.id.button_arrow_inputs__score_2.click()
        }
        R.id.button_input_end__next_end.click()
        CustomConditionWaiter.waitFor(500)
        R.id.scorePadFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "Main menu 2")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad -> Score stats")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad -> Many")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        R.id.archerRoundStatsFragment.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ArcherRoundStatsFragment::class.java.name))
        repeat(6) {
            R.id.scorePadFragment.click()
            CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
            R.id.inputEndFragment.click()
            CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class.java.name))
        }
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Edit end")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (InsertEndFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Insert end")
        R.id.button_main_menu__view_rounds.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class.java.name))
        onView(withText("2-2-2-2-2-2")).perform(click())
        CustomConditionWaiter.waitFor(500)
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(scenario, (EditEndFragment::class.java.name))
        logMessage(this::class, " -> press home")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))


        logMessage(this::class, "Main menu 3")
        R.id.action_bar__home.click()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class.java.name))

        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
    }
}