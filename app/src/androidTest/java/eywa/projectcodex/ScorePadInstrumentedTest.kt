package eywa.projectcodex

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.archerRoundScore.inputEnd.EditEndFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.InsertEndFragment
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.archeryObjects.End
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.infoTable.InfoTableCell
import eywa.projectcodex.components.infoTable.calculateScorePadTableData
import eywa.projectcodex.components.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScorePadInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = CommonStrings.testDatabaseName
        }
    }

    private val endSize = 6
    private val waitForMenuMs = 500L

    private var fragScenario: FragmentScenario<ScorePadFragment>? = null
    private var activityScenario: ActivityScenario<MainActivity>? = null
    private lateinit var navController: TestNavHostController
    private lateinit var resources: Resources
    private lateinit var db: ScoresRoomDatabase
    private lateinit var arrows: List<ArrowValue>

    private fun clickMenuButton(buttonText: String): Instruction {
        return object : Instruction() {
            override fun getDescription(): String {
                return "Click the desired menu item or wait if it hasn't appeared yet"
            }

            override fun checkCondition(): Boolean {
                return try {
                    onView(withText(buttonText)).perform(click())
                    true
                }
                catch (e: NoMatchingViewException) {
                    println("Sleep")
                    Thread.sleep(200)
                    false
                }
            }
        }
    }

    private fun checkColumnHeaders(goldsHeader: String = "10") {
        var col = 0
        val expectedColumnHeaders =
                listOf("Arrows", "H", "S", goldsHeader, "R/T").map { InfoTableCell(it, "col" + col++) }
        for (i in expectedColumnHeaders.indices) {
            assertEquals(expectedColumnHeaders[i], getTableView().adapter!!.getColumnHeaderItem(i))
        }
        assertNull(getTableView().adapter!!.getColumnHeaderItem(expectedColumnHeaders.size))
    }

    private fun checkRowsHeaders(rowsPerDistance: Int) {
        checkRowsHeaders(listOf(rowsPerDistance))
    }

    private fun checkRowsHeaders(rowsPerDistance: List<Int>) {
        val expectedRowHeaders = generateNumberedRowHeaders(rowsPerDistance, null, resources, true)
        for (i in expectedRowHeaders.indices) {
            assertEquals(expectedRowHeaders[i], getTableView().adapter!!.getRowHeaderItem(i))
        }
        assertNull(getTableView().adapter!!.getRowHeaderItem(expectedRowHeaders.size))
    }

    private fun checkCells(arrows: List<ArrowValue>) {
        val expectedCells = calculateScorePadTableData(arrows, endSize, GoldsType.TENS, resources)
        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], getTableView().adapter!!.getCellRowItems(i))
        }
    }

    private fun setupDb(context: Context, hasRound: Boolean = false) {
        ScoresRoomDatabase.clearInstance(context)
        db = ScoresRoomDatabase.getDatabase(context)

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
                            1, TestData.generateDate(), 1, true, roundId = roundId, roundSubTypeId = roundId
                    )
            )
        }
    }

    private fun setupFragment(arrowsForDatabase: List<ArrowValue>? = null, hasRound: Boolean = false) {
        check(activityScenario == null) { "Activity scenario already in use for this test" }

        arrows = arrowsForDatabase ?: TestData.generateArrowValues(36, 1)

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val args = Bundle()
        args.putInt("archerRoundId", 1)
        // Start initialised so we can add to the database before the onCreate methods are called
        fragScenario = launchFragmentInContainer(args, initialState = Lifecycle.State.INITIALIZED)
        fragScenario!!.onFragment {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.scorePadFragment, args)
            setupDb(it.requireContext(), hasRound)
        }

        fragScenario!!.moveToState(Lifecycle.State.RESUMED)
        fragScenario!!.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)

            resources = it.requireActivity().resources
        }
    }

    private fun setupActivity(arrowsForDatabase: List<ArrowValue>? = null, waitForRow: Int) {
        check(fragScenario == null) { "Fragment scenario already in use for this test" }

        arrows = arrowsForDatabase ?: TestData.generateArrowValues(36, 1)
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        activityScenario!!.onActivity {
            setupDb(it.applicationContext)
            resources = it.resources
        }

        ConditionWatcher.waitForCondition(waitForOpenScorePadFromMainMenu(arrows.sumOf { it.score }))
        ConditionWatcher.waitForCondition(activityScenario!!.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        ConditionWatcher.waitForCondition(getTableView().waitForRowToAppear(waitForRow))
    }

    @After
    fun afterEach() {
        fun teardown(context: Context) {
            ScoresRoomDatabase.clearInstance(context)
        }
        fragScenario?.let { scenario ->
            scenario.onFragment { teardown(it.requireContext()) }
            fragScenario = null
        }
        activityScenario?.let { scenario ->
            scenario.onActivity { teardown(it.applicationContext) }
            activityScenario = null
        }
    }

    @Test
    fun testTableValues() {
        setupFragment()
        ConditionWatcher.waitForCondition(getTableView().waitForRowToAppear(0))
        checkCells(arrows)
        checkColumnHeaders()
        checkRowsHeaders(6)
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
                db.roundDao().insert(Round(1, "RoundName", "Round Name", true, true, listOf()))
                db.roundArrowCountDao().insert(arrowCounts[0])
                db.roundArrowCountDao().insert(arrowCounts[1])
                db.roundDistanceDao().insert(roundDistances[0])
                db.roundDistanceDao().insert(roundDistances[1])
            }
        }
        ConditionWatcher.waitForCondition(getTableView().waitForRowToAppear(8))

        val expectedCells = calculateScorePadTableData(
                arrows, endSize, GoldsType.XS, resources, arrowCounts, roundDistances, "m"
        )
        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], getTableView().adapter!!.getCellRowItems(i))
        }

        checkColumnHeaders("X")
        checkRowsHeaders(listOf(3, 3))
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
                TestData.ARROWS[11], TestData.ARROWS[9], TestData.ARROWS[9],
                TestData.ARROWS[9], TestData.ARROWS[7], TestData.ARROWS[6]
        )
        val nextArrows = List(endSize) { TestData.ARROWS[1] }
        setupActivity(
                listOf(firstArrows, nextArrows).flatten()
                        .mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) },
                waitForRow = 2
        )

        onView(withText("X-9-9-9-7-6")).perform(click())
        ConditionWatcher.waitForCondition(waitFor(waitForMenuMs))
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        ConditionWatcher.waitForCondition(activityScenario!!.waitForFragmentInstruction(EditEndFragment::class.java.name))
        onView(withId(R.id.button_end_inputs__clear)).perform(click())
        val scoreButton = onView(withId(R.id.button_arrow_inputs__score_2))
        for (i in 0 until endSize) {
            scoreButton.perform(click())
        }
        onView(withId(R.id.button_edit_end__complete)).perform(click())
        ConditionWatcher.waitForCondition(activityScenario!!.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        ConditionWatcher.waitForCondition(getTableView().waitForRowToAppear(2))

        val newArrows = listOf(List(endSize) { TestData.ARROWS[2] }, nextArrows).flatten()
                .mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) }

        checkCells(newArrows)
        checkColumnHeaders()
        checkRowsHeaders(2)
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
        val firstEnd = End(arrows.subList(0, 6), TestData.ARROW_PLACEHOLDER, TestData.ARROW_DELIMINATOR)
        firstEnd.reorderScores()
        onView(withText(firstEnd.toString())).perform(click())
        ConditionWatcher.waitForCondition(waitFor(waitForMenuMs))
        onView(withText(CommonStrings.Menus.scorePadEditEnd)).perform(click())
        ConditionWatcher.waitForCondition(activityScenario!!.waitForFragmentInstruction(EditEndFragment::class.java.name))
    }

    private fun checkEditEndCancelledCorrectly() {
        ConditionWatcher.waitForCondition(activityScenario!!.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        ConditionWatcher.waitForCondition(getTableView().waitForRowToAppear(0))

        checkCells(arrows)
        checkColumnHeaders()
        checkRowsHeaders(6)
    }

    @Test
    fun testDeleteEnd() {
        var genArrowNumber = 1
        val expectedArrowsGrouped = List(6) { index ->
            List(endSize) { TestData.ARROWS[index].toArrowValue(1, genArrowNumber++) }
        }
        setupActivity(expectedArrowsGrouped.flatten(), waitForRow = 0)

        val deleteEndIndex = 1
        val endToClick =
                End(expectedArrowsGrouped[deleteEndIndex], TestData.ARROW_PLACEHOLDER, TestData.ARROW_DELIMINATOR)
        endToClick.reorderScores()
        onViewWithClassName(endToClick.toString()).perform(click())
        ConditionWatcher.waitForCondition(waitFor(waitForMenuMs))
        onView(withText(CommonStrings.Menus.scorePadDeleteEnd)).perform(click())

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "wait for row to be removed"
            }

            override fun checkCondition(): Boolean {
                // arrows.size (-1 for deleted row) (+1 for grand total)
                return getTableView().adapter!!.getCellColumnItems(2).size == arrows.size / endSize
            }
        })

        checkCells(expectedArrowsGrouped.filterIndexed { index, _ ->
            index != deleteEndIndex
        }.flatten())
        checkColumnHeaders()
        checkRowsHeaders(5)
    }

    @Test
    fun testInsertEnd() {
        val firstArrows = listOf(
                TestData.ARROWS[11], TestData.ARROWS[9], TestData.ARROWS[9],
                TestData.ARROWS[9], TestData.ARROWS[7], TestData.ARROWS[6]
        )
        setupActivity(
                listOf(
                        List(endSize) { TestData.ARROWS[1] },
                        firstArrows,
                        List(endSize * 2) { TestData.ARROWS[1] }
                ).flatten().mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) },
                waitForRow = 4
        )

        onView(withText("X-9-9-9-7-6")).perform(click())
        ConditionWatcher.waitForCondition(clickMenuButton(CommonStrings.Menus.scorePadInsertEnd))
        ConditionWatcher.waitForCondition(activityScenario!!.waitForFragmentInstruction(InsertEndFragment::class.java.name))

        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.-.")
        val scoreButton = onView(withId(R.id.button_arrow_inputs__score_2))
        for (i in 0 until 6) {
            scoreButton.perform(click())
        }
        onView(withId(R.id.button_insert_end__complete)).perform(click())
        ConditionWatcher.waitForCondition(activityScenario!!.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        ConditionWatcher.waitForCondition(getTableView().waitForRowToAppear(5))

        val newArrows = listOf(
                List(endSize) { TestData.ARROWS[1] },
                List(endSize) { TestData.ARROWS[2] }, /* New end */
                firstArrows, /* Clicked end */
                List(endSize * 2) { TestData.ARROWS[1] }
        ).flatten().mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) }

        checkCells(newArrows)
        checkColumnHeaders()
        checkRowsHeaders(5)
    }
}