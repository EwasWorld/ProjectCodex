package eywa.projectcodex

import android.os.Handler
import android.os.Looper
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
        val expected = calculateViewRoundsTableData(archerRounds, arrows.flatten(), GoldsType.TENS, "Y", "N")
        assertEquals(expected, tableViewAdapter.cellItems as List<List<InfoTableCell>>)
        var col = 0
        assertEquals(
                listOf("Date", "H", "S", "10", "HC").map { InfoTableCell(it, "col" + col++) },
                tableViewAdapter.columnHeaderItems
        )
        assertEquals(generateNumberedRowHeaders(expected.size), tableViewAdapter.rowHeaderItems)
    }
}