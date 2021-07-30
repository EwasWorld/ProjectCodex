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
import eywa.projectcodex.common.CommonStrings
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.clickAlertDialogOk
import eywa.projectcodex.common.withIndex
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
     * Columns returned by [calculateViewRoundsTableData] that are not actually displayed in the view rounds table
     * RoundId
     */
    private val removedColumnIndexes = listOf(0)

    private lateinit var scenario: FragmentScenario<ViewRoundsFragment>
    private lateinit var navController: TestNavHostController
    private lateinit var db: ScoresRoomDatabase
    private lateinit var resources: Resources
    private lateinit var tableViewAdapter: AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
    private var archerRounds: List<ArcherRoundWithRoundInfoAndName> = listOf()
    private var round: Round? = null
    private var roundSubType: RoundSubType? = null
    private var roundArrowCount: RoundArrowCount? = null
    private var roundDistance: RoundDistance? = null
    private var arrows: List<List<ArrowValue>> = listOf()

    @Before
    fun beforeEach() {
        archerRounds = listOf()
        round = null
        roundSubType = null
        roundArrowCount = null
        roundDistance = null
        arrows = listOf()

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

    private fun generateBasicDataAndAddToDb() {
        round = TestData.generateRounds(1)[0]
        roundSubType = TestData.generateSubTypes(1)[0]
        roundArrowCount = TestData.generateArrowCounts(1)[0]
        roundDistance = RoundDistance(1, 1, 1, 70)
        archerRounds = TestData.generateArcherRounds(5, 1, listOf(1, 1, null), listOf(1, null, null))
                .mapIndexed { i, archerRound ->
                    val roundInfo = if (i % 3 == 0 || i % 3 == 1) round!! else null
                    val roundSubTypeName = if (i % 3 == 0) roundSubType!!.name else null
                    ArcherRoundWithRoundInfoAndName(archerRound, roundInfo, roundSubTypeName)
                }
        val mutableArrows = mutableListOf<List<ArrowValue>>()
        for (round in archerRounds) {
            mutableArrows.add(TestData.generateArrowValues(30, round.archerRound.archerRoundId))
        }
        arrows = mutableArrows

        addToDbAndPopulateAdapter()
    }

    private fun addToDbAndPopulateAdapter() {
        scenario.onFragment {
            runBlocking {
                for (archerRound in archerRounds) {
                    db.archerRoundDao().insert(archerRound.archerRound)
                }
                for (arrow in arrows.flatten()) {
                    db.arrowValueDao().insert(arrow)
                }
                round?.let { db.roundDao().insert(it) }
                roundSubType?.let { db.roundSubTypeDao().insert(it) }
                roundArrowCount?.let { db.roundArrowCountDao().insert(it) }
                roundDistance?.let { db.roundDistanceDao().insert(it) }
            }
        }
        populateAdapter()
    }

    private fun populateAdapter() {
        scenario.onFragment { fragment ->
            tableViewAdapter = fragment.requireActivity().findViewById<TableView>(R.id.table_view_view_rounds).adapter!!
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
        generateBasicDataAndAddToDb()

        val expected = calculateViewRoundsTableData(
                archerRounds,
                arrows.flatten(),
                GoldsType.TENS,
                listOf(roundArrowCount!!),
                listOf(roundDistance!!)
        )
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
            generateBasicDataAndAddToDb()
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
        generateBasicDataAndAddToDb()
        var expected: List<List<InfoTableCell>> = calculateViewRoundsTableData(
                archerRounds,
                arrows.flatten(),
                GoldsType.TENS,
                listOf(roundArrowCount!!),
                listOf(roundDistance!!)
        )

        assertEquals(expected.size, tableViewAdapter.getCellColumnItems(2).size)
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())

        val deleteIndex = 1
        onView(withText(findRoundArrows(deleteIndex).sumOf { it.score }.toString())).perform(longClick())
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
        round = Round(1, "test", "test", true, true, listOf())
        roundArrowCount = RoundArrowCount(1, 1, 1.0, 6)
        roundDistance = RoundDistance(1, 1, 1, 10)
        archerRounds = listOf(
                ArcherRoundWithRoundInfoAndName(
                        ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1), round, null
                )
        )
        arrows = listOf(TestData.ARROWS.take(6).mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })
        addToDbAndPopulateAdapter()

        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())
        onView(withText(arrows.flatten().sumOf { it.score }.toString())).perform(longClick())
        onView(withText(CommonStrings.Menus.viewRoundsContinue)).check(doesNotExist())
    }

    @Test
    fun testConvertRound() {
        archerRounds = listOf(
                ArcherRoundWithRoundInfoAndName(
                        ArcherRound(1, TestData.generateDate(), 1, false)
                ),
                ArcherRoundWithRoundInfoAndName(
                        ArcherRound(2, TestData.generateDate(), 1, false)
                )
        )
        arrows = listOf(
                TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) },
                TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(2, i + 1) }
        )
        addToDbAndPopulateAdapter()

        // Convert first score
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())
        onView(withIndex(withText(TestData.ARROWS.sumOf { it.score }.toString()), 0)).perform(longClick())
        onView(withText(CommonStrings.Menus.viewRoundsConvert)).perform(click())
        onView(withText(CommonStrings.Menus.viewRoundsConvertToFiveZone)).perform(click())
        clickAlertDialogOk(CommonStrings.Dialogs.viewRoundsConvertTitle)


        // Convert second score (sum should be unique)
        onView(withText(TestData.ARROWS.sumOf { it.score }.toString())).perform(longClick())
        onView(withText(CommonStrings.Menus.viewRoundsConvert)).perform(click())
        onView(withText(CommonStrings.Menus.viewRoundsConvertToTens)).perform(click())
        clickAlertDialogOk(CommonStrings.Dialogs.viewRoundsConvertTitle)

        // TODO Better wait for (possibly await on a latch where it counts down when the finished toast appears)
        CustomConditionWaiter.waitFor(3000)

        populateAdapter()
        val expectedData = calculateViewRoundsTableData(
                archerRounds,
                listOf(
                        TestData.ARROWS[0], TestData.ARROWS[1], TestData.ARROWS[1], TestData.ARROWS[3],
                        TestData.ARROWS[3], TestData.ARROWS[5], TestData.ARROWS[5], TestData.ARROWS[7],
                        TestData.ARROWS[7], TestData.ARROWS[9], TestData.ARROWS[9], TestData.ARROWS[9]
                ).mapIndexed { i, arrow -> arrow.toArrowValue(1, i) }
                        .plus(
                                TestData.ARROWS.dropLast(1).plus(TestData.ARROWS[10])
                                .mapIndexed { i, arrow -> arrow.toArrowValue(2, i) }),
                GoldsType.TENS
        )
        for (expected in expectedData.withIndex()) {
            assertEquals(
                    // Ignore hidden columns
                    expected.value.filterIndexed { i, _ -> !removedColumnIndexes.contains(i) },
                    tableViewAdapter.getCellRowItems(expected.index)
            )
        }
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