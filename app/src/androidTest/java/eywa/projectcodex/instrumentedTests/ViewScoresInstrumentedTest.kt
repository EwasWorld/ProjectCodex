package eywa.projectcodex.instrumentedTests

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
import eywa.projectcodex.common.*
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.components.viewScores.data.ViewScoreData
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.instrumentedTests.daggerObjects.DatabaseDaggerTestModule
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

class ViewScoresInstrumentedTest {
    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(60)

    private lateinit var scenario: FragmentScenario<ViewScoresFragment>
    private lateinit var navController: TestNavHostController
    private lateinit var db: ScoresRoomDatabase
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
            db = DatabaseDaggerTestModule.scoresRoomDatabase
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.viewScoresFragment)
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
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
                ArcherRound(1, TestUtils.generateDate(), 1, false),
                ArcherRound(2, TestUtils.generateDate(), 1, false, roundId = 1),
                ArcherRound(3, TestUtils.generateDate(), 1, false, roundId = 2),
                ArcherRound(4, TestUtils.generateDate(), 1, false, roundId = 2, roundSubTypeId = 2)
        ).map { archerRound ->
            ArcherRoundWithRoundInfoAndName(
                    archerRound,
                    rounds.find { it.roundId == archerRound.roundId },
                    roundSubTypes.find {
                        it.roundId == archerRound.roundId && it.subTypeId == archerRound.roundSubTypeId
                    }?.name
            )
        }
        arrows = archerRounds.map { archerRound ->
            val archerRoundId = archerRound.archerRound.archerRoundId
            List(36) { arrowNumber -> TestUtils.ARROWS[archerRoundId].toArrowValue(archerRoundId, arrowNumber) }
        }

        addToDbAndRetrieveAdapter()
    }

    private fun ViewScoresEntry.getExpectedHsg(): String {
        val score = this.id * 36
        return "36/$score/0"
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

    private fun checkData(
            indexedExpectedData: Iterable<IndexedValue<ViewScoresEntry>>,
            useExpectedHsg: Boolean = true
    ) {
        for (indexedItem in indexedExpectedData) {
            scenario.onFragment {
                layoutManager.scrollToPosition(indexedItem.index)
            }
            CustomConditionWaiter.waitForTextToAppear(
                    if (useExpectedHsg) indexedItem.value.getExpectedHsg() else indexedItem.value.hitsScoreGolds,
                    R.id.text_vs_round_item__hsg,
                    indexedItem.index
            )
            CustomConditionWaiter.waitForTextToAppear(
                    indexedItem.value.handicap?.toString() ?: "-",
                    R.id.text_vs_round_item__handicap,
                    indexedItem.index
            )
        }
    }

    @Test
    fun testTableValues() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()
        checkData(expectedData.getData().withIndex())
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
        CustomConditionWaiter.waitForTextToAppear(clickedItem.getExpectedHsg())
        onView(withIndex(withText(clickedItem.getExpectedHsg()), 0)).perform(longClick())
        CustomConditionWaiter.waitForMenuToAppear(CommonStrings.Menus.viewRoundsShowScorePad)
        onView(withText(CommonStrings.Menus.viewRoundsShowScorePad)).perform(click())

        assertEquals(R.id.scorePadFragment, navController.currentDestination?.id)
        assertEquals(clickedItem.id, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    @Test
    fun testContinueRound() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()

        val clickedItem = expectedData.getData().find { it.round?.roundId == 1 }!!
        CustomConditionWaiter.waitForTextToAppear(clickedItem.getExpectedHsg())
        onView(withIndex(withText(clickedItem.getExpectedHsg()), 0)).perform(longClick())
        CustomConditionWaiter.waitForMenuToAppear(CommonStrings.Menus.viewRoundsContinue)
        CustomConditionWaiter.waitFor(2000)
        onView(withText(CommonStrings.Menus.viewRoundsContinue)).perform(click())

        assertEquals(R.id.inputEndFragment, navController.currentDestination?.id)
        assertEquals(clickedItem.id, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
    }

    @Test
    fun testDeleteRow() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()

        val deleteItem = expectedData.getData().filterIndexed { i, entry ->
            i != 0 && entry.round != null
        }.first()
        CustomConditionWaiter.waitForTextToAppear(deleteItem.getExpectedHsg())
        onView(withIndex(withText(deleteItem.getExpectedHsg()), 0)).perform(longClick())
        CustomConditionWaiter.waitForMenuToAppear(CommonStrings.Menus.viewRoundsDelete)
        onView(withText(CommonStrings.Menus.viewRoundsDelete)).perform(click())

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "wait for row to be removed"
            }

            override fun checkCondition(): Boolean {
                return expectedData.getData().size - 1 == layoutManager.childCount
            }
        })

        checkData(expectedData.getData().minus(deleteItem).withIndex())
    }

    @Test
    fun testContinueCompletedRound() {
        generateBasicDataAndAddToDb()
        val expectedData = generateExpectedData()

        val clickedItem = expectedData.getData().find { it.round?.roundId == 2 }!!
        CustomConditionWaiter.waitForTextToAppear(
                clickedItem.getExpectedHsg(),
                CustomConditionWaiter.Companion.ClickType.LONG_CLICK
        )
        CustomConditionWaiter.waitForMenuToAppear(CommonStrings.Menus.viewRoundsConvert)
        onView(withText(CommonStrings.Menus.viewRoundsContinue)).withFailureHandler { _, _ ->
            onView(withText(CommonStrings.Menus.viewRoundsContinue)).check(matches(not(isDisplayed())))
        }.check(doesNotExist())
    }

    @Test
    fun testConvertRound() {
        archerRounds = listOf(
                ArcherRoundWithRoundInfoAndName(
                        ArcherRound(1, TestUtils.generateDate(2020), 1, false)
                ),
                ArcherRoundWithRoundInfoAndName(
                        ArcherRound(2, TestUtils.generateDate(2019), 1, false)
                )
        )
        arrows = listOf(
                TestUtils.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) },
                TestUtils.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(2, i + 1) }
        )
        addToDbAndRetrieveAdapter()
        val expectedData = generateExpectedData()

        // Convert first score
        val expectedHsg = expectedData.getData().find { it.id == 1 }!!.hitsScoreGolds
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
                        TestUtils.ARROWS[0], TestUtils.ARROWS[1], TestUtils.ARROWS[1], TestUtils.ARROWS[3],
                        TestUtils.ARROWS[3], TestUtils.ARROWS[5], TestUtils.ARROWS[5], TestUtils.ARROWS[7],
                        TestUtils.ARROWS[7], TestUtils.ARROWS[9], TestUtils.ARROWS[9], TestUtils.ARROWS[9]
                ).mapIndexed { i, arrow -> arrow.toArrowValue(1, i) },
                // 10-zone arrows
                TestUtils.ARROWS.dropLast(1).plus(TestUtils.ARROWS[10])
                        .mapIndexed { i, arrow -> arrow.toArrowValue(2, i) }
        )
        checkData(generateExpectedData().getData().withIndex(), false)
    }

    @Test
    fun testMultiSelections() {
        val size = 4
        archerRounds = TestUtils.generateArcherRounds(size).map { ArcherRoundWithRoundInfoAndName(it) }
        arrows = List(size) { i ->
            val roundId = archerRounds[i].archerRound.archerRoundId
            TestUtils.generateArrowValues(roundId, 36, roundId)
        }
        addToDbAndRetrieveAdapter()
        val expectedData = generateExpectedData().getData()

        CustomConditionWaiter.waitForTextToAppear(expectedData[0].hitsScoreGolds)

        for (i in 0 until size) {
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(not(isSelected())))
        }
        R.id.button_view_scores__start_multi_select.click()

        /*
         * Select a single item
         */
        onView(withText(expectedData[1].hitsScoreGolds)).perform(click())
        for (i in 0 until size) {
            var matcher = isSelected()
            if (i != 1) {
                matcher = not(matcher)
            }
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(matcher))
        }

        /*
         * Deselect the item
         */
        onView(withText(expectedData[1].hitsScoreGolds)).perform(click())
        for (i in 0 until size) {
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(not(isSelected())))
        }

        /*
         * Select all items from none then deselect all
         */
        R.id.button_view_scores__select_all_or_none.click()
        for (i in 0 until size) {
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(isSelected()))
        }

        R.id.button_view_scores__select_all_or_none.click()
        for (i in 0 until size) {
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(not(isSelected())))
        }

        /*
         * Select two items
         */
        onView(withText(expectedData[1].hitsScoreGolds)).perform(click())
        onView(withText(expectedData[2].hitsScoreGolds)).perform(click())
        for (i in 0 until size) {
            var matcher = isSelected()
            if (i != 1 && i != 2) {
                matcher = not(matcher)
            }
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(matcher))
        }

        /*
         * Deselect one item
         */
        onView(withText(expectedData[2].hitsScoreGolds)).perform(click())
        for (i in 0 until size) {
            var matcher = isSelected()
            if (i != 1) {
                matcher = not(matcher)
            }
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(matcher))
        }

        /*
         * Select all items from single selection
         */
        R.id.button_view_scores__select_all_or_none.click()
        for (i in 0 until size) {
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(isSelected()))
        }

        /*
         * Deselect one item
         */
        onView(withText(expectedData[1].hitsScoreGolds)).perform(click())
        for (i in 0 until size) {
            var matcher = isSelected()
            if (i == 1) {
                matcher = not(matcher)
            }
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(matcher))
        }

        /*
         * Cancel
         */
        R.id.button_view_scores__cancel_selection.click()
        for (i in 0 until size) {
            onView(withIndex(withId(R.id.layout_vs_round_item), i)).check(matches(not(isSelected())))
        }
    }
}