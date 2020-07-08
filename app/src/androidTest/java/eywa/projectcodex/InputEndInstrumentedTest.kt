package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.infoTable.InfoTableCell
import eywa.projectcodex.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InputEndInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }
    }

    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)
    private lateinit var db: ScoresRoomDatabase
    private val emptyEnd = ".-.-.-.-.-."
    private val arrowsPerArrowCount = 12
    private val roundsInput = listOf(Round(1, "test", "Test", true, true, listOf()))
    private val arrowCountsInput = listOf(
            RoundArrowCount(1, 1, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 2, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 3, 1.0, arrowsPerArrowCount)
    )
    private val distancesInput = listOf(
            RoundDistance(1, 1, 1, 90),
            RoundDistance(1, 2, 1, 70),
            RoundDistance(1, 3, 1, 50)
    )

    @Before
    fun beforeEach() {
        activity.activity.supportFragmentManager.beginTransaction()
        db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext, GlobalScope)

        /*
         * Fill default rounds
         */
        Handler(Looper.getMainLooper()).post {
            for (i in distancesInput.indices) {
                runBlocking {
                    if (i < roundsInput.size) {
                        db.roundDao().insert(roundsInput[i])
                    }
                    if (i < arrowCountsInput.size) {
                        db.roundArrowCountDao().insert(arrowCountsInput[i])
                    }
                    if (i < distancesInput.size) {
                        db.roundDistanceDao().insert(distancesInput[i])
                    }
                }
            }
        }

        /*
         * Navigate to create round screen
         */
        R.id.button_start_new_round.click()
    }

    @After
    fun afterEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
    }

    @Test
    @Throws(Exception::class)
    fun testScoreButtonPressed() {
        R.id.button_create_round.click()

        val buttons = mapOf(
                R.id.button_score_0 to "m",
                R.id.button_score_1 to "1",
                R.id.button_score_2 to "2",
                R.id.button_score_3 to "3",
                R.id.button_score_4 to "4",
                R.id.button_score_5 to "5",
                R.id.button_score_6 to "6",
                R.id.button_score_7 to "7",
                R.id.button_score_8 to "8",
                R.id.button_score_9 to "9",
                R.id.button_score_10 to "10",
                R.id.button_score_x to "X"
        )

        // Pressing each button
        for (button in buttons) {
            val expected: Int = when {
                button.value == "m" -> 0
                button.value == "X" -> 10
                else -> Integer.parseInt(button.value)
            }
            button.key.click()
            R.id.text_arrow_scores.textEquals(button.value + emptyEnd.substring(1))
            R.id.text_end_total.textEquals(expected.toString())

            button.key.click()
            R.id.text_arrow_scores.textEquals(button.value + "-" + button.value + emptyEnd.substring(3))
            R.id.text_end_total.textEquals((expected * 2).toString())

            R.id.button_clear_end.click()
            R.id.text_arrow_scores.textEquals(emptyEnd)
            R.id.text_end_total.textEquals("0")
        }

        // Filling an end
        R.id.button_score_3.click()
        R.id.button_score_7.click()
        R.id.text_arrow_scores.textEquals("3-7-.-.-.-.")
        R.id.text_end_total.textEquals("10")
        R.id.button_score_3.click()
        R.id.text_arrow_scores.textEquals("3-7-3-.-.-.")
        R.id.text_end_total.textEquals("13")
        R.id.button_score_1.click()
        R.id.button_score_1.click()
        R.id.button_score_3.click()
        R.id.text_arrow_scores.textEquals("3-7-3-1-1-3")
        R.id.text_end_total.textEquals("18")

        // Too many arrows
        R.id.button_score_7.click()
        activity containsToast "Arrows already added"
        R.id.text_arrow_scores.textEquals("3-7-3-1-1-3")
        R.id.text_end_total.textEquals("18")
    }

    @Test
    @Throws(Exception::class)
    fun testClearScore() {
        R.id.button_create_round.click()

        // Full score
        R.id.button_score_3.click()
        R.id.button_score_7.click()
        R.id.button_score_3.click()
        R.id.button_score_1.click()
        R.id.button_score_1.click()
        R.id.button_score_3.click()
        R.id.text_arrow_scores.textEquals("3-7-3-1-1-3")
        R.id.text_end_total.textEquals("18")
        R.id.button_clear_end.click()
        R.id.text_arrow_scores.textEquals(emptyEnd)
        R.id.text_end_total.textEquals("0")

        // Partial score
        R.id.button_score_3.click()
        R.id.button_score_7.click()
        R.id.button_score_3.click()
        R.id.button_score_1.click()
        R.id.text_arrow_scores.textEquals("3-7-3-1-.-.")
        R.id.text_end_total.textEquals("14")
        R.id.button_clear_end.click()
        R.id.text_arrow_scores.textEquals(emptyEnd)
        R.id.text_end_total.textEquals("0")

        // No score
        R.id.button_clear_end.click()
        R.id.text_arrow_scores.textEquals(emptyEnd)
        R.id.text_end_total.textEquals("0")
    }

    @Test
    @Throws(Exception::class)
    fun testBackSpace() {
        R.id.button_create_round.click()

        // Full score
        R.id.button_score_3.click()
        R.id.button_score_7.click()
        R.id.button_score_3.click()
        R.id.button_score_1.click()
        R.id.button_score_1.click()
        R.id.button_score_3.click()
        R.id.text_arrow_scores.textEquals("3-7-3-1-1-3")
        R.id.text_end_total.textEquals("18")

        R.id.button_backspace.click()
        R.id.text_arrow_scores.textEquals("3-7-3-1-1-.")
        R.id.text_end_total.textEquals("15")

        R.id.button_backspace.click()
        R.id.text_arrow_scores.textEquals("3-7-3-1-.-.")
        R.id.text_end_total.textEquals("14")

        R.id.button_backspace.click()
        R.id.button_backspace.click()
        R.id.text_arrow_scores.textEquals("3-7-.-.-.-.")
        R.id.text_end_total.textEquals("10")

        R.id.button_backspace.click()
        R.id.button_backspace.click()
        R.id.text_arrow_scores.textEquals(emptyEnd)
        R.id.text_end_total.textEquals("0")

        R.id.button_backspace.click()
        activity containsToast "No arrows entered"
        R.id.text_arrow_scores.textEquals(emptyEnd)
        R.id.text_end_total.textEquals("0")
    }

    @Test
    @Throws(Exception::class)
    fun testNextEnd() {
        R.id.button_create_round.click()

        R.id.text_table_score_1.textEquals("0")
        R.id.text_table_arrow_count_1.textEquals("0")

        // End 1
        R.id.button_score_3.click()
        R.id.button_score_7.click()
        R.id.button_score_3.click()
        R.id.button_score_1.click()
        R.id.button_score_1.click()
        R.id.button_score_3.click()
        R.id.text_arrow_scores.textEquals("3-7-3-1-1-3")
        R.id.text_end_total.textEquals("18")

        R.id.button_next_end.click()
        R.id.text_table_score_1.textEquals("18")
        R.id.text_table_arrow_count_1.textEquals("6")
        R.id.text_arrow_scores.textEquals(emptyEnd)
        R.id.text_end_total.textEquals("0")

        // End 2
        R.id.button_score_3.click()
        R.id.button_score_7.click()
        R.id.button_score_3.click()
        R.id.button_score_6.click()
        R.id.button_score_6.click()
        R.id.button_score_3.click()
        R.id.text_arrow_scores.textEquals("3-7-3-6-6-3")
        R.id.text_end_total.textEquals("28")

        R.id.button_next_end.click()
        R.id.text_table_score_1.textEquals("46")
        R.id.text_table_arrow_count_1.textEquals("12")
        R.id.text_arrow_scores.textEquals(emptyEnd)
        R.id.text_end_total.textEquals("0")

        // No arrows
        R.id.button_next_end.click()
        activity containsToast "Please enter all arrows for this end"
        R.id.text_table_score_1.textEquals("46")
        R.id.text_table_arrow_count_1.textEquals("12")
        R.id.text_arrow_scores.textEquals(emptyEnd)
        R.id.text_end_total.textEquals("0")

        // Some arrows
        R.id.button_score_3.click()
        R.id.button_score_7.click()
        R.id.button_score_3.click()
        R.id.button_score_6.click()
        R.id.button_score_6.click()
        R.id.text_arrow_scores.textEquals("3-7-3-6-6-.")

        R.id.button_next_end.click()
        activity containsToast "Please enter all arrows for this end"
        R.id.text_table_score_1.textEquals("46")
        R.id.text_table_arrow_count_1.textEquals("12")
        R.id.text_arrow_scores.textEquals("3-7-3-6-6-.")
    }

    @Test
    @Throws(Exception::class)
    fun testOpenScorePad() {
        R.id.button_create_round.click()

        for (i in 0.rangeTo(5)) {
            R.id.button_score_1.click()
        }
        R.id.button_next_end.click()
        R.id.button_score_pad.click()
        val tableViewAdapter = activity.activity.findViewById<TableView>(R.id.score_pad__table_view).adapter!!
                as AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
        assertEquals(2, tableViewAdapter.getCellColumnItems(0).size)
        assertEquals(5, tableViewAdapter.getCellRowItems(0)?.size)
    }

    @Test
    fun testRemainingArrowsIndicator() {
        R.id.spinner_select_round.clickSpinnerItem(roundsInput[0].displayName)
        R.id.button_create_round.click()
        R.id.text_round_indicator_large.textEquals("12 at 90m")
        R.id.text_round_indicator_small.textEquals("12 at 70m, 12 at 50m")

        completeEnd()
        R.id.text_round_indicator_large.textEquals("6 at 90m")
        R.id.text_round_indicator_small.textEquals("12 at 70m, 12 at 50m")

        completeEnd()
        R.id.text_round_indicator_large.textEquals("12 at 70m")
        R.id.text_round_indicator_small.textEquals("12 at 50m")

        completeEnd()
        R.id.text_round_indicator_large.textEquals("6 at 70m")
        R.id.text_round_indicator_small.textEquals("12 at 50m")

        completeEnd()
        R.id.text_round_indicator_large.textEquals("12 at 50m")
        R.id.text_round_indicator_small.textEquals("")

        completeEnd()
        R.id.text_round_indicator_large.textEquals("6 at 50m")
        R.id.text_round_indicator_small.textEquals("")

        completeEnd()
        R.id.text_round_indicator_label.textEquals("Round Complete")
    }

    private fun completeEnd() {
        while (activity.activity.findViewById<TextView>(R.id.text_arrow_scores).text.contains('.')) {
            R.id.button_score_1.click()
        }
        R.id.button_next_end.click()
    }
}
