package eywa.projectcodex.instrumentedTests

import android.content.res.Resources
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.R
import eywa.projectcodex.TestData
import eywa.projectcodex.common.CommonStrings
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.clickAlertDialog
import eywa.projectcodex.common.withIndex
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.components.viewScores.data.ViewScoreData
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
import org.junit.Before
import org.junit.Test

class ViewScoresInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = CommonStrings.testDatabaseName
        }
    }

    private lateinit var scenario: FragmentScenario<ViewScoresFragment>
    private lateinit var navController: TestNavHostController
    private lateinit var db: ScoresRoomDatabase
    private lateinit var resources: Resources
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var archerRounds: List<ArcherRoundWithRoundInfoAndName> = listOf()
    private var rounds = listOf<Round>()
    private var roundSubTypes = listOf<RoundSubType>()
    private var roundArrowCounts = listOf<RoundArrowCount>()
    private var roundDistances = listOf<RoundDistance>()
    private var arrows: List<List<ArrowValue>> = listOf()

    @Before
    fun beforeEach() {
        archerRounds = listOf()
        rounds = listOf()
        roundSubTypes = listOf()
        roundArrowCounts = listOf()
        roundDistances = listOf()
        arrows = listOf()

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Start initialised so we can add to the database before the onCreate methods are called
        scenario = launchFragmentInContainer(initialState = Lifecycle.State.INITIALIZED)
        scenario.onFragment {
            ScoresRoomDatabase.clearInstance(it.requireContext())
            db = ScoresRoomDatabase.getDatabase(it.requireContext())

            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.viewScoresFragment)

        }

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            resources = it.resources
        }
    }

    @After
    fun afterEach() {
        scenario.onFragment {
            ScoresRoomDatabase.clearInstance(it.requireContext())
        }
        ViewScoreData.clearInstance()
    }

    private fun generateBasicDataAndAddToDb() {
        rounds = listOf(
                Round(1, "metricround", "Metric Round", true, true, listOf()),
                Round(2, "imperialround", "Imperial Round", true, true, listOf()),
        )
        roundSubTypes = listOf(
                RoundSubType(2, 1, "Sub Type 1"),
                RoundSubType(2, 2, "Sub Type 2")
        )
        roundArrowCounts = listOf(
                RoundArrowCount(1, 1, 122.0, 48),
                RoundArrowCount(2, 1, 122.0, 36)
        )
        roundDistances = listOf(
                RoundDistance(1, 1, 1, 70),
                RoundDistance(2, 1, 1, 60),
                RoundDistance(2, 1, 2, 50)
        )
        archerRounds = listOf(
                ArcherRound(1, TestData.generateDate(), 1, false),
                ArcherRound(2, TestData.generateDate(), 1, false, roundId = 1),
                ArcherRound(3, TestData.generateDate(), 1, false, roundId = 2),
                ArcherRound(4, TestData.generateDate(), 1, false, roundId = 2, roundSubTypeId = 2)
        ).map { archerRound ->
            ArcherRoundWithRoundInfoAndName(
                    archerRound,
                    rounds.find { it.roundId == archerRound.roundId },
                    roundSubTypes.find {
                        it.roundId == archerRound.roundId && it.subTypeId == archerRound.roundSubTypeId
                    }?.name
            )
        }
        arrows = List(archerRounds.size) { archerRoundId ->
            List(36) { arrowNumber -> TestData.ARROWS[archerRoundId].toArrowValue(archerRoundId, arrowNumber) }
        }

        addToDbAndRetrieveAdapter()
    }

    private fun addToDbAndRetrieveAdapter() {
        scenario.onFragment {
            runBlocking {
                archerRounds.forEach {
                    db.archerRoundDao().insert(it.archerRound)
                }
                arrows.flatten().forEach {
                    db.arrowValueDao().insert(it)
                }
                rounds.forEach {
                    db.roundDao().insert(it)
                }
                roundSubTypes.forEach {
                    db.roundSubTypeDao().insert(it)
                }
                roundArrowCounts.forEach {
                    db.roundArrowCountDao().insert(it)
                }
                roundDistances.forEach {
                    db.roundDistanceDao().insert(it)
                }
            }
        }
        retrieveUpdatedAdapter()
    }

    private fun retrieveUpdatedAdapter() {
        scenario.onFragment { fragment ->
            layoutManager =
                    fragment.requireActivity().findViewById<RecyclerView>(R.id.recycler_view_scores).layoutManager!!
        }
    }

    private fun generateExpectedData(): ViewScoreData {
        val expectedData = ViewScoreData.createInstance()
        expectedData.updateArcherRounds(archerRounds)
        expectedData.updateArrows(arrows.flatten())
        expectedData.updateArrowCounts(roundArrowCounts)
        expectedData.updateDistances(roundDistances)

        // Quick check
        assertEquals(archerRounds.size, expectedData.getData().size)
        return expectedData
    }

    @Test
    fun testTableValues() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()

        for (indexedItem in expectedData.getData().withIndex()) {
            scenario.onFragment {
                layoutManager.scrollToPosition(indexedItem.index)
            }
            onView(withIndex(withId(R.id.text_vs_round_item__hsg), indexedItem.index))
                    .check(matches(withText(indexedItem.value.hitsScoreGolds)))
            onView(withIndex(withId(R.id.text_vs_round_item__handicap), indexedItem.index))
                    .check(matches(withText(indexedItem.value.handicap?.toString() ?: "-")))
        }
    }

    @Test
    fun testEmptyTable() {
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        assertEquals(R.id.mainMenuFragment, navController.currentDestination?.id)
    }

    @Test
    fun testOpenScorePad() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()

        val clickedItem = expectedData.getData()[0]
        onView(withIndex(withText(clickedItem.hitsScoreGolds), 0)).perform(longClick())
        CustomConditionWaiter.waitFor(200)
        onView(withText(CommonStrings.Menus.viewRoundsShowScorePad)).perform(click())

        assertEquals(R.id.scorePadFragment, navController.currentDestination?.id)
        assertEquals(clickedItem.id, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    @Test
    fun testContinueRound() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()

        val clickedItem = expectedData.getData().find { it.round?.roundId == 1 }!!
        onView(withIndex(withText(clickedItem.hitsScoreGolds), 0)).perform(longClick())
        CustomConditionWaiter.waitFor(200)
        onView(withText(CommonStrings.Menus.viewRoundsContinue)).perform(click())

        assertEquals(R.id.inputEndFragment, navController.currentDestination?.id)
        assertEquals(clickedItem.id, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    @Test
    fun testDeleteRow() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()

        val deleteItem = expectedData.getData()[1]
        onView(withIndex(withText(deleteItem.hitsScoreGolds), 0)).perform(longClick())
        CustomConditionWaiter.waitFor(200)
        onView(withText(CommonStrings.Menus.viewRoundsDelete)).perform(click())

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "wait for row to be removed"
            }

            override fun checkCondition(): Boolean {
                return expectedData.getData().size - 1 == layoutManager.childCount
            }
        })

        for (indexedItem in expectedData.getData().minus(deleteItem).withIndex()) {
            scenario.onFragment {
                layoutManager.scrollToPosition(indexedItem.index)
            }
            onView(withIndex(withId(R.id.text_vs_round_item__hsg), indexedItem.index))
                    .check(matches(withText(indexedItem.value.hitsScoreGolds)))
            onView(withIndex(withId(R.id.text_vs_round_item__handicap), indexedItem.index))
                    .check(matches(withText(indexedItem.value.handicap?.toString() ?: "-")))
        }
    }

    @Test
    fun testContinueCompletedRound() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()

        val clickedItem = expectedData.getData().find { it.round?.roundId == 2 }!!
        onView(withIndex(withText(clickedItem.hitsScoreGolds), 0)).perform(longClick())
        CustomConditionWaiter.waitForMenuToAppear(CommonStrings.Menus.viewRoundsConvert)
        onView(withText(CommonStrings.Menus.viewRoundsContinue)).check(doesNotExist())
    }

    @Test
    fun testConvertRound() {
        archerRounds = listOf(
                ArcherRoundWithRoundInfoAndName(
                        ArcherRound(1, TestData.generateDate(2020), 1, false)
                ),
                ArcherRoundWithRoundInfoAndName(
                        ArcherRound(2, TestData.generateDate(2019), 1, false)
                )
        )
        arrows = listOf(
                TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) },
                TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(2, i + 1) }
        )
        addToDbAndRetrieveAdapter()
        val expectedData = generateExpectedData()

        // Convert first score
        val expectedHsg = expectedData.getData().find { it.id == 1 }!!.hitsScoreGolds!!
        CustomConditionWaiter.waitForTextToAppear(expectedHsg)
        onView(withIndex(withText(expectedHsg), 0)).perform(longClick())
        CustomConditionWaiter.waitForMenuToAppear(CommonStrings.Menus.viewRoundsConvert)
        onView(withText(CommonStrings.Menus.viewRoundsConvert)).perform(click())
        onView(withText(CommonStrings.Menus.viewRoundsConvertToFiveZone)).perform(click())
        CustomConditionWaiter.waitFor(500)
        clickAlertDialog(CommonStrings.Dialogs.viewRoundsConvertTitle)
        CustomConditionWaiter.waitForToast("Finished conversion")

        // Convert second score (sum should be unique)
        onView(withIndex(withText(expectedData.getData().find { it.id == 2 }!!.hitsScoreGolds), 0)).perform(longClick())
        CustomConditionWaiter.waitForMenuToAppear(CommonStrings.Menus.viewRoundsConvert)
        onView(withText(CommonStrings.Menus.viewRoundsConvert)).perform(click())
        onView(withText(CommonStrings.Menus.viewRoundsConvertToTens)).perform(click())
        CustomConditionWaiter.waitFor(500)
        clickAlertDialog(CommonStrings.Dialogs.viewRoundsConvertTitle)
        CustomConditionWaiter.waitForToast("Finished conversion")
        retrieveUpdatedAdapter()

        // Change arrows so we generate expected data again
        arrows = listOf(
                // 5-zone arrows
                listOf(
                        TestData.ARROWS[0], TestData.ARROWS[1], TestData.ARROWS[1], TestData.ARROWS[3],
                        TestData.ARROWS[3], TestData.ARROWS[5], TestData.ARROWS[5], TestData.ARROWS[7],
                        TestData.ARROWS[7], TestData.ARROWS[9], TestData.ARROWS[9], TestData.ARROWS[9]
                ).mapIndexed { i, arrow -> arrow.toArrowValue(1, i) },
                // 10-zone arrows
                TestData.ARROWS.dropLast(1).plus(TestData.ARROWS[10])
                        .mapIndexed { i, arrow -> arrow.toArrowValue(2, i) }
        )
        val newExpectedData = generateExpectedData()
        for (indexedItem in newExpectedData.getData().withIndex()) {
            onView(withIndex(withId(R.id.text_vs_round_item__hsg), indexedItem.index))
                    .check(matches(withText(indexedItem.value.hitsScoreGolds)))
            onView(withIndex(withId(R.id.text_vs_round_item__handicap), indexedItem.index))
                    .check(matches(withText(indexedItem.value.handicap?.toString() ?: "-")))
        }
    }
}