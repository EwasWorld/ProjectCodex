package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.infoTable.InfoTableCell
import eywa.projectcodex.components.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.components.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.components.inputEnd.InputEndFragment
import eywa.projectcodex.components.scorePad.ScorePadFragment
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
import org.junit.Rule
import org.junit.Test

class ViewRoundsInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }
    }

    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)

    private val menuButtonScorePad = "Show score pad"
    private val menuButtonContinue = "Continue round"
    private val menuButtonDelete = "Delete round"

    // RoundId and handicap
    private val removedColumnIndexes = listOf(0, 6)

    private lateinit var tableViewAdapter: AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
    private lateinit var archerRounds: List<ArcherRoundWithRoundInfoAndName>
    private lateinit var round: Round
    private lateinit var roundSubType: RoundSubType
    private lateinit var roundArrowCount: RoundArrowCount
    private var arrows: MutableList<List<ArrowValue>> = mutableListOf()

    @Before
    fun beforeEach() {
        ScoresRoomDatabase.clearInstance(activity.activity)
        activity.activity.supportFragmentManager.beginTransaction()
    }

    private fun addDataToDatabase() {
        val db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext)
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
        Handler(Looper.getMainLooper()).post {
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
    }

    private fun goToViewRoundsAndPopulateAdapter() {
        R.id.button_main_menu__view_rounds.click()
        tableViewAdapter = activity.activity.findViewById<TableView>(R.id.table_view_view_rounds).adapter!!
                as AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>
    }

    @After
    fun afterEach() {
        ScoresRoomDatabase.clearInstance(activity.activity)
    }

    @Test
    fun testTableValues() {
        addDataToDatabase()
        goToViewRoundsAndPopulateAdapter()

        val expected =
                calculateViewRoundsTableData(
                        archerRounds,
                        arrows.flatten(),
                        GoldsType.TENS,
                        activity.activity.resources
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
        goToViewRoundsAndPopulateAdapter()
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        onView(withText("Main Menu")).check(matches(isDisplayed()))
    }

    @Test
    fun testOpenScorePad() {
        // Generate rounds until there is a unique score
        var uniqueScore: Int? = null
        while (uniqueScore == null) {
            addDataToDatabase()
            val scores = arrows.map { roundArrows -> roundArrows.sumBy { arrow -> arrow.score } }

            for ((i, score) in scores.withIndex()) {
                if (scores.lastIndexOf(score) == i) {
                    uniqueScore = score
                    break
                }
            }
        }
        goToViewRoundsAndPopulateAdapter()

        // Click on that unique score
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())
        onView(withText(uniqueScore.toString())).perform(longClick())
        onView(withText(menuButtonScorePad)).perform(click())
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(ScorePadFragment::class.java.name))
        onView(withText("Score Pad")).check(matches(isDisplayed()))
        tableViewAdapter = activity.activity.findViewById<TableView>(R.id.table_view_score_pad).adapter!!
                as AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>

        // Check the last running total is the unique score
        val maxColIndex = tableViewAdapter.getCellRowItems(0)?.size!! - 1
        // -2 to ignore the total row
        val maxRowIndex = tableViewAdapter.getCellColumnItems(0).size - 2
        assertEquals(
                uniqueScore,
                tableViewAdapter.getCellItem(maxColIndex, maxRowIndex)?.content!! as Int
        )
    }

    @Test
    fun testDeleteRow() {
        addDataToDatabase()
        goToViewRoundsAndPopulateAdapter()
        var expected: List<List<InfoTableCell>> =
                calculateViewRoundsTableData(
                        archerRounds,
                        arrows.flatten(),
                        GoldsType.TENS,
                        activity.activity.resources
                )

        assertEquals(expected.size, tableViewAdapter.getCellColumnItems(2).size)
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())

        val deleteIndex = 1
        onView(findRoundArrows(deleteIndex).sumBy { it.score }.toString()).perform(longClick())
        onView(withText(menuButtonDelete)).perform(click())
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
    fun testContinueRound() {
        addDataToDatabase()
        goToViewRoundsAndPopulateAdapter()
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())

        val continueRound = findRoundArrows(1)
        onView(continueRound.sumBy { it.score }.toString()).perform(longClick())
        onView(withText(menuButtonContinue)).perform(click())
        ConditionWatcher.waitForCondition(activity.waitForFragmentInstruction(InputEndFragment::class.java.name))

        R.id.text_scores_indicator__table_score_1.textEquals(continueRound.sumBy { it.score }.toString())
        R.id.text_scores_indicator__table_arrow_count_1.textEquals(continueRound.size.toString())
    }

    @Test
    fun testContinueCompletedRound() {
        val round = Round(1, "test", "test", true, true, listOf())
        val roundArrowCount = RoundArrowCount(1, 1, 1.0, 6)
        val roundDistance = RoundDistance(1, 1, 1, 10)
        val archerRound = ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1)
        val arrowValues = TestData.ARROWS.take(6).mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) }

        val db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext)
        Handler(Looper.getMainLooper()).post {
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

        goToViewRoundsAndPopulateAdapter()
        onView(withId((R.id.table_view_view_rounds))).perform(swipeLeft())
        onView(arrowValues.sumBy { it.score }.toString()).perform(longClick())
        onView(withText(menuButtonContinue)).check(doesNotExist())
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