package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.TestUtils.parseDate
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersTypes
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.hiltModules.LocalDatastoreModule
import eywa.projectcodex.instrumentedTests.robots.ViewScoresFiltersRobot
import eywa.projectcodex.instrumentedTests.robots.ViewScoresRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsStatsRobot
import eywa.projectcodex.model.FullShootInfo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.Calendar

@HiltAndroidTest
class ViewScoresE2eTest {
    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(35)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private var shoots: List<FullShootInfo> = listOf()
    private var rounds = listOf<FullRoundInfo>()

    @Before
    fun beforeEach() {
        CommonSetupTeardownFns.generalSetup()

        hiltRule.inject()
        rounds = listOf()

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
                shoots.forEach { db.add(it) }
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
                        round = Round(2, "imperialround", "Imperial Round", true, false),
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

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        shoots = listOf(
                ShootPreviewHelperDsl.create {
                    val firstOfThisYear = DateTimeFormat.SHORT_DATE_TIME.parse("1/1/$currentYear 10:00")
                    shoot = shoot.copy(shootId = 1, dateShot = firstOfThisYear)
                    addIdenticalArrows(1, 1)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2, dateShot = DateTimeFormat.SHORT_DATE.parse("02/02/2012"))
                    round = rounds[0]
                    addIdenticalArrows(1, 2)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 3, dateShot = DateTimeFormat.SHORT_DATE.parse("03/03/2011"))
                    round = rounds[1]
                    addIdenticalArrows(1, 3)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 4, dateShot = DateTimeFormat.SHORT_DATE.parse("04/04/2010"))
                    round = rounds[1]
                    roundSubTypeId = 2
                    addIdenticalArrows(1, 4)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 5, dateShot = DateTimeFormat.SHORT_DATE.parse("05/05/2009"))
                    addIdenticalArrows(1, 5)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 6, dateShot = DateTimeFormat.SHORT_DATE.parse("06/06/2008"))
                    round = rounds[0]
                    addArrowCounter(6)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 7, dateShot = DateTimeFormat.SHORT_DATE.parse("07/07/2007"))
                    addArrowCounter(7)
                },
        )

        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(7)

                waitForHsg(0, "1/1/0")
                waitForHandicap(0, null)
                waitForRoundName(0, null)
                val expectedYear = currentYear.toString().takeLast(2)
                waitForDate(0, "01/01/$expectedYear 10:00")
                checkContentDescription(0, "1 Jan", "Score 1, 10s+ 0, Hits 1")

                waitForHsg(1, "1/2/0")
                waitForHandicap(1, 88)
                waitForRoundName(1, "Metric Round")
                waitForDate(1, "02/02/12 00:00")
                checkContentDescription(1, "2 Feb 2012", "Metric Round", "Score 2, 10s+ 0, Hits 1", "Handicap 88")

                waitForHsg(2, "1/3/0")
                waitForHandicap(2, 83)
                waitForRoundName(2, "Sub Type 1")
                waitForDate(2, "03/03/11 00:00")
                checkContentDescription(2, "3 Mar 2011", "Sub Type 1", "Score 3, Golds 0, Hits 1", "Handicap 83")

                waitForHsg(3, "1/4/0")
                waitForHandicap(3, 81)
                waitForRoundName(3, "Sub Type 2")
                waitForDate(3, "04/04/10 00:00")
                checkContentDescription(3, "4 Apr 2010", "Sub Type 2", "Score 4, Golds 0, Hits 1", "Handicap 81")

                waitForHsg(4, "1/5/0")
                waitForHandicap(4, null)
                waitForRoundName(4, null)
                waitForDate(4, "05/05/09 00:00")
                checkContentDescription(4, "5 May 2009", "Score 5, 10s+ 0, Hits 1")

                waitForArrowCount(5, 6)
                waitForRoundName(1, "Metric Round")
                waitForDate(5, "06/06/08 00:00")
                checkContentDescription(5, "6 Jun 2008", "Metric Round", "Count 6")

                waitForArrowCount(6, 7)
                waitForRoundName(6, null)
                waitForDate(6, "07/07/07 00:00")
                checkContentDescription(6, "7 Jul 2007", "Count 7")

                LocalDatastoreModule.datastore.setValues(mapOf(DatastoreKey.Use2023HandicapSystem to false))
                waitForHandicap(0, null)
                waitForHandicap(1, 64)
                waitForHandicap(2, 64)
                waitForHandicap(3, 65)
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
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1, dateShot = Calendar.getInstance().apply { set(2020, 8, 28) })
                    addFullSetOfArrows()
                },
                // Completed round
                ShootPreviewHelperDsl.create
                {
                    shoot = shoot.copy(shootId = 2, dateShot = TestUtils.generateDate(2019))
                    round = rounds[0]
                    completeRoundWithFullSet()
                },
                // Count
                ShootPreviewHelperDsl.create
                {
                    shoot = shoot.copy(shootId = 3, dateShot = Calendar.getInstance().apply { set(2018, 4, 15) })
                    addArrowCounter(6)
                },
        )
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(3)

                val rowId = 0

                // Single click - score pad
                clickRow(rowId) {
                    clickNavBarItem<ShootDetailsStatsRobot> {
                        checkRound(null)
                    }
                    pressBack()
                }

                // Long click - score pad
                longClickRow(rowId)
                clickScorePadDropdownMenuItem {
                    clickNavBarItem<ShootDetailsStatsRobot> {
                        checkRound(null)
                    }
                    pressBack()
                }

                // Long click - continue
                longClickRow(rowId)
                clickContinueDropdownMenuItem {
                    clickNavBarItem<ShootDetailsStatsRobot> {
                        checkRound(null)
                    }
                    pressBack()
                }

                // Long click - continue not exist
                longClickRow(1)
                checkDropdownMenuItemNotThere(ViewScoresRobot.CommonStrings.CONTINUE_MENU_ITEM)

                // Long click - email score
                longClickRow(rowId)
                clickEmailDropdownMenuItem {
                    checkScoreText("No Round - 28/09/20\nHits: 11, Score: 65, 10s+: 2")
                }
                pressBack()

                // Long click - edit
                longClickRow(rowId)
                clickEditDropdownMenuItem {
                    selectRoundsRobot.checkSelectedRound("No Round")
                    pressBack()
                }

                // Single click - view count
                clickRowCount(2) {
                    checkShotCount(6)
                    pressBack()
                }

                // Long click - view count
                longClickRow(2)
                clickViewDropdownMenuItem {
                    checkShotCount(6)
                    pressBack()
                }

                // Long click - email count
                longClickRow(2)
                clickEmailDropdownMenuItem {
                    checkScoreText("No Round - 15/05/18\n6 arrows shot")
                }
                pressBack()
            }
        }
    }

    @Test
    fun testViewScoresEntry_Delete() {
        shoots = listOf(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1, dateShot = TestUtils.generateDate(2020))
                    addIdenticalArrows(36, 1)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2, dateShot = TestUtils.generateDate(2019))
                    addIdenticalArrows(36, 10)
                },
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
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1, dateShot = TestUtils.generateDate(2020))
                    addFullSetOfArrows()
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2, dateShot = TestUtils.generateDate(2019))
                    addFullSetOfArrows()
                },
        )
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(2)
                waitForHsg(0, "11/65/2")
                waitForHsg(1, "11/65/2")
                // TODO Add checks of the score pad

                /*
                 * Cancelled
                 */
                longClickRow(0)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.CONVERT_MENU_ITEM)
                chooseConvertDialogOption(ViewScoresRobot.CommonStrings.CONVERT_TEN_ZONE_TO_FIVE_ZONE_OPTION)
                clickConvertDialogCancel()
                waitForHsg(0, "11/65/2")
                waitForHsg(1, "11/65/2")

                /*
                 * Xs to 10s
                 */
                longClickRow(0)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.CONVERT_MENU_ITEM)
                chooseConvertDialogOption(ViewScoresRobot.CommonStrings.CONVERT_XS_TO_TENS_OPTION)
                clickConvertDialogOk()
                waitForHsg(0, "11/65/2")
                waitForHsg(1, "11/65/2")


                /*
                 * 10-zone to 5-zone
                 */
                longClickRow(1)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.CONVERT_MENU_ITEM)
                chooseConvertDialogOption(ViewScoresRobot.CommonStrings.CONVERT_TEN_ZONE_TO_FIVE_ZONE_OPTION)
                clickConvertDialogOk()
                waitForHsg(0, "11/65/2")
                waitForHsg(1, "11/59/0")
            }
        }
    }

    /**
     * Test selecting and deselecting items
     */
    @Test
    fun testMultiSelect_Selections() {
        shoots = listOf(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1, dateShot = TestUtils.generateDate(2020))
                    setArrowsWithFinalScore(1, 36)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2, dateShot = TestUtils.generateDate(2019))
                    setArrowsWithFinalScore(2, 36)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 3, dateShot = TestUtils.generateDate(2018))
                    setArrowsWithFinalScore(3, 36)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 4, dateShot = TestUtils.generateDate(2017))
                    setArrowsWithFinalScore(4, 36)
                },
        )
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
        shoots = listOf(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1, dateShot = TestUtils.generateDate(2020))
                    setArrowsWithFinalScore(1, 36)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2, dateShot = TestUtils.generateDate(2019))
                    setArrowsWithFinalScore(2, 36)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 3, dateShot = TestUtils.generateDate(2018))
                    setArrowsWithFinalScore(3, 36)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 4, dateShot = TestUtils.generateDate(2017))
                    setArrowsWithFinalScore(4, 36)
                },
        )
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
                                val date = DateTimeFormat.SHORT_DATE.format(round.shoot.dateShot)
                                "No Round - $date\nHits: 1, Score: ${index + 1}, 10s+: 0"
                            }
                    )
                }
            }
        }
    }

    @Test
    fun testHelp_withMultiselect() {
        shoots = List(20) {
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(shootId = it + 1, dateShot = TestUtils.generateDate())
            }
        }
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
        shoots = List(20) {
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(shootId = it + 1, dateShot = TestUtils.generateDate())
            }
        }
        populateDb()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                scrollToRow(19)
                cycleThroughComposeHelpDialogs()
            }
        }
    }

    @Test
    fun testFilters() {
        shoots = listOf(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1, dateShot = "30/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    completeRoundWithFinalScore(1)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2, dateShot = "29/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    completeRoundWithFinalScore(2)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 3, dateShot = "28/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    completeRoundWithFinalScore(3)
                    deleteLastArrow()
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 4, dateShot = "27/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    roundSubTypeId = 6
                    completeRoundWithCounter()
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 5, dateShot = "25/10/2020 12:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    completeRoundWithFinalScore(5)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 6, dateShot = "25/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    roundSubTypeId = 6
                    completeRoundWithFinalScore(6)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 7, dateShot = "24/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa25RoundData
                    completeRoundWithFinalScore(7)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 8, dateShot = "23/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa25RoundData
                    completeRoundWithFinalScore(8)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 9, dateShot = "22/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    completeRoundWithFinalScore(9)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 10, dateShot = "21/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    completeRoundWithFinalScore(10)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 11, dateShot = "20/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    completeRoundWithFinalScore(11)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 12, dateShot = "19/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                    completeRoundWithFinalScore(12)
                },
        )
        rounds = listOf(RoundPreviewHelper.wa1440RoundData, RoundPreviewHelper.wa25RoundData)
        populateDb()

        fun ViewScoresRobot.setFilter(block: ViewScoresFiltersRobot.() -> Unit) {
            clickFilters {
                block()
                clickClose()
            }
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                checkFiltersCount(0)
                checkRows(
                        "1/1/0", "1/2/0", "1/3/0",
                        144, "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1", "2/11/1", "2/12/1",
                )

                setFilter {
                    setFromDate(2020, 10, 20)
                }
                checkFiltersCount(1)
                waitForRowNotExist(11)
                checkRows(
                        "1/1/0", "1/2/0", "1/3/0",
                        144, "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1", "2/11/1",
                )

                setFilter {
                    setUntilDate(2020, 10, 10)
                    checkUntilDateErrorShown()
                }
                checkFiltersCount(1)
                waitForRowNotExist(11)
                checkRows(
                        "1/1/0", "1/2/0", "1/3/0",
                        144, "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1", "2/11/1",
                )

                setFilter {
                    setUntilDate(2020, 10, 29)
                }
                checkFiltersCount(1)
                waitForRowCount(10)
                waitForRowNotExist(10)
                checkRows(
                        "1/2/0", "1/3/0",
                        144, "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1", "2/11/1",
                )

                setFilter {
                    checkTypeFilter(ViewScoresFiltersTypes.ALL)
                    clickTypeFilter()
                    checkTypeFilter(ViewScoresFiltersTypes.SCORE)
                    clickTypeFilter()
                    checkTypeFilter(ViewScoresFiltersTypes.COUNT)
                }
                checkFiltersCount(2)
                waitForRowCount(1)
                checkRows(144)

                setFilter {
                    clickTypeFilter()
                    checkTypeFilter(ViewScoresFiltersTypes.ALL)
                    clickTypeFilter()
                    checkTypeFilter(ViewScoresFiltersTypes.SCORE)
                }
                checkFiltersCount(2)
                waitForRowCount(9)
                waitForRowNotExist(9)
                checkRows(
                        "1/2/0", "1/3/0",
                        "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1", "2/11/1",
                )

                setFilter {
                    setMinScore(3)
                }
                checkFiltersCount(3)
                waitForRowCount(8)
                checkRows(
                        "1/3/0",
                        "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1", "2/11/1",
                )

                setFilter {
                    setMaxScore(2)
                    checkScoreErrorShown()
                }
                checkFiltersCount(3)
                waitForRowCount(8)
                checkRows(
                        "1/3/0",
                        "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1", "2/11/1",
                )

                setFilter {
                    setMaxScore(10)
                }
                checkFiltersCount(3)
                waitForRowCount(7)
                checkRows(
                        "1/3/0",
                        "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1",
                )

                setFilter {
                    clickCompleteOnlyFilter()
                }
                checkFiltersCount(4)
                waitForRowCount(6)
                checkRows(
                        "1/5/0", "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1",
                )

                setFilter {
                    clickFirstOfDayFilter()
                }
                checkFiltersCount(5)
                waitForRowCount(5)
                checkRows(
                        "1/6/0",
                        "1/7/0", "1/8/0", "1/9/0",
                        "1/10/1",
                )

                setFilter {
                    with(selectRoundsRobot) {
                        clickSelectedRound {
                            clickRound(RoundPreviewHelper.wa25RoundData.getDisplayName(null))
                        }
                    }
                }
                checkFiltersCount(6)
                waitForRowCount(2)
                checkRows("1/7/0", "1/8/0")

                setFilter {
                    with(selectRoundsRobot) {
                        clickSelectedRound {
                            clickRound(RoundPreviewHelper.wa1440RoundData.round.displayName)
                        }
                    }
                }
                checkFiltersCount(6)
                waitForRowCount(3)
                checkRows("1/6/0", "1/9/0", "1/10/1")

                setFilter {
                    with(selectRoundsRobot) {
                        clickSelectedSubtype {
                            clickSubtypeDialogSubtype(RoundPreviewHelper.wa1440RoundData.roundSubTypes!![0].name!!)
                        }
                    }
                }
                checkFiltersCount(6)
                waitForRowCount(2)
                checkRows("1/9/0", "1/10/1")

                setFilter {
                    clearRoundsFilter()
                    clickPbsOnlyFilter()
                    checkPbFilterInterferenceErrorShown()
                }
                checkFiltersCount(6)
                waitForRowCount(2)
                checkRows("1/6/0", "1/8/0")

                setFilter {
                    clearDateFilters()
                    clearScoreFilters()
                }
                checkFiltersCount(4)
                waitForRowCount(3)
                checkRows("1/6/0", "1/8/0", "2/12/1")
            }
        }
    }
}
