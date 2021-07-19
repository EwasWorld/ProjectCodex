package eywa.projectcodex

import android.content.pm.ActivityInfo
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.about.AboutFragment
import eywa.projectcodex.components.archerRoundScore.archerRoundStats.ArcherRoundStatsFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.EditEndFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.InputEndFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.InsertEndFragment
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.commonUtils.SharedPrefs
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

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private lateinit var navController: NavController

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            ScoresRoomDatabase.clearInstance(it.applicationContext)
            db = ScoresRoomDatabase.getDatabase(it.applicationContext)
            navController = it.navHostFragment.navController
        }
    }

    @After
    fun afterEach() {
        scenario.onActivity {
            ScoresRoomDatabase.clearInstance(it.applicationContext)
        }
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
    @Ignore("Not implemented")
    fun mainTest() {
        logMessage(this::class, "View rounds (nothing inputted)")

        logMessage(this::class, "Start score A - default date, with round")
        // TODO Check initially is importing rounds data - check info screen?
        // TODO Check date is within 1min of current date

        logMessage(this::class, "Score A - open score pad - no arrows entered")
        logMessage(this::class, "Score A - open stats - no arrows entered")

        logMessage(this::class, "Score A - enter 3 ends")
        // TODO click help

        logMessage(this::class, "Score A - open stats - some arrows entered")
        logMessage(this::class, "Score A - open score pad - some arrows entered")

        logMessage(this::class, "Score A - Score pad insert end - 1")
        logMessage(this::class, "Score A - Score pad edit end - 1")
        logMessage(this::class, "Score A - Score pad delete end - 1")
        logMessage(this::class, "Score A - Score pad insert end - 2")
        logMessage(this::class, "Score A - Score pad edit end - 2")
        // TODO click help
        logMessage(this::class, "Score A - Score pad delete end - 2")

        logMessage(this::class, "Score A - return to main menu")
        // TODO click help
        // TODO click help

        logMessage(this::class, "View rounds (score A inputted)")

        logMessage(this::class, "Score A - View round insert end - 1")
        logMessage(this::class, "Score A - View round edit end - 1")
        logMessage(this::class, "Score A - View round delete end - 1")
        logMessage(this::class, "Score A - View round insert end - 2")
        logMessage(this::class, "Score A - View round edit end - 2")
        logMessage(this::class, "Score A - View round delete end - 2")

        logMessage(this::class, "Score A - Continue round")

        logMessage(this::class, "Score A - Continue round insert end - 1")
        logMessage(this::class, "Score A - Continue round edit end - 1")
        logMessage(this::class, "Score A - Continue round delete end - 1")
        // TODO Complete round
        // TODO Check kicked to main menu

        logMessage(this::class, "Score A - Continue round (completed)")
        // TODO Try to continue the round from view rounds, score pad - insert, and input end (nav from score pad and stats)

        logMessage(this::class, "Start score B - change date, no round")

        logMessage(this::class, "View rounds (scores A and B inputted)")

        logMessage(this::class, "Score B - Continue round")
    }

    /**
     * Do repeated edit operations on a round to try and cause a problem
     */
    @Ignore("Not implemented")
    fun testEditScoreStressTest() {
        // TODO Implement
        logMessage(this::class, "Insert end")
        logMessage(this::class, "Edit end")
        logMessage(this::class, "Delete end")

        logMessage(this::class, "Insert end")
        logMessage(this::class, "Edit end")
        logMessage(this::class, "Delete end")

        logMessage(this::class, "Edit end")
        logMessage(this::class, "Edit end")
        logMessage(this::class, "Edit end")
        logMessage(this::class, "Insert end")
        logMessage(this::class, "Insert end")
        logMessage(this::class, "Insert end")
        logMessage(this::class, "Delete end")
        logMessage(this::class, "Delete end")
        logMessage(this::class, "Delete end")

        logMessage(this::class, "Insert end")
        logMessage(this::class, "Edit end")
        logMessage(this::class, "Delete end")
    }

    /**
     * Navigate to every screen and check the help button works
     */
    @Test
    fun testHelpDialogs() {
        touchEveryScreen(listOf(ArcherRoundStatsFragment::class, AboutFragment::class)) {
            R.id.action_bar__help.click()
            onViewWithClassName(MaterialShowcaseView::class.java).check(matches(isDisplayed()))
            onView(withText("Close help")).perform(click())
            ConditionWatcher.waitForCondition(waitFor(1000))
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
            ConditionWatcher.waitForCondition(waitFor(1000))
            scenario.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            ConditionWatcher.waitForCondition(waitFor(1000))
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
            ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(current.jvmName))
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
        ConditionWatcher.waitForCondition(waitFor(500))
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(EditEndFragment::class.java.name))
        performAction(EditEndFragment::class)

        logMessage(this::class, "Navigating to: Insert end")
        R.id.button_edit_end__cancel.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        onView(withText("2-2-2-2-2-2")).perform(click())
        ConditionWatcher.waitForCondition(waitFor(500))
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        performAction(InsertEndFragment::class)

        logMessage(this::class, "Navigating to: Score stats")
        R.id.button_insert_end__cancel.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        R.id.archerRoundStatsFragment.click()
        performAction(ArcherRoundStatsFragment::class)

        logMessage(this::class, "Navigating to: View rounds")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(InputEndFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))
        R.id.button_main_menu__view_rounds.click()
        performAction(ViewRoundsFragment::class)

        logMessage(this::class, "Returning to main menu")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))
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
        ConditionWatcher.waitForCondition(waitForOpenScorePadFromMainMenu(arrowScore * arrowCount))
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        var tableView: TableView? = null
        scenario.onActivity {
            tableView = it.findViewById(R.id.table_view_score_pad)
        }
        ConditionWatcher.waitForCondition(tableView!!.waitForRowToAppear(0))

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
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "About")
        R.id.action_bar__about.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(AboutFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "New score")
        R.id.button_main_menu__start_new_round.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(NewRoundFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "New score -> Input end")
        R.id.button_main_menu__start_new_round.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(NewRoundFragment::class.java.name))
        R.id.button_create_round__submit.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(InputEndFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "New score -> Score pad")
        R.id.button_main_menu__start_new_round.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(NewRoundFragment::class.java.name))
        R.id.button_create_round__submit.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(InputEndFragment::class.java.name))
        for (i in 0 until 6) {
            R.id.button_arrow_inputs__score_2.click()
        }
        R.id.button_input_end__next_end.click()
        ConditionWatcher.waitForCondition(waitFor(500))
        R.id.scorePadFragment.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(InputEndFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "Main menu 2")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds")
        R.id.button_main_menu__view_rounds.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad")
        R.id.button_main_menu__view_rounds.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad -> Score stats")
        R.id.button_main_menu__view_rounds.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        R.id.archerRoundStatsFragment.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ArcherRoundStatsFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Score pad -> Many")
        R.id.button_main_menu__view_rounds.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        R.id.archerRoundStatsFragment.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ArcherRoundStatsFragment::class.java.name))
        for (i in 0 until 6) {
            R.id.scorePadFragment.click()
            ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
            R.id.inputEndFragment.click()
            ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(InputEndFragment::class.java.name))
        }
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ArcherRoundStatsFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Edit end")
        R.id.button_main_menu__view_rounds.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        onView(withText("2-2-2-2-2-2")).perform(click())
        ConditionWatcher.waitForCondition(waitFor(500))
        onView(withText(CommonStrings.Menus.scorePadInsertEnd)).perform(click())
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(InsertEndFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "View rounds -> Insert end")
        R.id.button_main_menu__view_rounds.click()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        onView(withText("12")).perform(click())
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        onView(withText("2-2-2-2-2-2")).perform(click())
        ConditionWatcher.waitForCondition(waitFor(500))
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(EditEndFragment::class.java.name))
        logMessage(this::class, " - back")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ViewRoundsFragment::class.java.name))
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))


        logMessage(this::class, "Main menu 3")
        pressBack()
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(MainMenuFragment::class.java.name))

        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
    }
}