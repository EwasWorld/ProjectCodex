package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.hiltModules.LocalDatastoreModule
import eywa.projectcodex.instrumentedTests.robots.ViewScoresRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.sql.Date
import java.util.*

@HiltAndroidTest
class ViewScoresInstrumentedTest {
    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private var shoots: List<DatabaseShoot> = listOf()
    private var rounds = listOf<FullRoundInfo>()
    private var arrows: List<List<DatabaseArrowScore>> = listOf()
    private var shootRound: List<DatabaseShootRound> = listOf()

    @Before
    fun beforeEach() {
        hiltRule.inject()
        shoots = listOf()
        rounds = listOf()
        arrows = listOf()

        scenario = composeTestRule.activityRule.scenario

        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(composeTestRule.activityRule)
    }

    private fun populateDb() {
        scenario.onActivity {
            runBlocking {
                rounds.forEach { db.add(it) }
                shoots.forEach { db.shootDao().insert(it) }
                arrows.flatten().forEach { db.arrowScoreDao().insert(it) }
                shootRound.forEach { db.shootRoundDao().insert(it) }
            }
        }
    }

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
                FullRoundInfo(
                        round = Round(1, "metricround", "Metric Round", true, true),
                        roundSubTypes = listOf(),
                        roundArrowCounts = listOf(
                                RoundArrowCount(1, 1, 122.0, 48),
                        ),
                        roundDistances = listOf(
                                RoundDistance(1, 1, 1, 70),
                        ),
                ),
                FullRoundInfo(
                        round = Round(2, "imperialround", "Imperial Round", true, true),
                        roundSubTypes = listOf(
                                RoundSubType(2, 1, "Sub Type 1"),
                                RoundSubType(2, 2, "Sub Type 2"),
                        ),
                        roundArrowCounts = listOf(
                                RoundArrowCount(2, 1, 122.0, 36),
                        ),
                        roundDistances = listOf(
                                RoundDistance(2, 1, 1, 60),
                                RoundDistance(2, 1, 2, 50),
                        ),
                ),
        )

        val firstOfThisYear =
                Date(Calendar.getInstance().get(Calendar.YEAR), Calendar.JANUARY, 1, 10, 0, 0).asCalendar()
//        val firstOfThisYear = Calendar.Builder()
//                .setFields(
//                        Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR),
//                        Calendar.MONTH, Calendar.JANUARY,
//                        Calendar.DAY_OF_MONTH, 1,
//                        Calendar.HOUR_OF_DAY, 10,
//                )
//                .build()
//                .time
        shoots = listOf(
                DatabaseShoot(1, firstOfThisYear),
                DatabaseShoot(2, Date.valueOf("2012-2-2").asCalendar()),
                DatabaseShoot(3, Date.valueOf("2011-3-3").asCalendar()),
                DatabaseShoot(4, Date.valueOf("2010-4-4").asCalendar()),
                DatabaseShoot(5, Date.valueOf("2009-5-5").asCalendar()),
        )
        shootRound = listOf(
                DatabaseShootRound(2, roundId = 1),
                DatabaseShootRound(3, roundId = 2),
                DatabaseShootRound(4, roundId = 2, roundSubTypeId = 2),
        )
        arrows = shoots.map { shoot ->
            val shootId = shoot.shootId
            List(1) { arrowNumber -> TestUtils.ARROWS[shootId].asArrowScore(shootId, arrowNumber) }
        }

        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(5)

                waitForHsg(0, "1/1/0")
                waitForHandicap(0, null)
                waitForRoundName(0, null)
                // Not checking the date as the year will change, other row's date checks are sufficient
                checkContentDescription(0, "1 Jan, Score 1, Golds 0, Hits 1")

                waitForHsg(1, "1/2/0")
                waitForHandicap(1, 88)
                waitForRoundName(1, "Metric Round")
                waitForDate(1, "02/02/12")
                checkContentDescription(1, "2 Feb 2012, Metric Round, Score 2, Handicap 88, Golds 0, Hits 1")

                waitForHsg(2, "1/3/0")
                waitForHandicap(2, 82)
                waitForRoundName(2, "Imperial Round")
                waitForDate(2, "03/03/11")
                checkContentDescription(2, "3 Mar 2011, Imperial Round, Score 3, Handicap 82, Golds 0, Hits 1")

                waitForHsg(3, "1/4/0")
                waitForHandicap(3, 80)
                waitForRoundName(3, "Sub Type 2")
                waitForDate(3, "04/04/10")
                checkContentDescription(3, "4 Apr 2010, Sub Type 2, Score 4, Handicap 80, Golds 0, Hits 1")

                waitForHsg(4, "1/5/0")
                waitForHandicap(4, null)
                waitForRoundName(4, null)
                waitForDate(4, "05/05/09")
                checkContentDescription(4, "5 May 2009, Score 5, Golds 0, Hits 1")

                LocalDatastoreModule.datastore.setValues(mapOf(DatastoreKey.Use2023HandicapSystem to false))
                waitForHandicap(0, null)
                waitForHandicap(1, 64)
                waitForHandicap(2, 63)
                waitForHandicap(3, 64)
                waitForHandicap(4, null)
            }
        }
    }

    /**
     * Test actions that do not change the data in the database
     */
    @Test
    fun testViewScoresEntry_NonDestructiveActions() {
        rounds = TestUtils.ROUNDS.take(1)

        shoots = listOf(
                // No round
                DatabaseShoot(1, Calendar.getInstance().apply { set(2020, 8, 28) }),
                // Completed round
                DatabaseShoot(2, TestUtils.generateDate(2019)),
        )
        shootRound = listOf(DatabaseShootRound(2, roundId = 1))
        arrows = listOf(
                TestUtils.ARROWS.mapIndexed { i, arrow -> arrow.asArrowScore(1, i) },
                // Add the correct number of arrows to complete the round
                List(rounds.first().roundArrowCounts!!.sumOf { it.arrowCount }) {
                    TestUtils.ARROWS[it % TestUtils.ARROWS.size].asArrowScore(2, it)
                },
        )
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)

                val rowId = 0

                // Single click - score pad
                clickRow(rowId) {
                    clickNavBarStats {
                        checkRound(null)
                    }
                    pressBack()
                }

                // Long click - score pad
                longClickRow(rowId)
                clickScorePadDropdownMenuItem {
                    clickNavBarStats {
                        checkRound(null)
                    }
                    pressBack()
                }

                // Long click - continue
                longClickRow(rowId)
                clickContinueDropdownMenuItem {
                    clickNavBarStats {
                        checkRound(null)
                    }
                    pressBack()
                }

                // Long click - continue not exist
                longClickRow(1)
                checkDropdownMenuItemNotThere(ViewScoresRobot.CommonStrings.CONTINUE_MENU_ITEM)

                // Long click - email
                longClickRow(rowId)
                clickEmailDropdownMenuItem {
                    checkScoreText("No Round - 28/09/20\nHits: 11, Score: 65, Golds (Golds): 3")
                }
                pressBack()

                // Long click - edit
                longClickRow(rowId)
                clickEditDropdownMenuItem {
                    roundsRobot.checkSelectedRound("No Round")
                    pressBack()
                }
            }
        }
    }

    @Test
    fun testViewScoresEntry_Delete() {
        shoots = listOf(
                DatabaseShoot(1, TestUtils.generateDate(2020)),
                DatabaseShoot(2, TestUtils.generateDate(2019)),
        )
        arrows = listOf(
                List(36) { TestUtils.ARROWS[1].asArrowScore(1, it) },
                List(36) { TestUtils.ARROWS[10].asArrowScore(2, it) },
        )
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)
                waitForHsg(0, "36/36/0")
                waitForHsg(1, "36/360/36")

                longClickRow(0)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.DELETE_MENU_ITEM)
                clickDeleteDialogCancel()

                waitForRowCount(2)

                longClickRow(0)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.DELETE_MENU_ITEM)
                clickDeleteDialogOk()

                waitForRowCount(1)
                waitForHsg(0, "36/360/36")
            }
        }
    }

    @Test
    fun testViewScoresEntry_Convert() {
        shoots = listOf(
                DatabaseShoot(1, TestUtils.generateDate(2020)),
                DatabaseShoot(2, TestUtils.generateDate(2019)),
        )
        arrows = listOf(
                TestUtils.ARROWS.mapIndexed { i, arrow -> arrow.asArrowScore(1, i) },
                TestUtils.ARROWS.mapIndexed { i, arrow -> arrow.asArrowScore(2, i) },
        )
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)
                waitForHsg(0, "11/65/3")
                waitForHsg(1, "11/65/3")
                // TODO Add checks of the score pad

                /*
                 * Cancelled
                 */
                longClickRow(0)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.CONVERT_MENU_ITEM)
                chooseConvertDialogOption(ViewScoresRobot.CommonStrings.CONVERT_TEN_ZONE_TO_FIVE_ZONE_OPTION)
                clickConvertDialogCancel()
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
                waitForHsg(1, "11/65/3")


                /*
                 * 10-zone to 5-zone
                 */
                longClickRow(1)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.CONVERT_MENU_ITEM)
                chooseConvertDialogOption(ViewScoresRobot.CommonStrings.CONVERT_TEN_ZONE_TO_FIVE_ZONE_OPTION)
                clickConvertDialogOk()
                waitForHsg(0, "11/65/3")
                waitForHsg(1, "11/59/3")
            }
        }
    }

    /**
     * Test selecting and deselecting items
     */
    @Test
    fun testMultiSelect_Selections() {
        val size = 4
        shoots = TestUtils.generateShoots(size)
        arrows = List(size) { i ->
            val roundId = shoots[i].shootId
            TestUtils.generateArrowScores(roundId, 36, roundId)
        }
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                val rowCount = 4

                waitForRowCount(rowCount)
                checkMultiSelectMode(false)

                clickStartMultiSelectMode()
                checkMultiSelectMode(true)
                checkEntriesSelected(listOf(), rowCount)

                // Select item
                clickRowForMultiSelect(0)
                checkEntriesSelected(listOf(0), rowCount)

                // Deselect item
                clickRowForMultiSelect(0)
                checkEntriesSelected(listOf(), rowCount)

                // Select all items from none
                clickMultiSelectSelectAll()
                checkEntriesSelected(0..3, rowCount)

                // Deselect all from all selected
                clickMultiSelectSelectAll()
                checkEntriesSelected(listOf(), rowCount)

                // Select two items
                clickRowForMultiSelect(1)
                clickRowForMultiSelect(2)
                checkEntriesSelected(listOf(1, 2), rowCount)

                // Deselect one
                clickRowForMultiSelect(2)
                checkEntriesSelected(listOf(1), rowCount)

                // Select all items from a single selected
                clickMultiSelectSelectAll()
                checkEntriesSelected(0..3, rowCount)

                // Deselect one item
                clickRowForMultiSelect(1)
                checkEntriesSelected(listOf(0, 2, 3), rowCount)
                checkMultiSelectMode(true)

                // Cancel
                clickCancelMultiSelectMode()
                checkEntriesNotSelectable()
                checkMultiSelectMode(false)
            }
        }
    }

    @Test
    fun testMultiSelect_Email() {
        val size = 4
        shoots = TestUtils.generateShoots(size)
        arrows = List(size) { i ->
            val roundId = shoots[i].shootId
            TestUtils.generateArrowScores(roundId, 36, roundId)
        }
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                val rowCount = 4
                waitForRowCount(rowCount)
                waitForHsg(0, "1/1/0")
                waitForHsg(1, "1/2/0")
                waitForHsg(2, "1/3/0")
                waitForHsg(3, "1/4/0")

                clickStartMultiSelectMode()
                clickMultiSelectSelectAll()
                checkEntriesSelected(0..3, rowCount)
                checkMultiSelectMode(true)

                clickMultiSelectEmail {
                    checkScoreText(
                            shoots.withIndex().joinToString("\n\n") { (index, round) ->
                                val date = DateTimeFormat.SHORT_DATE.format(round.dateShot)
                                "No Round - $date\nHits: 1, Score: ${index + 1}, Golds (Golds): 0"
                            }
                    )
                }
            }
        }
    }

    @Test
    fun testHelp_withMultiselect() {
        shoots = TestUtils.generateShoots(20)
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickStartMultiSelectMode()
                cycleThroughComposeHelpDialogs()
            }
        }
    }

    @Test
    fun testHelp_withScroll() {
        shoots = TestUtils.generateShoots(20)
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                scrollToRow(19)
                cycleThroughComposeHelpDialogs()
            }
        }
    }
}
