package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import android.widget.NumberPicker
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
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
        R.id.button_main_menu__start_new_round.click()
    }

    @After
    fun afterEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
    }

    @Test
    fun testScoreButtonPressed() {
        R.id.button_create_round__submit.click()

        val buttons = mapOf(
                R.id.button_arrow_inputs__score_0 to "m",
                R.id.button_arrow_inputs__score_1 to "1",
                R.id.button_arrow_inputs__score_2 to "2",
                R.id.button_arrow_inputs__score_3 to "3",
                R.id.button_arrow_inputs__score_4 to "4",
                R.id.button_arrow_inputs__score_5 to "5",
                R.id.button_arrow_inputs__score_6 to "6",
                R.id.button_arrow_inputs__score_7 to "7",
                R.id.button_arrow_inputs__score_8 to "8",
                R.id.button_arrow_inputs__score_9 to "9",
                R.id.button_arrow_inputs__score_10 to "10",
                R.id.button_arrow_inputs__score_x to "X"
        )

        // Pressing each button
        for (button in buttons) {
            val expected: Int = when (button.value) {
                "m" -> 0
                "X" -> 10
                else -> Integer.parseInt(button.value)
            }
            button.key.click()
            R.id.text_end_inputs__inputted_arrows.textEquals(button.value + emptyEnd.substring(1))
            R.id.text_end_inputs__end_total.textEquals(expected.toString())

            button.key.click()
            R.id.text_end_inputs__inputted_arrows.textEquals(button.value + "-" + button.value + emptyEnd.substring(3))
            R.id.text_end_inputs__end_total.textEquals((expected * 2).toString())

            R.id.button_end_inputs__clear.click()
            R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
            R.id.text_end_inputs__end_total.textEquals("0")
        }

        // Filling an end
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_7.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-.-.-.-.")
        R.id.text_end_inputs__end_total.textEquals("10")
        R.id.button_arrow_inputs__score_3.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-.-.-.")
        R.id.text_end_inputs__end_total.textEquals("13")
        R.id.button_arrow_inputs__score_1.click()
        R.id.button_arrow_inputs__score_1.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-1-3")
        R.id.text_end_inputs__end_total.textEquals("18")

        // Too many arrows
        R.id.button_arrow_inputs__score_7.click()
        activity containsToast "Arrows already added"
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-1-3")
        R.id.text_end_inputs__end_total.textEquals("18")
    }

    @Test
    fun testClearScore() {
        R.id.button_create_round__submit.click()

        // Full score
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_7.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_1.click()
        R.id.button_arrow_inputs__score_1.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-1-3")
        R.id.text_end_inputs__end_total.textEquals("18")
        R.id.button_end_inputs__clear.click()
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")

        // Partial score
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_7.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_1.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-.-.")
        R.id.text_end_inputs__end_total.textEquals("14")
        R.id.button_end_inputs__clear.click()
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")

        // No score
        R.id.button_end_inputs__clear.click()
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")
    }

    @Test
    fun testBackSpace() {
        R.id.button_create_round__submit.click()

        // Full score
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_7.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_1.click()
        R.id.button_arrow_inputs__score_1.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-1-3")
        R.id.text_end_inputs__end_total.textEquals("18")

        R.id.button_end_inputs__backspace.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-1-.")
        R.id.text_end_inputs__end_total.textEquals("15")

        R.id.button_end_inputs__backspace.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-.-.")
        R.id.text_end_inputs__end_total.textEquals("14")

        R.id.button_end_inputs__backspace.click()
        R.id.button_end_inputs__backspace.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-.-.-.-.")
        R.id.text_end_inputs__end_total.textEquals("10")

        R.id.button_end_inputs__backspace.click()
        R.id.button_end_inputs__backspace.click()
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")

        R.id.button_end_inputs__backspace.click()
        activity containsToast "No arrows entered"
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")
    }

    @Test
    fun testNextEnd() {
        R.id.button_create_round__submit.click()

        R.id.text_scores_indicator__table_score_1.textEquals("0")
        R.id.text_scores_indicator__table_arrow_count_1.textEquals("0")

        // End 1
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_7.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_1.click()
        R.id.button_arrow_inputs__score_1.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-1-3")
        R.id.text_end_inputs__end_total.textEquals("18")

        R.id.button_input_end__next_end.click()
        R.id.text_scores_indicator__table_score_1.textEquals("18")
        R.id.text_scores_indicator__table_arrow_count_1.textEquals("6")
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")

        // End 2
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_7.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_6.click()
        R.id.button_arrow_inputs__score_6.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-6-6-3")
        R.id.text_end_inputs__end_total.textEquals("28")

        R.id.button_input_end__next_end.click()
        R.id.text_scores_indicator__table_score_1.textEquals("46")
        R.id.text_scores_indicator__table_arrow_count_1.textEquals("12")
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")

        // No arrows
        R.id.button_input_end__next_end.click()
        activity containsToast "Please enter all arrows for this end"
        R.id.text_scores_indicator__table_score_1.textEquals("46")
        R.id.text_scores_indicator__table_arrow_count_1.textEquals("12")
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")

        // Some arrows
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_7.click()
        R.id.button_arrow_inputs__score_3.click()
        R.id.button_arrow_inputs__score_6.click()
        R.id.button_arrow_inputs__score_6.click()
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-6-6-.")

        R.id.button_input_end__next_end.click()
        activity containsToast "Please enter all arrows for this end"
        R.id.text_scores_indicator__table_score_1.textEquals("46")
        R.id.text_scores_indicator__table_arrow_count_1.textEquals("12")
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-6-6-.")
    }

    @Test
    fun testOpenScorePad() {
        R.id.button_create_round__submit.click()

        for (i in 0.rangeTo(5)) {
            R.id.button_arrow_inputs__score_1.click()
        }
        R.id.button_input_end__next_end.click()
        R.id.button_input_end__score_pad.click()
        val tableViewAdapter = activity.activity.findViewById<TableView>(R.id.table_view_score_pad).adapter!!
        assertEquals(2, tableViewAdapter.getCellColumnItems(0).size)
        assertEquals(5, tableViewAdapter.getCellRowItems(0)?.size)
    }

    @Test
    fun testRemainingArrowsIndicator() {
        R.id.spinner_create_round__round.clickSpinnerItem(roundsInput[0].displayName)
        R.id.button_create_round__submit.click()
        R.id.text_input_end__remaining_arrows_current_distance.textEquals("12 at 90m")
        R.id.text_input_end__remaining_arrows_later_distances.textEquals("12 at 70m, 12 at 50m")

        completeEnd()
        R.id.text_input_end__remaining_arrows_current_distance.textEquals("6 at 90m")
        R.id.text_input_end__remaining_arrows_later_distances.textEquals("12 at 70m, 12 at 50m")

        completeEnd()
        R.id.text_input_end__remaining_arrows_current_distance.textEquals("12 at 70m")
        R.id.text_input_end__remaining_arrows_later_distances.textEquals("12 at 50m")

        completeEnd()
        R.id.text_input_end__remaining_arrows_current_distance.textEquals("6 at 70m")
        R.id.text_input_end__remaining_arrows_later_distances.textEquals("12 at 50m")

        completeEnd()
        R.id.text_input_end__remaining_arrows_current_distance.textEquals("12 at 50m")
        R.id.text_input_end__remaining_arrows_later_distances.textEquals("")

        completeEnd()
        R.id.text_input_end__remaining_arrows_current_distance.textEquals("6 at 50m")
        R.id.text_input_end__remaining_arrows_later_distances.textEquals("")

        completeEnd()
        R.id.text_input_end__remaining_arrows_label.textEquals("Round Complete")
    }

    /**
     * Fills the end by pressing the '1' button then presses 'next end'
     */
    private fun completeEnd() {
        while (activity.activity.findViewById<TextView>(R.id.text_end_inputs__inputted_arrows).text.contains('.')) {
            R.id.button_arrow_inputs__score_1.click()
        }
        R.id.button_input_end__next_end.click()
    }

    @Test
    fun testOddEndSize() {
        R.id.spinner_create_round__round.clickSpinnerItem(roundsInput[0].displayName)
        R.id.button_create_round__submit.click()

        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.-.")
        R.id.text_end_inputs__inputted_arrows.click()
        onView(withClassName(Matchers.equalTo(NumberPicker::class.java.name))).perform(setNumberPickerValue(5))
        onView(withText("OK")).perform(click())

        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.")
        completeEnd()
        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.")
        completeEnd()
        R.id.text_end_inputs__inputted_arrows.textEquals(".-.")
        completeEnd()
        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.")
    }
}
