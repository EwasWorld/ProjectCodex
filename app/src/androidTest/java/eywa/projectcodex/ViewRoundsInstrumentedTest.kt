package eywa.projectcodex

import android.content.res.Resources
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.infoTable.InfoTableCell
import eywa.projectcodex.components.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.components.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.components.viewRounds.ViewRoundsFragment
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ViewRoundsInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = CommonStrings.testDatabaseName
        }
    }

    /**
     * RoundId and handicap
     */
    private val removedColumnIndexes = listOf(0, 6)

    private lateinit var scenario: FragmentScenario<ViewRoundsFragment>
    private lateinit var navController: TestNavHostController
    private lateinit var db: ScoresRoomDatabase
    private lateinit var resources: Resources
    private lateinit var tableViewAdapter: AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
    private lateinit var archerRounds: List<ArcherRoundWithRoundInfoAndName>
    private lateinit var round: Round
    private lateinit var roundSubType: RoundSubType
    private lateinit var roundArrowCount: RoundArrowCount
    private var arrows: MutableList<List<ArrowValue>> = mutableListOf()

    @Before
    fun beforeEach() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Start initialised so we can add to the database before the onCreate methods are called
        scenario = launchFragmentInContainer(initialState = Lifecycle.State.INITIALIZED)
        scenario.onFragment {
            ScoresRoomDatabase.clearInstance(it.requireContext())
            db = ScoresRoomDatabase.getDatabase(it.requireContext())

            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.viewRoundsFragment)

        }

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            resources = it.resources
        }
    }

    private fun addDataToDatabase() {
        round = TestData.generateRounds(1)[0]
        roundSubType = TestData.generateSubTypes(1)[0]
        roundArrowCount = TestData.generateArrowCounts(1)[0]
        archerRounds = TestData.generateArcherRounds(5, 1, listOf(1, 1, null), listOf(1, null, null))
                .mapIndexed { i, archerRound ->
                    val roundInfo = if (i % 3 == 0 || i % 3 == 1) round else null
                    val roundSubTypeName = if (i % 3 == 0) roundSubType.name else null
                    ArcherRoundWithRoundInfoAndName(archerRound, roundInfo, roundSubTypeName)
                }
        for (round in archerRounds) {
            arrows.add(TestData.generateArrowValues(30, round.archerRound.archerRoundId))
        }

        scenario.onFragment {
            for (archerRound in archerRounds) {
                runBlocking {
                    db.archerRoundDao().insert(archerRound.archerRound)
                }
            }
            for (arrow in arrows.flatten()) {
                runBlocking {
                    db.arrowValueDao().insert(arrow)
                }
            }
            runBlocking {
                db.roundDao().insert(round)
                db.roundSubTypeDao().insert(roundSubType)
                db.roundArrowCountDao().insert(roundArrowCount)
            }
        }

        populateAdapter()
    }

    private fun populateAdapter() {
        scenario.onFragment {
            tableViewAdapter = it.requireActivity().findViewById<TableView>(R.id.table_view_view_rounds).adapter!!
                    as AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
        }
    }

    @After
    fun afterEach() {
        scenario.onFragment {
            ScoresRoomDatabase.clearInstance(it.requireContext())
        }
    }

    @Test
    fun testTableValues() {
        addDataToDatabase()

        val expected =
                calculateViewRoundsTableData(archerRounds, arrows.flatten(), GoldsType.TENS, resources)
        for (i in expected.indices) {
            assertEquals(
                    expected[i].filterIndexed { j, _ -> !removedColumnIndexes.contains(j) },
                    tableViewAdapter.getCellRowItems(i) as List<InfoTableCell>
            )
        }
        var col = 0
        val expectedColumns =
                listOf("ID", "Date", "Round", "H", "S", "G", "HC").map { InfoTableCell(it, "col" + col++) }
                        .filterIndexed { i, _ -> !removedColumnIndexes.contains(i) }
        for (i in expectedColumns.indices) {
            assertEquals(expectedColumns[i], tableViewAdapter.getColumnHeaderItem(i))
        }
        val expectedRows = generateNumberedRowHeaders(expected.size)
        for (i in expectedRows.indices) {
            assertEquals(expectedRows[i], tableViewAdapter.getRowHeaderItem(i))
        }
    }

    @Test
    fun testEmptyTable() {
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        assertEquals(R.id.mainMenuFragment, navController.currentDestination?.id)
    }


    /**
     * Generate rounds until there is a unique score then open the long press menu on that
     */
    private fun createRoundWithUniqueScoreAndLongPress(): Int {
        data class RoundScore(val archerRoundId: Int, val score: Int)

        var uniqueScore: RoundScore? = null
        while (uniqueScore == null) {
            addDataToDatabase()
            val scores = arrows.map { roundArrows ->
                RoundScore(roundArrows[0].archerRoundId, roundArrows.sumOf { arrow -> arrow.score })
            }
            for ((i, score) in scores.withIndex()) {
                if (scores.indexOfLast { it.score == score.score } == i) {
                    uniqueScore = score
                    break
                }
            }
        }

        // Open the score pad for that unique score
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())
        onView(withText(uniqueScore.score.toString())).perform(longClick())

        return uniqueScore.archerRoundId
    }

    @Test
    fun testOpenScorePad() {
        val uniqueScoreRoundId = createRoundWithUniqueScoreAndLongPress()
        onView(withText(CommonStrings.Menus.viewRoundsShowScorePad)).perform(click())

        assertEquals(R.id.scorePadFragment, navController.currentDestination?.id)
        assertEquals(uniqueScoreRoundId, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    @Test
    fun testContinueRound() {
        val uniqueScoreRoundId = createRoundWithUniqueScoreAndLongPress()
        onView(withText(CommonStrings.Menus.viewRoundsContinue)).perform(click())

        assertEquals(R.id.inputEndFragment, navController.currentDestination?.id)
        assertEquals(uniqueScoreRoundId, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    @Test
    fun testDeleteRow() {
        addDataToDatabase()
        var expected: List<List<InfoTableCell>> =
                calculateViewRoundsTableData(archerRounds, arrows.flatten(), GoldsType.TENS, resources)

        assertEquals(expected.size, tableViewAdapter.getCellColumnItems(2).size)
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())

        val deleteIndex = 1
        onViewWithClassName(findRoundArrows(deleteIndex).sumOf { it.score }.toString()).perform(longClick())
        onView(withText(CommonStrings.Menus.viewRoundsDelete)).perform(click())
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "wait for row to be removed"
            }

            override fun checkCondition(): Boolean {
                return tableViewAdapter.getCellColumnItems(2).size == expected.size - 1
            }
        })
        assertEquals(expected.size - 1, tableViewAdapter.getCellColumnItems(2).size)

        // Only check contents as the ids will have changed when the table recalculated itself
        expected = expected.minusElement(expected[deleteIndex])
        for (i in expected.indices) {
            assertEquals(
                    expected[i].filterIndexed { j, _ -> !removedColumnIndexes.contains(j) }.map { it.content },
                    (tableViewAdapter.getCellRowItems(i) as List<InfoTableCell>).map { it.content }
            )
        }
    }

    @Test
    fun testContinueCompletedRound() {
        val round = Round(1, "test", "test", true, true, listOf())
        val roundArrowCount = RoundArrowCount(1, 1, 1.0, 6)
        val roundDistance = RoundDistance(1, 1, 1, 10)
        val archerRound = ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1)
        val arrowValues = TestData.ARROWS.take(6).mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) }

        scenario.onFragment {
            runBlocking {
                db.roundDao().insert(round)
                db.roundArrowCountDao().insert(roundArrowCount)
                db.roundDistanceDao().insert(roundDistance)
                db.archerRoundDao().insert(archerRound)
            }
            for (arrow in arrowValues) {
                runBlocking {
                    db.arrowValueDao().insert(arrow)
                }
            }
        }
        populateAdapter()

        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())
        onViewWithClassName(arrowValues.sumOf { it.score }.toString()).perform(longClick())
        onView(withText(CommonStrings.Menus.viewRoundsContinue)).check(doesNotExist())
    }

    /**
     * Get the arrow list of the Nth item shown in the table
     */
    private fun findRoundArrows(index: Int): List<ArrowValue> {
        val archerRoundsByDate = archerRounds.map { it.archerRound }.sortedByDescending { it.dateShot }
        val foundRound = arrows.find { it[0].archerRoundId == archerRoundsByDate[index].archerRoundId }
        return if (foundRound == null) {
            fail("Round not found")
            listOf()
        }
        else {
            foundRound
        }
    }
}