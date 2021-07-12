package eywa.projectcodex

import android.os.Bundle
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import eywa.projectcodex.components.archerRoundScore.inputEnd.InputEndFragment
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
            SharedPrefs.sharedPreferencesCustomName = testSharedPrefsName
        }

    }

    private lateinit var scenario: FragmentScenario<InputEndFragment>
    private lateinit var navController: TestNavHostController
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
    private val archerRounds = listOf(
            ArcherRound(1, TestData.generateDate(), 1, true),
            ArcherRound(2, TestData.generateDate(), 1, true, roundId = 1)
    )

    /**
     * Set up [scenario] with desired fragment in the resumed state, [navController] to allow transitions, and [db]
     * with all desired information
     */
    private fun setup(archerRoundId: Int = 1) {
        check(archerRounds.find { it.archerRoundId == archerRoundId } != null) {
            "Desired archer round not added to the db"
        }

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val args = Bundle()
        args.putInt("archerRoundId", archerRoundId)

        // Start initialised so we can add to the database before the onCreate methods are called
        scenario = launchFragmentInContainer(args, initialState = Lifecycle.State.INITIALIZED)
        scenario.onFragment {
            ScoresRoomDatabase.clearInstance(it.requireContext())
            db = ScoresRoomDatabase.getDatabase(it.requireContext())

            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.inputEndFragment, args)

            /*
             * Fill default rounds
             */
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
                    if (i < archerRounds.size) {
                        db.archerRoundDao().insert(archerRounds[i])
                    }
                }
            }
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
    }

    @After
    fun afterEach() {
        scenario.onFragment {
            ScoresRoomDatabase.clearInstance(it.requireContext())
        }
    }

    @Test
    fun testScoreButtonPressed() {
        setup()

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
        checkContainsToast("Arrows already added")
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-1-1-3")
        R.id.text_end_inputs__end_total.textEquals("18")
    }

    @Test
    fun testClearAndBackspace() {
        setup()

        /*
         * Clear
         */
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

        /*
         * Backspace
         */
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
        checkContainsToast("No arrows entered")
        R.id.text_end_inputs__inputted_arrows.textEquals(emptyEnd)
        R.id.text_end_inputs__end_total.textEquals("0")
    }

    @Test
    fun testNextEnd() {
        setup()

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
        checkContainsToast("Please enter all arrows for this end")
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
        checkContainsToast("Please enter all arrows for this end")
        R.id.text_scores_indicator__table_score_1.textEquals("46")
        R.id.text_scores_indicator__table_arrow_count_1.textEquals("12")
        R.id.text_end_inputs__inputted_arrows.textEquals("3-7-3-6-6-.")
    }

    @Test
    fun testOpenScorePad() {
        setup()

        for (i in 0.rangeTo(5)) {
            R.id.button_arrow_inputs__score_1.click()
        }
        R.id.button_input_end__next_end.click()
        R.id.fragment_input_end__score_indicator.click()
        assertEquals(R.id.scorePadFragment, navController.currentDestination?.id)
    }

    @Test
    fun testRemainingArrowsIndicatorAndCompleteRound() {
        setup(2)

        // Give it a moment to sort out the indicators
        ConditionWatcher.waitForCondition(waitFor(1000))

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
        onViewWithClassName("OK").perform(click())
        assertEquals(R.id.mainMenuFragment, navController.currentDestination?.id)
    }

    /**
     * Fills the end by pressing the '1' button then presses 'next end'
     */
    private fun completeEnd() {
        var contains = true
        while (contains) {
            scenario.onFragment {
                contains = it.requireActivity()
                        .findViewById<TextView>(R.id.text_end_inputs__inputted_arrows).text.contains('.')
            }
            if (contains) {
                R.id.button_arrow_inputs__score_1.click()
            }
        }
        R.id.button_input_end__next_end.click()
    }

    @Test
    fun testOddEndSize() {
        setup(2)

        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.-.")
        R.id.text_end_inputs__inputted_arrows.click()

        onViewWithClassName(NumberPicker::class.java).perform(setNumberPickerValue(5))
        onViewWithClassName("OK").perform(click())

        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.")
        completeEnd()
        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.")
        completeEnd()
        R.id.text_end_inputs__inputted_arrows.textEquals(".-.")
        completeEnd()
        R.id.text_end_inputs__inputted_arrows.textEquals(".-.-.-.-.")
    }
}
