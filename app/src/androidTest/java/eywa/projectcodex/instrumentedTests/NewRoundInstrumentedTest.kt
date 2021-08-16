package eywa.projectcodex.instrumentedTests

import android.os.Bundle
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TimePicker
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.R
import eywa.projectcodex.TestData
import eywa.projectcodex.common.*
import eywa.projectcodex.components.newRound.NewRoundFragment
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.RoundDistance
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class NewRoundInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = CommonStrings.testDatabaseName
        }

    }

    // TODO Check date is within 1min of current date
    private lateinit var scenario: FragmentScenario<NewRoundFragment>
    private lateinit var navController: TestNavHostController
    private lateinit var db: ScoresRoomDatabase
    private var currentArcherRounds: List<ArcherRound> = listOf()
    private val noRoundDisplayName = "No Round"
    private val roundsListSizes = 3
    private val roundsInput = TestData.generateRounds(roundsListSizes)
    private val subtypesInput = TestData.generateSubTypes(roundsListSizes - 1, roundsListSizes, roundsListSizes)
    private val arrowCountsInput = TestData.ROUND_ARROW_COUNTS
    private val archerRoundInput = ArcherRound(
            1,
            Calendar.Builder().setDate(2019, 5, 10).setTimeOfDay(17, 12, 13).build().time,
            1,
            false,
            roundId = 1,
            roundSubTypeId = 2
    )

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
     * Set up [scenario] with desired fragment in the resumed state, [navController] to allow transitions, and [db]
     * with all desired information
     */
    fun setup(archerRoundId: Int = -1) {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        val args = Bundle()
        args.putInt("archerRoundId", archerRoundId)

        // Start initialised so we can add to the database before the onCreate methods are called
        scenario = launchFragmentInContainer(args, initialState = Lifecycle.State.INITIALIZED)
        scenario.onFragment {
            db = ScoresRoomDatabase.getDatabase(it.requireContext())

            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.newRoundFragment)

            /*
             * Fill default rounds
             */
            runBlocking {
                db.archerRoundDao().insert(archerRoundInput)
                for (i in distancesInput.indices) {
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


        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)

            db.archerRoundDao().getAllArcherRounds().observe(it.requireActivity(), { obArcherRounds ->
                currentArcherRounds = obArcherRounds
            })
        }
    }

    @After
    fun afterEach() {
        scenario.onFragment {
            ScoresRoomDatabase.clearInstance(it.requireContext())
        }
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun addRoundNoType() {
        setup()
        R.id.button_create_round__submit.click()

        assertEquals(2, currentArcherRounds.size)
        assertEquals(2, currentArcherRounds[1].archerRoundId)
        assertEquals(null, currentArcherRounds[1].roundId)
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun addAnotherRound() {
        setup()
        val ar = ArcherRound(2, TestData.generateDate(), 1, true)
        scenario.onFragment {
            runBlocking {
                db.archerRoundDao().insert(ar)
            }
        }

        R.id.button_create_round__submit.click()
        val roundsAfterCreate = currentArcherRounds.toMutableList()
        assertEquals(3, roundsAfterCreate.size)

        roundsAfterCreate.remove(ar)
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(archerRoundInput, roundsAfterCreate[0])
        assert(roundsAfterCreate[1].archerRoundId > ar.archerRoundId)
        assertEquals(null, roundsAfterCreate[1].roundId)
        assertEquals(null, roundsAfterCreate[1].roundSubTypeId)

        assertEquals(R.id.inputEndFragment, navController.currentDestination?.id)
        assertEquals(3, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    /**
     * Test row added to archer_round
     * Test round and subtype id are correct
     */
    @Test
    fun addRoundWithSubtype() {
        setup()
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "Wait for currentArcherRounds to be populated"
            }

            override fun checkCondition(): Boolean {
                return currentArcherRounds.isNotEmpty()
            }
        })
        val roundsBeforeCreate = currentArcherRounds
        assertEquals(1, roundsBeforeCreate.size)

        val selectedRound = roundsInput[1]
        R.id.spinner_create_round__round.clickSpinnerItem(selectedRound.displayName)
        val selectedSubtype = subtypesInput.filter { it.roundId == selectedRound.roundId }[1]
        R.id.spinner_create_round__round_sub_type.clickSpinnerItem(selectedSubtype.name!!)

        R.id.button_create_round__submit.click()
        val roundsAfterCreate = currentArcherRounds.toMutableList()
        assertEquals(2, roundsAfterCreate.size)
        assertEquals(archerRoundInput, roundsAfterCreate[0])
        assertEquals(selectedRound.roundId, roundsAfterCreate[1].roundId)
        assertEquals(selectedSubtype.roundId, roundsAfterCreate[1].roundSubTypeId)
    }

    /**
     * Test round select spinner options are correct
     * Test round info indicators are correct
     * Test spinners and info indicators show and hide correctly
     */
    @Test
    fun roundsSpinner() {
        setup()

        /*
         * Check spinner options
         */
        var roundSpinner: Spinner? = null
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "Wait for round spinner to have the correct number of items"
            }

            override fun checkCondition(): Boolean {
                scenario.onFragment {
                    roundSpinner = it.requireActivity().findViewById(R.id.spinner_create_round__round)
                }
                // + 1 for 'no rounds'
                val conditionMet = roundsInput.size + 1 == roundSpinner!!.count
                if (!conditionMet) {
                    Thread.sleep(CustomConditionWaiter.DEFAULT_THREAD_SLEEP)
                }
                return conditionMet
            }
        })
        for (i in roundsInput.indices) {
            assertEquals(roundsInput[i].displayName, roundSpinner!!.getItemAtPosition(i + 1) as String)
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
        R.id.text_create_round__arrow_count_indicator.labelledTextEquals(expectedArrowCounts(selectedRound.roundId))
        R.id.text_create_round__distance_indicator.labelledTextEquals(
                expectedDistances(selectedRound.roundId, 1, selectedRound.isMetric)
        )
        R.id.text_create_round__arrow_count_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_create_round__distance_indicator.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.spinner_create_round__round_sub_type.visibilityIs(ViewMatchers.Visibility.VISIBLE)

        // subtype -> no subtype
        selectedRound = unSubTypedRound
        R.id.spinner_create_round__round.clickSpinnerItem(selectedRound.displayName)
        R.id.text_create_round__arrow_count_indicator.labelledTextEquals("5.8, 5, 4.2")
        R.id.text_create_round__distance_indicator.labelledTextEquals(
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
        setup()
        var subtypeSpinnerTemp: Spinner? = null
        scenario.onFragment {
            subtypeSpinnerTemp = it.requireActivity().findViewById(R.id.spinner_create_round__round_sub_type)
        }
        val subtypeSpinner = subtypeSpinnerTemp!!

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
        R.id.text_create_round__arrow_count_indicator.labelledTextEquals(expectedArrowCounts(selectedRound.roundId))
        R.id.text_create_round__distance_indicator.labelledTextEquals(
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
        setup()
        R.id.text_create_round__time.click()
        onViewWithClassName(TimePicker::class.java).perform(setTimePickerValue(20, 22))
        onView(withText("OK")).perform(ViewActions.click())

        R.id.text_create_round__date.click()
        val calendar = Calendar.getInstance()
        // Use a different hour/minute to ensure it's not overwriting the time
        calendar.set(2040, 9, 30, 13, 15, 0)
        onViewWithClassName(DatePicker::class.java).perform(setDatePickerValue(calendar))
        onView(withText("OK")).perform(ViewActions.click())

        R.id.text_create_round__time.textEquals("20:22")
        R.id.text_create_round__date.textEquals("30 Oct 40")

        val roundsBeforeCreate = currentArcherRounds.toMutableList()
        R.id.button_create_round__submit.click()
        val roundsAfterCreate = currentArcherRounds.toMutableList()

        for (round in roundsAfterCreate) {
            if (roundsBeforeCreate.contains(round)) continue
            // Date returns year -1900
            val dateShot = Calendar.Builder().setInstant(round.dateShot).build()
            assertEquals(2040, dateShot.get(Calendar.YEAR))
            assertEquals(9, dateShot.get(Calendar.MONTH))
            assertEquals(30, dateShot.get(Calendar.DATE))
            assertEquals(20, dateShot.get(Calendar.HOUR_OF_DAY))
            assertEquals(22, dateShot.get(Calendar.MINUTE))
        }
    }

    @Test
    fun testEditInfo() {
        setup(archerRoundInput.archerRoundId)

        R.id.text_create_round__time.textEquals("17:12")
        R.id.text_create_round__date.textEquals("10 Jun 19")
        R.id.spinner_create_round__round.spinnerTextEquals(roundsInput.find { it.roundId == archerRoundInput.roundId }!!.displayName)
        R.id.spinner_create_round__round_sub_type.spinnerTextEquals(subtypesInput.find { it.roundId == archerRoundInput.roundId && it.subTypeId == archerRoundInput.roundSubTypeId }!!.name!!)

        /*
         * Change some stuff
         */
        val calendar = Calendar.getInstance()
        // Use a different hour/minute to ensure it's not overwriting the time
        calendar.set(2040, 9, 30, 13, 15, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        R.id.text_create_round__date.click()
        onViewWithClassName(DatePicker::class.java).perform(setDatePickerValue(calendar))
        onView(withText("OK")).perform(ViewActions.click())
        R.id.text_create_round__time.click()
        onViewWithClassName(TimePicker::class.java).perform(setTimePickerValue(calendar))
        onView(withText("OK")).perform(ViewActions.click())

        R.id.text_create_round__time.textEquals("13:15")
        R.id.text_create_round__date.textEquals("30 Oct 40")

        val selectedRound = roundsInput[1]
        R.id.spinner_create_round__round.clickSpinnerItem(selectedRound.displayName)

        /*
         * Reset
         */
        R.id.button_create_round__reset.click()

        R.id.text_create_round__time.textEquals("17:12")
        R.id.text_create_round__date.textEquals("10 Jun 19")
        R.id.spinner_create_round__round.spinnerTextEquals(roundsInput.find { it.roundId == archerRoundInput.roundId }!!.displayName)
        R.id.spinner_create_round__round_sub_type.spinnerTextEquals(subtypesInput.find { it.roundId == archerRoundInput.roundId && it.subTypeId == archerRoundInput.roundSubTypeId }!!.name!!)

        /*
         * Change again
         */
        R.id.text_create_round__date.click()
        onViewWithClassName(DatePicker::class.java).perform(setDatePickerValue(calendar))
        onView(withText("OK")).perform(ViewActions.click())
        R.id.text_create_round__time.click()
        onViewWithClassName(TimePicker::class.java).perform(setTimePickerValue(calendar))
        onView(withText("OK")).perform(ViewActions.click())
        R.id.text_create_round__time.textEquals("13:15")
        R.id.text_create_round__date.textEquals("30 Oct 40")

        R.id.spinner_create_round__round.clickSpinnerItem(selectedRound.displayName)

        /*
         * Save
         */
        R.id.button_create_round__complete.click()
        assertEquals(
                ArcherRound(
                        archerRoundInput.archerRoundId,
                        calendar.time,
                        archerRoundInput.archerId,
                        archerRoundInput.countsTowardsHandicap,
                        roundId = selectedRound.roundId,
                        roundSubTypeId = 1
                ),
                currentArcherRounds.find { it.archerRoundId == archerRoundInput.archerRoundId }
        )
    }

    @Test
    fun testEditInfoToNoRound() {
        setup(archerRoundInput.archerRoundId)

        R.id.spinner_create_round__round.clickSpinnerItem("No Round")
        R.id.button_create_round__complete.click()
        assertEquals(
                ArcherRound(
                        archerRoundInput.archerRoundId,
                        archerRoundInput.dateShot,
                        archerRoundInput.archerId,
                        archerRoundInput.countsTowardsHandicap,
                        roundId = null,
                        roundSubTypeId = null
                ),
                currentArcherRounds.find { it.archerRoundId == archerRoundInput.archerRoundId }
        )
    }
}