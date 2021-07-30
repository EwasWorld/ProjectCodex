package eywa.projectcodex

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.CommonStrings
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.labelledTextViewTextEquals
import eywa.projectcodex.common.visibilityIs
import eywa.projectcodex.components.archerRoundScore.archerRoundStats.ArcherRoundStatsFragment
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.math.max

@RunWith(AndroidJUnit4::class)
class ArcherRoundStatsInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = CommonStrings.testDatabaseName
        }
    }

    private lateinit var scenario: FragmentScenario<ArcherRoundStatsFragment>
    private lateinit var navController: TestNavHostController
    private lateinit var db: ScoresRoomDatabase

    private lateinit var arrows: List<ArrowValue>
    private val arrowsPerArrowCount = 12
    private val roundsInput = listOf(
            Round(1, "round1", "Round1", true, false, listOf()),
            Round(2, "round2", "Round2", true, false, listOf())
    )
    private val arrowCountsInput = listOf(
            RoundArrowCount(1, 1, 122.0, arrowsPerArrowCount),
            RoundArrowCount(1, 2, 122.0, arrowsPerArrowCount),
            RoundArrowCount(2, 1, 122.0, arrowsPerArrowCount),
            RoundArrowCount(2, 2, 122.0, arrowsPerArrowCount)
    )
    private val distancesInput = listOf(
            RoundDistance(1, 1, 1, 60),
            RoundDistance(1, 2, 1, 50),
            RoundDistance(2, 1, 1, 60),
            RoundDistance(2, 2, 1, 50),
            RoundDistance(2, 1, 2, 30),
            RoundDistance(2, 2, 2, 20)
    )
    private val subTypesInput = listOf(
            RoundSubType(2, 1, "Sub Type 1"),
            RoundSubType(2, 2, "Sub Type 2")
    )
    private val archerRounds = listOf(
            ArcherRound(
                    1,
                    Date.from(Calendar.Builder().setDate(2014, 6, 17).setTimeOfDay(15, 21, 37).build().toInstant()),
                    1,
                    true
            ),
            ArcherRound(2, TestData.generateDate(), 1, true, roundId = 1),
            ArcherRound(3, TestData.generateDate(), 1, true, roundId = 2, roundSubTypeId = 1)
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
            navController.setCurrentDestination(R.id.archerRoundStatsFragment, args)

            /*
             * Fill default rounds
             */
            for (i in 0 until max(arrows.size, distancesInput.size)) {
                runBlocking {
                    if (i < arrows.size) {
                        db.arrowValueDao().insert(arrows[i])
                    }
                    if (i < roundsInput.size) {
                        db.roundDao().insert(roundsInput[i])
                    }
                    if (i < arrowCountsInput.size) {
                        db.roundArrowCountDao().insert(arrowCountsInput[i])
                    }
                    if (i < distancesInput.size) {
                        db.roundDistanceDao().insert(distancesInput[i])
                    }
                    if (i < subTypesInput.size) {
                        db.roundSubTypeDao().insert(subTypesInput[i])
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

        CustomConditionWaiter.waitFor(500)
    }

    @After
    fun teardown() {
        scenario.onFragment {
            ScoresRoomDatabase.clearInstance(it.requireContext())
        }
    }

    @Test
    fun testAllStatsNoRound() {
        val archerRoundId = ArcherRoundTypes.NO_ROUND.archerRoundId
        check(archerRounds.find { it.archerRoundId == archerRoundId } != null) { "Invalid archer round ID" }

        var arrowNumber = 1
        arrows = listOf(
                List(6) { TestData.ARROWS[10].toArrowValue(archerRoundId, arrowNumber++) },
                List(38) { TestData.ARROWS[5].toArrowValue(archerRoundId, arrowNumber++) },
                List(4) { TestData.ARROWS[0].toArrowValue(archerRoundId, arrowNumber++) }
        ).flatten()
        setup(archerRoundId)

        R.id.text_archer_round_stats__date.labelledTextViewTextEquals("17 Jul 14 15:21")
        R.id.text_archer_round_stats__round.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.text_archer_round_stats__hits.labelledTextViewTextEquals("44 (of 48)")
        R.id.text_archer_round_stats__score.labelledTextViewTextEquals((38 * 5 + 6 * 10).toString())
        R.id.text_archer_round_stats__golds.labelledTextViewTextEquals("6")
        R.id.text_archer_round_stats__remaining_arrows.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.text_archer_round_stats__handicap.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.text_archer_round_stats__predicted_score.visibilityIs(ViewMatchers.Visibility.GONE)
    }

    @Test
    fun testHasRound() {
        val archerRoundId = ArcherRoundTypes.ROUND.archerRoundId
        val archerRound = archerRounds.find { it.archerRoundId == archerRoundId }!!
        val round = roundsInput.find { it.roundId == archerRound.roundId }!!

        var arrowNumber = 1
        arrows = List(arrowsPerArrowCount) { TestData.ARROWS[8].toArrowValue(archerRoundId, arrowNumber++) }
        setup(archerRoundId)

        R.id.text_archer_round_stats__round.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_archer_round_stats__remaining_arrows.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_archer_round_stats__handicap.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_archer_round_stats__predicted_score.visibilityIs(ViewMatchers.Visibility.VISIBLE)

        R.id.text_archer_round_stats__round.labelledTextViewTextEquals(round.displayName)
        R.id.text_archer_round_stats__remaining_arrows.labelledTextViewTextEquals(arrowsPerArrowCount.toString())
        // Checked these values in the handicap tables (1998), score for two dozen
        R.id.text_archer_round_stats__handicap.labelledTextViewTextEquals("32")
        // divide by 2 because only one dozen was shot
        R.id.text_archer_round_stats__predicted_score.labelledTextViewTextEquals(((192 + 201) / 2).toString())
    }

    @Test
    fun testRoundWithSubTypeEmptyScore() {
        val archerRoundId = ArcherRoundTypes.SUBTYPE.archerRoundId
        var arrowNumber = 1
        arrows = List(arrowsPerArrowCount) { TestData.ARROWS[8].toArrowValue(archerRoundId, arrowNumber++) }
        setup(archerRoundId)

        R.id.text_archer_round_stats__round.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_archer_round_stats__remaining_arrows.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_archer_round_stats__handicap.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_archer_round_stats__predicted_score.visibilityIs(ViewMatchers.Visibility.VISIBLE)

        R.id.text_archer_round_stats__round.labelledTextViewTextEquals(subTypesInput[0].name!!)
        R.id.text_archer_round_stats__remaining_arrows.labelledTextViewTextEquals(arrowsPerArrowCount.toString())
        // Checked these values in the handicap tables (1998), score for two dozen
        R.id.text_archer_round_stats__handicap.labelledTextViewTextEquals("32")
        // divide by 2 because only one dozen was shot
        R.id.text_archer_round_stats__predicted_score.labelledTextViewTextEquals(((192 + 201) / 2).toString())
    }

    private enum class ArcherRoundTypes(val archerRoundId: Int) {
        NO_ROUND(1), ROUND(2), SUBTYPE(3)
    }
}