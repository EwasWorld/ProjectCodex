package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.components.archerRoundScore.inputEnd.InputEndFragment
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.newScore.NewScoreFragment
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresFragment
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.instrumentedTests.daggerObjects.DatabaseDaggerTestModule
import eywa.projectcodex.instrumentedTests.robots.ViewScoresRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.sql.Date

class ViewScoresInstrumentedTest {
    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(160)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController
    private lateinit var db: ScoresRoomDatabase
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

        scenario = composeTestRule.activityRule.scenario

        scenario.onActivity {
            db = DatabaseDaggerTestModule.scoresRoomDatabase
            navController = it.navHostFragment.navController
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(composeTestRule.activityRule)
    }

    private fun populateDb() {
        scenario.onActivity {
            runBlocking {
                rounds.forEach { db.roundDao().insert(it) }
                roundSubTypes.forEach { db.roundSubTypeDao().insert(it) }
                roundArrowCounts.forEach { db.roundArrowCountDao().insert(it) }
                roundDistances.forEach { db.roundDistanceDao().insert(it) }
                archerRounds.forEach { db.archerRoundDao().insert(it.archerRound) }
                arrows.flatten().forEach { db.arrowValueDao().insert(it) }
            }
        }
    }

    // TODO_CURRENT Test semantics string?

    @Test
    fun testEmptyTable() {
        composeTestRule.mainMenuRobot {
            clickViewScores {
                clickOkOnEmptyTableDialog()
            }
        }
    }

    @Test
    fun testViewScoresEntry_Values() {
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
                ArcherRound(1, Date.valueOf("2013-1-1"), 1),
                ArcherRound(2, Date.valueOf("2012-2-2"), 1, roundId = 1),
                ArcherRound(3, Date.valueOf("2011-3-3"), 1, roundId = 2),
                ArcherRound(4, Date.valueOf("2010-4-4"), 1, roundId = 2, roundSubTypeId = 2),
                ArcherRound(5, Date.valueOf("2009-5-5"), 1),
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
            List(1) { arrowNumber -> TestUtils.ARROWS[archerRoundId].toArrowValue(archerRoundId, arrowNumber) }
        }

        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(5)

                waitForHsg(0, "1/1/0")
                waitForHandicap(0, null)
                waitForRoundName(0, null)
                waitForDate(0, "01/01/13")

                waitForHsg(1, "1/2/0")
                waitForHandicap(1, 64)
                waitForRoundName(1, "Metric Round")
                waitForDate(1, "02/02/12")

                waitForHsg(2, "1/3/0")
                waitForHandicap(2, 63)
                waitForRoundName(2, "Imperial Round")
                waitForDate(2, "03/03/11")

                waitForHsg(3, "1/4/0")
                waitForHandicap(3, 64)
                waitForRoundName(3, "Sub Type 2")
                waitForDate(3, "04/04/10")

                waitForHsg(4, "1/5/0")
                waitForHandicap(4, null)
                waitForRoundName(4, null)
                waitForDate(4, "05/05/09")
            }
        }
    }

    /**
     * Test actions that do not change the data in the database
     */
    @Test
    fun testViewScoresEntry_NonDestructiveActions() {
        val roundId = 1
        rounds = TestUtils.ROUNDS.filter { it.roundId == roundId }
        roundArrowCounts = TestUtils.ROUND_ARROW_COUNTS.filter { it.roundId == roundId }
        roundSubTypes = TestUtils.ROUND_SUB_TYPES.filter { it.roundId == roundId }
        roundDistances = TestUtils.ROUND_DISTANCES.filter { it.roundId == roundId }

        archerRounds = listOf(
                // No round
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestUtils.generateDate(2020), 1)),
                // Completed round
                ArcherRoundWithRoundInfoAndName(ArcherRound(2, TestUtils.generateDate(2019), 1, roundId = 1)),
        )
        arrows = listOf(
                TestUtils.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i) },
                // Add the correct number of arrows to complete the round
                List(roundArrowCounts.sumOf { it.arrowCount }) {
                    TestUtils.ARROWS[it % TestUtils.ARROWS.size].toArrowValue(2, it)
                },
        )
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)

                val archerRoundId = 1
                val rowId = 0

                // Single click - score pad
                clickRow(rowId)
                CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
                assertEquals(archerRoundId, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
                pressBack()

                // Long click - score pad
                longClickRow(rowId)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.SCORE_PAD_MENU_ITEM)
                CustomConditionWaiter.waitForFragmentToShow(scenario, (ScorePadFragment::class))
                assertEquals(archerRoundId, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
                pressBack()

                // Long click - continue
                longClickRow(rowId)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.CONTINUE_MENU_ITEM)
                CustomConditionWaiter.waitForFragmentToShow(scenario, (InputEndFragment::class))
                assertEquals(archerRoundId, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
                pressBack()

                // Long click - continue not exist
                longClickRow(1)
                checkDropdownMenuItemNotThere(ViewScoresRobot.CommonStrings.CONTINUE_MENU_ITEM)

                // Long click - email
                longClickRow(rowId)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.EMAIL_MENU_ITEM)
                CustomConditionWaiter.waitForFragmentToShow(scenario, (EmailScoresFragment::class))
                assertEquals(archerRoundId, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
                pressBack()

                // Long click - edit
                longClickRow(rowId)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.EDIT_MENU_ITEM)
                CustomConditionWaiter.waitForFragmentToShow(scenario, (NewScoreFragment::class))
                assertEquals(archerRoundId, navController.currentBackStackEntry?.arguments?.get("archerRoundId"))
                pressBack()
            }
        }
    }

    @Test
    fun testViewScoresEntry_Delete() {
        archerRounds = listOf(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestUtils.generateDate(2020), 1)),
                ArcherRoundWithRoundInfoAndName(ArcherRound(2, TestUtils.generateDate(2019), 1)),
        )
        arrows = listOf(
                List(36) { TestUtils.ARROWS[1].toArrowValue(1, it) },
                List(36) { TestUtils.ARROWS[10].toArrowValue(2, it) },
        )
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)
                waitForHsg(0, "36/36/0")
                waitForHsg(1, "36/360/36")

                longClickRow(0)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.DELETE_MENU_ITEM)
                // TODO_CURRENT make an 'are you sure' popup

                waitForRowCount(1)
                waitForHsg(0, "36/360/36")
            }
        }
    }

    @Test
    fun testViewScoresEntry_Convert() {
        archerRounds = listOf(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestUtils.generateDate(2020), 1)),
                ArcherRoundWithRoundInfoAndName(ArcherRound(2, TestUtils.generateDate(2019), 1)),
        )
        arrows = listOf(
                TestUtils.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i) },
                TestUtils.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(2, i) },
        )
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)
                waitForHsg(0, "11/65/3")
                waitForHsg(1, "11/65/3")

                /*
                 * Xs to 10s
                 */
                longClickRow(0)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.CONVERT_MENU_ITEM)
                chooseConvertDialogOption(ViewScoresRobot.CommonStrings.CONVERT_XS_TO_TENS_OPTION)
                clickConvertDialogOk()
                waitForHsg(0, "11/65/3")
                // TODO Check score pad


                /*
                 * 10-zone to 5-zone
                 */
                longClickRow(1)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.CONVERT_MENU_ITEM)
                chooseConvertDialogOption(ViewScoresRobot.CommonStrings.CONVERT_TEN_ZONE_TO_FIVE_ZONE_OPTION)
                clickConvertDialogOk()
                waitForHsg(1, "11/59/3")
                // TODO Check score pad
            }
        }
    }

    /**
     * Test selecting and deselecting items
     */
    @Test
    fun testMultiSelect_Selections() {
        val size = 4
        archerRounds = TestUtils.generateArcherRounds(size).map { ArcherRoundWithRoundInfoAndName(it) }
        arrows = List(size) { i ->
            val roundId = archerRounds[i].archerRound.archerRoundId
            TestUtils.generateArrowValues(roundId, 36, roundId)
        }
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(4)
                checkMultiSelectMode(false)

                clickStartMultiSelectMode()
                checkMultiSelectMode(true)
                checkEntriesSelected(listOf())

                // Select item
                clickRow(0)
                checkEntriesSelected(listOf(0))

                // Deselect item
                clickRow(0)
                checkEntriesSelected(listOf())

                // Select all items from none
                clickMultiSelectSelectAll()
                checkEntriesSelected(0..3)

                // Deselect all from all selected
                clickMultiSelectSelectAll()
                checkEntriesSelected(listOf())

                // Select two items
                clickRow(1)
                clickRow(2)
                checkEntriesSelected(listOf(1, 2))

                // Deselect one
                clickRow(2)
                checkEntriesSelected(listOf(1))

                // Select all items from a single selected
                clickMultiSelectSelectAll()
                checkEntriesSelected(0..3)

                // Deselect one item
                clickRow(1)
                checkEntriesSelected(listOf(0, 2, 3))
                checkMultiSelectMode(true)

                // Cancel
                clickCancelMultiSelectMode()
                checkEntriesSelected(listOf())
                checkMultiSelectMode(false)
            }
        }
    }

    @Test
    fun testMultiSelect_Email() {
        val size = 4
        archerRounds = TestUtils.generateArcherRounds(size).map { ArcherRoundWithRoundInfoAndName(it) }
        arrows = List(size) { i ->
            val roundId = archerRounds[i].archerRound.archerRoundId
            TestUtils.generateArrowValues(roundId, 36, roundId)
        }
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(4)
                waitForHsg(0, "1/1/0")
                waitForHsg(1, "1/2/0")
                waitForHsg(2, "1/3/0")
                waitForHsg(3, "1/4/0")

                clickStartMultiSelectMode()
                clickMultiSelectSelectAll()
                checkEntriesSelected(0..3)
                checkMultiSelectMode(true)

                clickMultiSelectEmail()
                CustomConditionWaiter.waitForFragmentToShow(scenario, (EmailScoresFragment::class))

                // TODO_CURRENT Check email scores shows correct items
            }
        }
    }
}