package eywa.projectcodex

import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Test

/**
 * Tests navigation between fragments
 */
class NavigationInstrumentedTests {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
            SharedPrefs.sharedPreferencesCustomName = testSharedPrefsName
        }
    }

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private lateinit var navController: NavController
    private val arrowsInRound = 36
    private val arrowScore = 5

    @After
    fun afterEach() {
        scenario.onActivity {
            ScoresRoomDatabase.clearInstance(it.applicationContext)
        }
    }

    fun setup(arrowCount: Int) {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            ScoresRoomDatabase.clearInstance(it.applicationContext)
            db = ScoresRoomDatabase.getDatabase(it.applicationContext)

            navController = it.navHostFragment.navController

            runBlocking {
                List(arrowCount) { index -> ArrowValue(1, index + 1, arrowScore, false) }.forEach { arrow ->
                    db.arrowValueDao().insert(arrow)
                }
                db.archerRoundDao().insert(
                        ArcherRound(1, TestData.generateDate(), 1, true, roundId = 1)
                )
                db.roundDao().insert(Round(1, "test", "test", false, false, listOf()))
                db.roundArrowCountDao().insert(RoundArrowCount(1, 1, 10.0, arrowsInRound))
                db.roundDistanceDao().insert(RoundDistance(1, 1, 1, 20))
            }
        }
    }

    private fun testArcherRoundScoreBottomNavAux(roundComplete: Boolean) {
        fun moveTo(destinationId: Int, expectedToWork: Boolean) {
            val expected = if (expectedToWork) destinationId else navController.currentDestination!!.id
            destinationId.click()
            Assert.assertEquals(expected, navController.currentDestination?.id)
        }

        val arrowCount = if (roundComplete) arrowsInRound else arrowsInRound - 6
        setup(arrowCount)
        ConditionWatcher.waitForCondition(waitForOpenScorePadFromMainMenu(arrowScore * arrowCount))
        ConditionWatcher.waitForCondition(scenario.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        var tableView: TableView? = null
        scenario.onActivity {
            tableView = it.findViewById(R.id.table_view_score_pad)
        }
        ConditionWatcher.waitForCondition(tableView!!.waitForRowToAppear(0))

        // Score Pad -> Input End
        moveTo(R.id.inputEndFragment, !roundComplete)

        if (!roundComplete) {
            // Input End -> Score Pad
            moveTo(R.id.scorePadFragment, true)
        }

        // Score Pad -> Stats
        moveTo(R.id.archerRoundStatsFragment, true)

        // Stats -> Input End
        moveTo(R.id.inputEndFragment, !roundComplete)

        if (!roundComplete) {
            // Input End -> Stats
            moveTo(R.id.archerRoundStatsFragment, true)
        }

        // Stats -> Score Pad
        moveTo(R.id.scorePadFragment, true)
    }


    /**
     * Testing the transitions between Input End, Score Pad, and Stats
     */
    @Test
    fun testArcherRoundScoreBottomNavigation() {
        testArcherRoundScoreBottomNavAux(false)
        afterEach()
        testArcherRoundScoreBottomNavAux(true)
    }
}