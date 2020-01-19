package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.InfoTableCell
import eywa.projectcodex.infoTable.calculateScorePadTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.ui.MainActivity
import eywa.projectcodex.ui.ScorePadFragment
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

    private lateinit var arrows: List<ArrowValue>
    private lateinit var tableViewAdapter: AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>

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
        runBlocking {
            db.archerRoundDao().insert(TestData.generateArcherRounds(1, 1)[0])
        }
    }

    @After
    fun afterEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
    }

    @Test
    @Throws(Exception::class)
    fun testTableValues() {
        addDataToDatabase()
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "Wait for data to appear in view rounds table"
            }

            override fun checkCondition(): Boolean {
                return try {
                    R.id.button_view_rounds.click()
                    onView(withText(arrows.sumBy { it.score }.toString())).perform(click())
                    true
                }
                catch (e: NoMatchingViewException) {
                    pressBack()
                    false
                }
            }
        })
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        tableViewAdapter = activity.activity.findViewById<TableView>(R.id.score_pad__table_view)?.adapter!!
                as AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>

        val expectedCells = calculateScorePadTableData(arrows, 6, GoldsType.TENS, activity.activity.resources)
        for (i in expectedCells.indices) {
            assertEquals(expectedCells[i], tableViewAdapter.getCellRowItems(i))
        }

        var col = 0
        val expectedColumnHeaders = listOf("E/T", "H", "S", "10", "R/T").map { InfoTableCell(it, "col" + col++) }
        for (i in expectedColumnHeaders.indices) {
            assertEquals(expectedColumnHeaders[i], tableViewAdapter.getColumnHeaderItem(i))
        }

        val expectedRowHeaders = generateNumberedRowHeaders(6, activity.activity.resources, true)
        for (i in expectedRowHeaders.indices) {
            assertEquals(expectedRowHeaders[i], tableViewAdapter.getRowHeaderItem(i))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyTable() {
        R.id.button_start_new_round.click()
        R.id.button_create_round.click()
        R.id.button_score_pad.click()
        tableViewAdapter = activity.activity.findViewById<TableView>(R.id.score_pad__table_view).adapter!!
                as AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        onView(withText("Input End")).check(matches(isDisplayed()))
    }
}