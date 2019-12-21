package eywa.projectcodex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.After
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
    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)
    private val emptyEnd = ".-.-.-.-.-."

    lateinit var db: SQLiteDatabase

    @Before
    fun beforeEach() {
        activity.activity.supportFragmentManager.beginTransaction()
        db = activity.activity.openOrCreateDatabase("TestDatabase", Context.MODE_PRIVATE, null)
    }

    @After
    fun afterEach() {
        activity.activity.deleteDatabase("TestDatabase")
    }

    @Test
    @Throws(Exception::class)
    fun testScoreButtonPressed() {
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
        // TODO Implement (need some way to check it worked)
    }
}
