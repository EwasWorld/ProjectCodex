package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.InfoTableCell
import eywa.projectcodex.infoTable.calculateScorePadTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ph.ingenuity.tableview.TableView
import ph.ingenuity.tableview.adapter.AbstractTableAdapter

@RunWith(AndroidJUnit4::class)
class ScorePadInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }
    }

    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)

    private lateinit var arrows: List<ArrowValue>
    private lateinit var tableViewAdapter: AbstractTableAdapter

    @Before
    fun beforeEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
        activity.activity.supportFragmentManager.beginTransaction()
    }

    private fun addDataToDatabase() {
        val db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext, GlobalScope)
        arrows = TestData.generateArrowValues(36, 1)
        Handler(Looper.getMainLooper()).post {
            for (arrow in arrows) {
                runBlocking {
                    db.arrowValueDao().insert(arrow)
                }
            }
        }
    }

    private fun goToViewRoundsAndPopulateAdapter() {
        R.id.button_start_new_round.click()
        R.id.button_create_round.click()
        R.id.button_score_pad.click()
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
        assertEquals(
                calculateScorePadTableData(arrows, 6, GoldsType.TENS, ".", "-"),
                tableViewAdapter.cellItems as List<List<InfoTableCell>>
        )
        var col = 0
        assertEquals(
                listOf("E/T", "H", "S", "10", "R/T").map { InfoTableCell(it, "col" + col++) },
                tableViewAdapter.columnHeaderItems
        )
        assertEquals(generateNumberedRowHeaders(6), tableViewAdapter.rowHeaderItems)
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyTable() {
        goToViewRoundsAndPopulateAdapter()
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(ViewActions.click())
        onView(withText("Input End")).check(matches(isDisplayed()))
    }
}