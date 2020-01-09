package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.InfoTableCell
import eywa.projectcodex.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ph.ingenuity.tableview.TableView
import ph.ingenuity.tableview.adapter.AbstractTableAdapter

class ViewRoundsInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }
    }

    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)

    private lateinit var tableViewAdapter: AbstractTableAdapter
    private lateinit var archerRounds: List<ArcherRound>
    private var arrows: MutableList<List<ArrowValue>> = mutableListOf()

    @Before
    fun beforeEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
        activity.activity.supportFragmentManager.beginTransaction()
    }

    private fun addDataToDatabase() {
        val db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext, GlobalScope)
        archerRounds = TestData.generateArcherRounds(5, 1)
        for (round in archerRounds) {
            arrows.add(TestData.generateArrowValues(36, round.archerRoundId))
        }
        Handler(Looper.getMainLooper()).post {
            for (archerRound in archerRounds) {
                runBlocking {
                    db.archerRoundDao().insert(archerRound)
                }
            }
            for (arrow in arrows.flatten()) {
                runBlocking {
                    db.arrowValueDao().insert(arrow)
                }
            }
        }
    }

    private fun goToViewRoundsAndPopulateAdapter() {
        R.id.button_view_rounds.click()
        tableViewAdapter = activity.activity.findViewById<TableView>(R.id.table_view).adapter!!
    }

    @After
    fun afterEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
    }

    @Test
    @Throws(Exception::class)
    fun testTableValues() {
        addDataToDatabase()
        goToViewRoundsAndPopulateAdapter()

        val expected = calculateViewRoundsTableData(archerRounds, arrows.flatten(), GoldsType.TENS, "Y", "N")
        assertEquals(expected, tableViewAdapter.cellItems as List<List<InfoTableCell>>)
        var col = 0
        assertEquals(
                listOf("Date", "H", "S", "10", "HC", "ID").map { InfoTableCell(it, "col" + col++) },
                tableViewAdapter.columnHeaderItems
        )
        assertEquals(generateNumberedRowHeaders(expected.size), tableViewAdapter.rowHeaderItems)
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyTable() {
        goToViewRoundsAndPopulateAdapter()
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        onView(withText("Main Menu")).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
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
        onView(withText(uniqueScore.toString())).perform(click())
        onView(withText("Score Pad")).check(matches(isDisplayed()))
        tableViewAdapter = activity.activity.findViewById<TableView>(R.id.table_view).adapter!!
        // Check the last running total is the unique score
        val cellItems = tableViewAdapter.cellItems!!
        assertEquals(
                uniqueScore,
                (cellItems[cellItems.size - 1][cellItems[0].size - 1] as InfoTableCell).content as Int
        )
    }
}