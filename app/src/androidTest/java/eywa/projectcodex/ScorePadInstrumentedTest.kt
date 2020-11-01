package eywa.projectcodex

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.*
import eywa.projectcodex.infoTable.InfoTableCell
import eywa.projectcodex.infoTable.calculateScorePadTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.logic.End
import eywa.projectcodex.logic.GoldsType
import eywa.projectcodex.ui.MainActivity
import eywa.projectcodex.ui.ScorePadFragment
import eywa.projectcodex.ui.inputEnd.EditEndFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScorePadInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }
    }

    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)

    private lateinit var db: ScoresRoomDatabase
    private lateinit var arrows: List<ArrowValue>

    private val openScorePadInstruction = object : Instruction() {
        override fun getDescription(): String {
            return "Wait for data to appear in view rounds table"
        }

        override fun checkCondition(): Boolean {
            return try {
                R.id.button_main_menu__view_rounds.click()
                onView(withText(arrows.sumBy { it.score }.toString())).perform(click())
                true
            }
            catch (e: NoMatchingViewException) {
                pressBack()
                false
            }
        }
    }

    @Before
    fun beforeEach() {
        activity.activity.supportFragmentManager.beginTransaction()
        db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext, GlobalScope)
    }

    private fun generateArrowsAndAddToDb() {
        arrows = TestData.generateArrowValues(36, 1)
        addArrowsToDatabase()
    }

    private fun addArrowsToDatabase() {
        for (arrow in arrows) {
            // Sometimes the test has kittens so it's nice to have a log
            println("ArrowValue(${arrow.archerRoundId},${arrow.arrowNumber},${arrow.score},${arrow.isX}),")
            runBlocking {
                db.arrowValueDao().insert(arrow)
            }
        }
        runBlocking {
            db.archerRoundDao()
                    .insert(ArcherRound(1, TestData.generateDate(), 1, true, roundId = 1, roundSubTypeId = 1))
        }
    }

    @After
    fun afterEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
    }

    @Test
    @Throws(Exception::class)
    fun testTableValues() {
        generateArrowsAndAddToDb()
        ConditionWatcher.waitForCondition(openScorePadInstruction)
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        while (getTableAdapter().getCellRowItems(0) == null) {
            println("Waiting for score pad entries to load")
        }

        val expectedCells = calculateScorePadTableData(arrows, 6, GoldsType.TENS, activity.activity.resources)
        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], getTableAdapter().getCellRowItems(i))
        }

        var col = 0
        val expectedColumnHeaders = listOf("Arrows", "H", "S", "10", "R/T").map { InfoTableCell(it, "col" + col++) }
        for (i in expectedColumnHeaders.indices) {
            assertEquals(expectedColumnHeaders[i], getTableAdapter().getColumnHeaderItem(i))
        }

        val expectedRowHeaders = generateNumberedRowHeaders(6, null, activity.activity.resources, true)
        for (i in expectedRowHeaders.indices) {
            assertEquals(expectedRowHeaders[i], getTableAdapter().getRowHeaderItem(i))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testTableValuesWithTotals() {
        generateArrowsAndAddToDb()
        val arrowCounts = listOf(
                RoundArrowCount(1, 1, 1.0, 18),
                RoundArrowCount(1, 2, 1.0, 18)
        )
        val roundDistances = listOf(
                RoundDistance(1, 1, 1, 60),
                RoundDistance(1, 2, 1, 50)
        )
        runBlocking {
            db.roundDao().insert(Round(1, "RoundName", "Round Name", true, true, listOf()))
            db.roundArrowCountDao().insert(arrowCounts[0])
            db.roundArrowCountDao().insert(arrowCounts[1])
            db.roundDistanceDao().insert(roundDistances[0])
            db.roundDistanceDao().insert(roundDistances[1])
        }

        ConditionWatcher.waitForCondition(openScorePadInstruction)
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        while (getTableAdapter().getCellRowItems(8) == null) {
            println("Waiting for score pad entries to load")
        }

        val expectedCells = calculateScorePadTableData(
                arrows, 6, GoldsType.XS, activity.activity.resources, arrowCounts, roundDistances, "m"
        )
        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], getTableAdapter().getCellRowItems(i))
        }

        var col = 0
        val expectedColumnHeaders = listOf("Arrows", "H", "S", "X", "R/T").map { InfoTableCell(it, "col" + col++) }
        for (i in expectedColumnHeaders.indices) {
            assertEquals(expectedColumnHeaders[i], getTableAdapter().getColumnHeaderItem(i))
        }

        val expectedRowHeaders = generateNumberedRowHeaders(listOf(3, 3), null, activity.activity.resources, true)
        for (i in expectedRowHeaders.indices) {
            assertEquals(expectedRowHeaders[i], getTableAdapter().getRowHeaderItem(i))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyTable() {
        R.id.button_main_menu__start_new_round.click()
        R.id.button_create_round__submit.click()
        R.id.button_input_end__score_pad.click()
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        onView(withText("Input End")).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun testEditEnd() {
        val firstArrows = listOf(
                TestData.ARROWS[11], TestData.ARROWS[9], TestData.ARROWS[9],
                TestData.ARROWS[9], TestData.ARROWS[7], TestData.ARROWS[6]
        )
        val nextArrows = List(6) { TestData.ARROWS[1] }
        arrows = listOf(firstArrows, nextArrows).flatten()
                .mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) }
        addArrowsToDatabase()
        ConditionWatcher.waitForCondition(openScorePadInstruction)
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        while (getTableAdapter().getCellRowItems(2) == null) {
            println("Waiting for score pad entries to load")
        }

        onView(withText("X-9-9-9-7-6")).perform(click())
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(EditEndFragment::class.java.name))
        onView(withId(R.id.button_end_inputs__clear)).perform(click())
        val scoreButton = onView(withId(R.id.button_arrow_inputs__score_2))
        for (i in 0 until 6) {
            scoreButton.perform(click())
        }
        onView(withId(R.id.button_edit_end__complete)).perform(click())
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        ConditionWatcher.waitForCondition(waitFor(200))

        while (getTableAdapter().getCellRowItems(2) == null) {
            println("Waiting for score pad entries to load")
        }

        val newArrows = listOf(List(6) { TestData.ARROWS[2] }, nextArrows).flatten()
                .mapIndexed { index, arrow -> ArrowValue(1, index + 1, arrow.score, arrow.isX) }
        val expectedCells = calculateScorePadTableData(newArrows, 6, GoldsType.TENS, activity.activity.resources)
        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], getTableAdapter().getCellRowItems(i))
        }

        var col = 0
        val expectedColumnHeaders = listOf("Arrows", "H", "S", "10", "R/T").map { InfoTableCell(it, "col" + col++) }
        for (i in expectedColumnHeaders.indices) {
            assertEquals(expectedColumnHeaders[i], getTableAdapter().getColumnHeaderItem(i))
        }

        val expectedRowHeaders = generateNumberedRowHeaders(2, null, activity.activity.resources, true)
        for (i in expectedRowHeaders.indices) {
            assertEquals(expectedRowHeaders[i], getTableAdapter().getRowHeaderItem(i))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testEditEndCancel() {
        generateArrowsAndAddToDb()
        ConditionWatcher.waitForCondition(openScorePadInstruction)
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        while (getTableAdapter().getCellRowItems(0) == null) {
            println("Waiting for score pad entries to load")
        }

        /*
         * Edit an end
         */
        val firstEnd = End(arrows.subList(0, 6), 6, ".", "-")
        firstEnd.reorderScores()
        onView(withText(firstEnd.toString())).perform(click())
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(EditEndFragment::class.java.name))

        /*
         * Cancel edit
         */
        onView(withId(R.id.button_edit_end__cancel)).perform(click())
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        ConditionWatcher.waitForCondition(waitFor(200))
        while (getTableAdapter().getCellRowItems(0) == null) {
            println("Waiting for score pad entries to load")
        }

        val expectedCells = calculateScorePadTableData(arrows, 6, GoldsType.TENS, activity.activity.resources)
        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], getTableAdapter().getCellRowItems(i))
        }

        var col = 0
        val expectedColumnHeaders = listOf("Arrows", "H", "S", "10", "R/T").map { InfoTableCell(it, "col" + col++) }
        for (i in expectedColumnHeaders.indices) {
            assertEquals(expectedColumnHeaders[i], getTableAdapter().getColumnHeaderItem(i))
        }

        val expectedRowHeaders = generateNumberedRowHeaders(6, null, activity.activity.resources, true)
        for (i in expectedRowHeaders.indices) {
            assertEquals(expectedRowHeaders[i], getTableAdapter().getRowHeaderItem(i))
        }
    }

    // TODO Edit end back button test

    /**
     * Helper function as the adapter changes whenever the table is updated so cannot store a reference to it
     */
    private fun getTableAdapter(): AbstractTableAdapter<*, *, *> {
        return activity.activity.findViewById<TableView>(R.id.table_view_score_pad)?.adapter!!
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteEnd() {
        generateArrowsAndAddToDb()
        ConditionWatcher.waitForCondition(openScorePadInstruction)
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        while (getTableAdapter().getCellRowItems(0) == null) {
            println("Waiting for score pad entries to load")
        }

        onView(withId((R.id.table_view_score_pad))).perform(ViewActions.swipeLeft())
        // Delete second row (index 2 because header has the same text)
        onView(withIndex(withText("Delete"), 2)).perform(click())

        val endSize = 6
        val expectedCells =
                calculateScorePadTableData(
                        arrows.filterIndexed { i, _ -> i < endSize || i >= endSize * 2 },
                        endSize,
                        GoldsType.TENS,
                        activity.activity.resources
                )

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "wait for row to be removed"
            }

            override fun checkCondition(): Boolean {
                return getTableAdapter().getCellColumnItems(2).size == expectedCells.size
            }
        })

        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], getTableAdapter().getCellRowItems(i))
        }

        var col = 0
        val expectedColumnHeaders = listOf("Arrows", "H", "S", "10", "R/T").map { InfoTableCell(it, "col" + col++) }
        for (i in expectedColumnHeaders.indices) {
            assertEquals(expectedColumnHeaders[i], getTableAdapter().getColumnHeaderItem(i))
        }

        val expectedRowHeaders = generateNumberedRowHeaders(5, null, activity.activity.resources, true)
        for (i in expectedRowHeaders.indices) {
            assertEquals(expectedRowHeaders[i], getTableAdapter().getRowHeaderItem(i))
        }
    }
}