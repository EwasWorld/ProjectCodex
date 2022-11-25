package eywa.projectcodex.instrumentedTests

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.R
import eywa.projectcodex.common.*
import eywa.projectcodex.common.archeryObjects.End
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.components.archerRoundScore.inputEnd.EditEndFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.InsertEndFragment
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.InfoTableCell
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadData
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.LocalDatabaseDaggerModule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScorePadInstrumentedTest {
    companion object {
        init {
            SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(60)

    private val endSize = 6

    private var fragScenario: FragmentScenario<ScorePadFragment>? = null
    private var activityScenario: ActivityScenario<MainActivity>? = null
    private lateinit var navController: TestNavHostController
    private lateinit var resources: Resources
    private lateinit var db: ScoresRoomDatabase
    private lateinit var arrows: List<ArrowValue>

    private val columnHeaderOrder = listOf(
            ScorePadData.ColumnHeader.END_STRING,
            ScorePadData.ColumnHeader.HITS,
            ScorePadData.ColumnHeader.SCORE,
            ScorePadData.ColumnHeader.GOLDS,
            ScorePadData.ColumnHeader.RUNNING_TOTAL
    )

    private fun checkData(arrows: List<ArrowValue>, goldsHeader: String = "G") {
        checkData(ScorePadData(arrows, endSize, GoldsType.NINES, resources), goldsHeader)
    }

    /**
     * Check the data currently in the adapter against [expectedData]
     */
    private fun checkData(expectedData: ScorePadData, goldsHeader: String = "G") {
        /*
         * Check cells
         */
        var col = 0
        val expectedColumnHeaders =
                listOf("Arrows", "H", "S", goldsHeader, "R/T").map { InfoTableCell(it, "col" + col++) }
        for (i in expectedColumnHeaders.indices) {
            assertEquals(expectedColumnHeaders[i], getTableView().adapter!!.getColumnHeaderItem(i))
        }
        assertNull(getTableView().adapter!!.getColumnHeaderItem(expectedColumnHeaders.size))

        /*
         * Check row headers
         */
        val expectedRowHeaders = expectedData.generateRowHeaders("T", "GT")
        for (i in expectedRowHeaders.indices) {
            assertEquals(expectedRowHeaders[i], getTableView().adapter!!.getRowHeaderItem(i))
        }
        assertNull(getTableView().adapter!!.getRowHeaderItem(expectedRowHeaders.size))

        /*
         * Check column headers
         */
        val expectedCells = expectedData.getAsTableCells(columnHeaderOrder)
        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], getTableView().adapter!!.getCellRowItems(i))
        }
    }

    private fun setupDb(hasRound: Boolean = false) {
        db = LocalDatabaseDaggerModule.scoresRoomDatabase

        for (arrow in arrows) {
            // Sometimes the test has kittens so it's nice to have a log
            logMessage(
                    this::class,
                    "ArrowValue(${arrow.archerRoundId},${arrow.arrowNumber},${arrow.score},${arrow.isX}),"
            )
            runBlocking {
                db.arrowValueDao().insert(arrow)
            }
        }
        val roundId = if (hasRound) 1 else null
        runBlocking {
            db.archerRoundDao().insert(
                    ArcherRound(
                            1, TestUtils.generateDate(), 1, true, roundId = roundId, roundSubTypeId = roundId
                    )
            )
        }
    }

    private fun setupFragment(arrowsForDatabase: List<ArrowValue>? = null, hasRound: Boolean = false) {
        check(activityScenario == null) { "Activity scenario already in use for this test" }

        arrows = arrowsForDatabase ?: TestUtils.generateArrowValues(1, 36)

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        val data = Pair("archerRoundId", 1)
        val bundle = Bundle()
        bundle.putInt(data.first, data.second)

        // Start initialised so we can add to the database before the onCreate methods are called
        fragScenario = launchFragmentInContainer(bundle, initialState = Lifecycle.State.INITIALIZED)
        fragScenario!!.onFragment {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.scorePadFragment, bundle)
            setupDb(hasRound)
        }

        fragScenario!!.moveToState(Lifecycle.State.RESUMED)
        fragScenario!!.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)

            resources = it.requireActivity().resources
        }
    }

    private fun setupActivity(arrowsForDatabase: List<ArrowValue>? = null, waitForRow: Int) {
        check(fragScenario == null) { "Fragment scenario already in use for this test" }

        arrows = arrowsForDatabase ?: TestUtils.generateArrowValues(1, 36)
        activityScenario = composeTestRule.activityRule.scenario
        activityScenario!!.onActivity {
            setupDb()
            resources = it.resources
        }
        activityScenario!!.recreate()

        CustomConditionWaiter.waitForScorePadToOpen(composeTestRule = composeTestRule, arrows = arrows, rowIndex = 0)
        CustomConditionWaiter.waitForRowToAppear(getTableView(), (waitForRow))
    }

    @After
    fun afterEach() {
        fragScenario?.let { scenario ->
            CommonSetupTeardownFns.teardownScenario(scenario)
            fragScenario = null
        }
        activityScenario?.let { scenario ->
            CommonSetupTeardownFns.teardownScenario(scenario)
            activityScenario = null
        }
    }

    @Test
    fun testTableValues() {
        setupFragment()
        CustomConditionWaiter.waitForRowToAppear(getTableView(), (0))
        checkData(arrows)
    }

    @Test
    fun testTableValuesWithTotals() {
        setupFragment(hasRound = true)
        val arrowCounts = listOf(
                RoundArrowCount(1, 1, 1.0, 18),
                RoundArrowCount(1, 2, 1.0, 18)
        )
        val roundDistances = listOf(
                RoundDistance(1, 1, 1, 60),
                RoundDistance(1, 2, 1, 50)
        )
        fragScenario!!.onFragment {
            runBlocking {
                db.roundDao().insert(Round(1, "RoundName", "Round Name", false, true, listOf()))
                db.roundArrowCountDao().insert(arrowCounts[0])
                db.roundArrowCountDao().insert(arrowCounts[1])
                db.roundDistanceDao().insert(roundDistances[0])
                db.roundDistanceDao().insert(roundDistances[1])
            }
        }
        CustomConditionWaiter.waitForRowToAppear(getTableView(), (8))

        checkData(ScorePadData(arrows, endSize, GoldsType.TENS, resources, arrowCounts, roundDistances, "m"), "10")
    }

    @Test
    fun testEmptyTable() {
        setupFragment(arrowsForDatabase = listOf())
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        assertNotEquals(R.id.scorePadFragment, navController.currentDestination?.id)
    }

    /**
     * Helper function as the adapter changes whenever the table is updated so cannot store a reference to it
     */
    private fun getTableView(): TableView {
        var tableView: TableView? = null

        fun getTableView(activity: Activity) {
            tableView = activity.findViewById(R.id.table_view_score_pad)
        }

        fragScenario?.onFragment { getTableView(it.requireActivity()) }
        activityScenario?.onActivity { getTableView(it) }
        return tableView!!
    }

    @Test
    fun testEditEnd() {
        val firstArrows = listOf(
                TestUtils.ARROWS[11], TestUtils.ARROWS[9], TestUtils.ARROWS[9],
                TestUtils.ARROWS[9], TestUtils.ARROWS[7], TestUtils.ARROWS[6]
        )
        val nextArrows = List(endSize) { TestUtils.ARROWS[1] }
        setupActivity(
                listOf(firstArrows, nextArrows).flatten()
                        .mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) },
                waitForRow = 2
        )

        onView(withText("X-9-9-9-7-6")).perform(click())
        CustomConditionWaiter.waitForMenuItemAndPerform(CommonStrings.Menus.scorePadEditEnd)
        CustomConditionWaiter.waitForFragmentToShow(activityScenario!!, (EditEndFragment::class))
        onView(withId(R.id.button_end_inputs__clear)).perform(click())
        val scoreButton = onView(withId(R.id.button_arrow_inputs__score_2))
        for (i in 0 until endSize) {
            scoreButton.perform(click())
        }
        onView(withId(R.id.button_edit_end__complete)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(activityScenario!!, (ScorePadFragment::class))
        CustomConditionWaiter.waitForRowToAppear(getTableView(), (2))

        val newArrows = listOf(List(endSize) { TestUtils.ARROWS[2] }, nextArrows).flatten()
                .mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) }

        checkData(newArrows)
    }

    @Test
    fun testEditEndCancel() {
        setupEditEnd()
        onView(withId(R.id.button_edit_end__cancel)).perform(click())
        checkEditEndCancelledCorrectly()
    }

    @Test
    fun testEditEndBackButtonPress() {
        setupEditEnd()
        pressBack()
        checkEditEndCancelledCorrectly()
    }

    private fun setupEditEnd() {
        setupActivity(waitForRow = 0)

        /*
         * Edit an end
         */
        val firstEnd = End(arrows.subList(0, 6), TestUtils.ARROW_PLACEHOLDER, TestUtils.ARROW_DELIMINATOR)
        firstEnd.reorderScores()
        onView(withIndex(withText(firstEnd.toString()), 0)).perform(click())
        CustomConditionWaiter.waitForMenuItemAndPerform(CommonStrings.Menus.scorePadEditEnd)
        CustomConditionWaiter.waitForFragmentToShow(activityScenario!!, (EditEndFragment::class))
    }

    private fun checkEditEndCancelledCorrectly() {
        CustomConditionWaiter.waitForFragmentToShow(activityScenario!!, (ScorePadFragment::class))
        CustomConditionWaiter.waitForRowToAppear(getTableView(), (0))

        checkData(arrows)
    }

    @Test
    fun testDeleteEnd() {
        var genArrowNumber = 1
        val expectedArrowsGrouped = List(6) { index ->
            List(endSize) { TestUtils.ARROWS[index].toArrowValue(1, genArrowNumber++) }
        }
        setupActivity(expectedArrowsGrouped.flatten(), waitForRow = 0)

        val deleteEndIndex = 1
        val endToClick =
                End(expectedArrowsGrouped[deleteEndIndex], TestUtils.ARROW_PLACEHOLDER, TestUtils.ARROW_DELIMINATOR)
        endToClick.reorderScores()
        onView(withText(endToClick.toString())).perform(click())
        CustomConditionWaiter.waitForMenuItemAndPerform(CommonStrings.Menus.scorePadDeleteEnd)

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "wait for row to be removed"
            }

            override fun checkCondition(): Boolean {
                // arrows.size (-1 for deleted row) (+1 for grand total)
                return getTableView().adapter!!.getCellColumnItems(2).size == arrows.size / endSize
            }
        })

        checkData(expectedArrowsGrouped.filterIndexed { index, _ ->
            index != deleteEndIndex
        }.flatten())
    }

    @Test
    fun testInsertEnd() {
        val firstArrows = listOf(
                TestUtils.ARROWS[11], TestUtils.ARROWS[9], TestUtils.ARROWS[9],
                TestUtils.ARROWS[9], TestUtils.ARROWS[7], TestUtils.ARROWS[6]
        )
        setupActivity(
                listOf(
                        List(endSize) { TestUtils.ARROWS[1] },
                        firstArrows,
                        List(endSize * 2) { TestUtils.ARROWS[1] }
                ).flatten().mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) },
                waitForRow = 4
        )

        onView(withText("X-9-9-9-7-6")).perform(click())
        CustomConditionWaiter.waitForMenuItemAndPerform(CommonStrings.Menus.scorePadInsertEnd)
        CustomConditionWaiter.waitForFragmentToShow(activityScenario!!, (InsertEndFragment::class))

        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.-.")
        val scoreButton = onView(withId(R.id.button_arrow_inputs__score_2))
        for (i in 0 until 6) {
            scoreButton.perform(click())
        }
        onView(withId(R.id.button_insert_end__complete)).perform(click())
        CustomConditionWaiter.waitForFragmentToShow(activityScenario!!, (ScorePadFragment::class))
        CustomConditionWaiter.waitForRowToAppear(getTableView(), (5))

        val newArrows = listOf(
                List(endSize) { TestUtils.ARROWS[1] },
                List(endSize) { TestUtils.ARROWS[2] }, /* New end */
                firstArrows, /* Clicked end */
                List(endSize * 2) { TestUtils.ARROWS[1] }
        ).flatten().mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) }

        checkData(newArrows)
    }
}