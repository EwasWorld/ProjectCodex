package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.infoTable.InfoTableCell
import eywa.projectcodex.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.logic.GoldsType
import eywa.projectcodex.ui.MainActivity
import eywa.projectcodex.ui.ScorePadFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ViewRoundsInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }
    }

    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)

    private val menuButtonScorePad = "Show score pad"
    private val menuButtonDelete = "Delete round"

    // RoundId and handicap
    private val removedColumnIndexes = listOf(0, 6)

    private lateinit var tableViewAdapter: AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
    private lateinit var archerRounds: List<ArcherRoundWithRoundInfoAndName>
    private lateinit var round: Round
    private lateinit var roundSubType: RoundSubType
    private var arrows: MutableList<List<ArrowValue>> = mutableListOf()

    @Before
    fun beforeEach() {
        activity.activity.supportFragmentManager.beginTransaction()
    }

    private fun addDataToDatabase() {
        val db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext, GlobalScope)
        round = TestData.generateRounds(1)[0]
        roundSubType = TestData.generateSubTypes(1)[0]
        archerRounds = TestData.generateArcherRounds(5, 1, listOf(1, 1, null), listOf(1, null, null))
                .mapIndexed { i, archerRound ->
                    val roundInfo = if (i % 3 == 0 || i % 3 == 1) round else null
                    val roundSubTypeName = if (i % 3 == 0) roundSubType.name else null
                    ArcherRoundWithRoundInfoAndName(archerRound, roundInfo, roundSubTypeName)
                }
        for (round in archerRounds) {
            arrows.add(TestData.generateArrowValues(36, round.archerRound.archerRoundId))
        }
        Handler(Looper.getMainLooper()).post {
            for (archerRound in archerRounds) {
                runBlocking {
                    db.archerRoundDao().insert(archerRound.archerRound)
                }
            }
            for (arrow in arrows.flatten()) {
                runBlocking {
                    db.arrowValueDao().insert(arrow)
                }
            }
            runBlocking {
                db.roundDao().insert(round)
                db.roundSubTypeDao().insert(roundSubType)
            }
        }
    }

    private fun goToViewRoundsAndPopulateAdapter() {
        R.id.button_main_menu__view_rounds.click()
        tableViewAdapter = activity.activity.findViewById<TableView>(R.id.table_view_view_rounds).adapter!!
                as AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
    }

    @After
    fun afterEach() {
        ScoresRoomDatabase.clearInstance(activity.activity)
    }

    @Test
    fun testTableValues() {
        addDataToDatabase()
        goToViewRoundsAndPopulateAdapter()

        val expected =
            calculateViewRoundsTableData(archerRounds, arrows.flatten(), GoldsType.TENS, activity.activity.resources)
        for (i in expected.indices) {
            assertEquals(
                    expected[i].filterIndexed { j, _ -> !removedColumnIndexes.contains(j) },
                    tableViewAdapter.getCellRowItems(i) as List<InfoTableCell>
            )
        }
        var col = 0
        val expectedColumns =
                listOf("ID", "Date", "Round", "H", "S", "G", "HC").map { InfoTableCell(it, "col" + col++) }
                .filterIndexed { i, _ -> !removedColumnIndexes.contains(i) }
        for (i in expectedColumns.indices) {
            assertEquals(expectedColumns[i], tableViewAdapter.getColumnHeaderItem(i))
        }
        val expectedRows = generateNumberedRowHeaders(expected.size)
        for (i in expectedRows.indices) {
            assertEquals(expectedRows[i], tableViewAdapter.getRowHeaderItem(i))
        }
    }

    @Test
    fun testEmptyTable() {
        goToViewRoundsAndPopulateAdapter()
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        onView(withText("Main Menu")).check(matches(isDisplayed()))
    }

    @Test
    fun testOpenScorePad() {
        // Generate rounds until there is a unique score
        var uniqueScore: Int? = null
        while (uniqueScore == null) {
            addDataToDatabase()
            val scores = arrows.map { roundArrows -> roundArrows.sumBy { arrow -> arrow.score } }

            for ((i, score) in scores.withIndex()) {
                if (scores.lastIndexOf(score) == i) {
                    uniqueScore = score
                    break
                }
            }
        }
        goToViewRoundsAndPopulateAdapter()

        // Click on that unique score
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())
        onView(withText(uniqueScore.toString())).perform(longClick())
        onView(withText(menuButtonScorePad)).perform(click())
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        onView(withText("Score Pad")).check(matches(isDisplayed()))
        tableViewAdapter = activity.activity.findViewById<TableView>(R.id.table_view_score_pad).adapter!!
                as AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>

        // Check the last running total is the unique score
        val maxColIndex = tableViewAdapter.getCellRowItems(0)?.size!! - 1
        // -2 to ignore the total row
        val maxRowIndex = tableViewAdapter.getCellColumnItems(0).size - 2
        assertEquals(
                uniqueScore,
                tableViewAdapter.getCellItem(maxColIndex, maxRowIndex)?.content!! as Int
        )
    }

    @Test
    fun testDeleteRow() {
        addDataToDatabase()
        goToViewRoundsAndPopulateAdapter()
        var expected: List<List<InfoTableCell>> =
                calculateViewRoundsTableData(archerRounds, arrows.flatten(), GoldsType.TENS, activity.activity.resources)

        assertEquals(expected.size, tableViewAdapter.getCellColumnItems(2).size)
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())

        // Which row to delete
        val deleteIndex = 1
        val archerRoundsByDate = archerRounds.map { it.archerRound }.sortedByDescending { it.dateShot }
        val deleteRound = arrows.find { it[0].archerRoundId == archerRoundsByDate[deleteIndex].archerRoundId }
        if (deleteRound == null) fail("Round to delete not found")

        onView(deleteRound!!.sumBy { it.score }.toString()).perform(longClick())
        onView(withText(menuButtonDelete)).perform(click())
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "wait for row to be removed"
            }

            override fun checkCondition(): Boolean {
                return tableViewAdapter.getCellColumnItems(2).size == expected.size - 1
            }
        })
        assertEquals(expected.size - 1, tableViewAdapter.getCellColumnItems(2).size)

        // Only check contents as the ids will have changed when the table recalculated itself
        expected = expected.minusElement(expected[deleteIndex])
        for (i in expected.indices) {
            assertEquals(
                    expected[i].filterIndexed { j, _ -> !removedColumnIndexes.contains(j) }.map { it.content },
                    (tableViewAdapter.getCellRowItems(i) as List<InfoTableCell>).map { it.content }
            )
        }
    }
}