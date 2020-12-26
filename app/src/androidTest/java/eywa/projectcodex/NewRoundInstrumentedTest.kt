package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TimePicker
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class NewRoundInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }
    }

    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)

    private lateinit var db: ScoresRoomDatabase
    private var currentArcherRounds: List<ArcherRound> = listOf()
    private val noRoundDisplayName = "No Round"
    private val roundsListSizes = 3
    private val roundsInput = TestData.generateRounds(roundsListSizes)
    private val subtypesInput = TestData.generateSubTypes(roundsListSizes - 1, roundsListSizes, roundsListSizes)
    private val arrowCountsInput = TestData.generateArrowCounts(roundsListSizes, roundsListSizes, roundsListSizes)

    // Distances should always be the largest
    private val distancesInput = TestData.generateDistances(roundsListSizes - 1, roundsListSizes, roundsListSizes)
            .plus(
                    listOf(
                            RoundDistance(roundsListSizes, 1, 1, 70),
                            RoundDistance(roundsListSizes, 2, 1, 60),
                            RoundDistance(roundsListSizes, 3, 1, 50)
                    )
            )

    /**
     * Set up database and navigate to create rounds screen
     */
    @Before
    fun roundsBeforeCreateEach() {
        activity.activity.supportFragmentManager.beginTransaction()
        db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext, GlobalScope)

        /*
         * Observe created rounds
         */
        Handler(Looper.getMainLooper()).post {
            db.archerRoundDao().getAllArcherRounds().observe(activity.activity, Observer { obArcherRounds ->
                currentArcherRounds = obArcherRounds
            })
        }

        /*
         * Fill default rounds
         */
        Handler(Looper.getMainLooper()).post {
            for (i in distancesInput.indices) {
                runBlocking {
                    if (i < roundsInput.size) {
                        db.roundDao().insert(roundsInput[i])
                    }
                    if (i < subtypesInput.size) {
                        db.roundSubTypeDao().insert(subtypesInput[i])
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
        ScoresRoomDatabase.clearInstance(activity.activity)
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun addRoundNoType() {
        R.id.button_create_round__submit.click()

        assertEquals(1, currentArcherRounds.size)
        assertEquals(1, currentArcherRounds[0].archerRoundId)

        Espresso.pressBack()
        R.id.button_main_menu__start_new_round.click()

        val roundsBeforeCreate = currentArcherRounds
        assertEquals(1, roundsBeforeCreate.size)

        R.id.button_create_round__submit.click()
        val roundsAfterCreate = currentArcherRounds.toMutableList()
        assertEquals(2, roundsAfterCreate.size)

        roundsAfterCreate.removeAll(roundsBeforeCreate)
        assertEquals(1, roundsAfterCreate.size)
        assert(
                roundsAfterCreate[0].archerRoundId
                        > roundsBeforeCreate.maxBy { round -> round.archerId }!!.archerRoundId
        )
        assertEquals(null, roundsAfterCreate[0].roundId)
        assertEquals(null, roundsAfterCreate[0].roundSubTypeId)
    }

    /**
     * Test row added to archer_round
     * Test round and subtype id are correct
     */
    @Test
    fun addRoundWithSubtype() {
        val roundsBeforeCreate = currentArcherRounds
        assertEquals(0, roundsBeforeCreate.size)

        val selectedRound = roundsInput[1]
        R.id.spinner_create_round__round.clickSpinnerItem(selectedRound.displayName)
        val selectedSubtype = subtypesInput.filter { it.roundId == selectedRound.roundId }[1]
        R.id.spinner_create_round__round_sub_type.clickSpinnerItem(selectedSubtype.name!!)

        R.id.button_create_round__submit.click()
        val roundsAfterCreate = currentArcherRounds.toMutableList()
        assertEquals(1, roundsAfterCreate.size)
        assertEquals(selectedRound.roundId, roundsAfterCreate[0].roundId)
        assertEquals(selectedSubtype.roundId, roundsAfterCreate[0].roundSubTypeId)
    }

    /**
     * Test round select spinner options are correct
     * Test round info indicators are correct
     * Test spinners and info indicators show and hide correctly
     */
    @Test
    fun roundsSpinner() {
        /*
         * Check spinner options
         */
        val roundSpinner = activity.activity.findViewById<Spinner>(R.id.spinner_create_round__round)
        // + 1 for 'no rounds'
        assertEquals(roundsInput.size + 1, roundSpinner.count)
        for (i in roundsInput.indices) {
            assertEquals(roundsInput[i].displayName, roundSpinner.getItemAtPosition(i + 1) as String)
        }

        R.id.text_create_round__arrow_count_indicator.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.text_create_round__distance_indicator.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.spinner_create_round__round_sub_type.visibilityIs(ViewMatchers.Visibility.GONE)

        /*
         * Check switches correctly and indicators are correct
         */
        val subTypedRound = roundsInput[0]
        val unSubTypedRound = roundsInput[roundsListSizes - 1]

        // no round -> subtype
        var selectedRound = subTypedRound
        R.id.spinner_create_round__round.clickSpinnerItem(selectedRound.displayName)
        R.id.text_create_round__arrow_count_indicator.textEquals(expectedArrowCounts(selectedRound.roundId))
        R.id.text_create_round__distance_indicator.textEquals(
                expectedDistances(selectedRound.roundId, 1, selectedRound.isMetric)
        )
        R.id.text_create_round__arrow_count_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_create_round__distance_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.spinner_create_round__round_sub_type.visibilityIs(ViewMatchers.Visibility.VISIBLE)

        // subtype -> no subtype
        selectedRound = unSubTypedRound
        R.id.spinner_create_round__round.clickSpinnerItem(selectedRound.displayName)
        R.id.text_create_round__arrow_count_indicator.textEquals(expectedArrowCounts(selectedRound.roundId))
        R.id.text_create_round__distance_indicator.textEquals(
                expectedDistances(selectedRound.roundId, 1, selectedRound.isMetric)
        )
        R.id.text_create_round__arrow_count_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_create_round__distance_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.spinner_create_round__round_sub_type.visibilityIs(ViewMatchers.Visibility.GONE)

        // no subtype -> no round
        R.id.spinner_create_round__round.clickSpinnerItem(noRoundDisplayName)
        R.id.text_create_round__arrow_count_indicator.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.text_create_round__distance_indicator.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.spinner_create_round__round_sub_type.visibilityIs(ViewMatchers.Visibility.GONE)

        // no round -> no subtype
        R.id.spinner_create_round__round.clickSpinnerItem(unSubTypedRound.displayName)
        R.id.text_create_round__arrow_count_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_create_round__distance_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.spinner_create_round__round_sub_type.visibilityIs(ViewMatchers.Visibility.GONE)

        // no subtype -> subtype
        R.id.spinner_create_round__round.clickSpinnerItem(subTypedRound.displayName)
        R.id.text_create_round__arrow_count_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_create_round__distance_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.spinner_create_round__round_sub_type.visibilityIs(ViewMatchers.Visibility.VISIBLE)

        // subtype -> no round
        R.id.spinner_create_round__round.clickSpinnerItem(noRoundDisplayName)
        R.id.text_create_round__arrow_count_indicator.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.text_create_round__distance_indicator.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.spinner_create_round__round_sub_type.visibilityIs(ViewMatchers.Visibility.GONE)
    }

    /**
     * Test round subtype select spinner options are correct
     * Test round info indicators are correct
     */
    @Test
    fun subTypeSpinner() {
        val subtypeSpinner = activity.activity.findViewById<Spinner>(R.id.spinner_create_round__round_sub_type)

        /*
         * Check spinner options
         */
        val selectedRound = roundsInput[0]
        R.id.spinner_create_round__round.clickSpinnerItem(selectedRound.displayName)
        val availableRounds = subtypesInput.filter { it.roundId == selectedRound.roundId }
        assertEquals(availableRounds.size, subtypeSpinner.count)
        for (i in availableRounds.indices) {
            assertEquals(availableRounds[i].name, subtypeSpinner.getItemAtPosition(i) as String)
        }

        /*
         * Select a round and check the output
         */
        val selectedSubtype = subtypesInput[1]
        R.id.spinner_create_round__round_sub_type.clickSpinnerItem(selectedSubtype.name!!)
        R.id.text_create_round__arrow_count_indicator.textEquals(expectedArrowCounts(selectedRound.roundId))
        R.id.text_create_round__distance_indicator.textEquals(
                expectedDistances(selectedRound.roundId, selectedSubtype.subTypeId, selectedRound.isMetric)
        )
    }

    private fun expectedArrowCounts(roundId: Int): String {
        // `/12` because it's in dozens
        return arrowCountsInput.filter { it.roundId == roundId }.sortedBy { it.distanceNumber }
                .map { it.arrowCount / 12 }.joinToString(", ")
    }

    private fun expectedDistances(roundId: Int, subTypeId: Int, isMetric: Boolean): String {
        val unit = if (isMetric) "m" else "yd"
        return distancesInput.filter { it.roundId == roundId && it.subTypeId == subTypeId }
                .sortedByDescending { it.distance }.joinToString(", ") { it.distance.toString() + unit }
    }

    @Test
    fun testCustomDateTime() {
        R.id.text_create_round__time.click()
        onView(TimePicker::class.java).perform(setTimePickerValue(20, 22))
        onView("OK").perform(ViewActions.click())

        R.id.text_create_round__date.click()
        val calendar = Calendar.getInstance()
        // Use a different hour/minute to ensure it's not overwriting the time
        calendar.set(2040, 9, 30, 13, 15, 0)
        onView(DatePicker::class.java).perform(setDatePickerValue(calendar))
        onView("OK").perform(ViewActions.click())

        R.id.text_create_round__time.textEquals("20:22")
        R.id.text_create_round__date.textEquals("30 Oct 40")

        val roundsBeforeCreate = currentArcherRounds.toMutableList()
        R.id.button_create_round__submit.click()
        val roundsAfterCreate = currentArcherRounds.toMutableList()

        for (round in roundsAfterCreate) {
            if (roundsBeforeCreate.contains(round)) continue
            // Date returns year -1900
            assertEquals(140, round.dateShot.year)
            assertEquals(9, round.dateShot.month)
            assertEquals(30, round.dateShot.date)
            assertEquals(20, round.dateShot.hours)
            assertEquals(22, round.dateShot.minutes)
        }
    }
}