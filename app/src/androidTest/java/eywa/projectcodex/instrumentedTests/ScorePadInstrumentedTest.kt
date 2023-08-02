package eywa.projectcodex.instrumentedTests

import android.content.res.Resources
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.instrumentedTests.robots.archerRoundScore.EditEndRobot
import eywa.projectcodex.instrumentedTests.robots.archerRoundScore.ScorePadRobot.ExpectedRowData
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ScorePadInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(10)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var resources: Resources
    private lateinit var db: ScoresRoomDatabase

    private suspend fun addArcherRound(archerRoundId: Int = 1, roundId: Int? = null, year: Int = 2022) {
        db.shootDao().insert(
                DatabaseShoot(
                        shootId = archerRoundId,
                        dateShot = TestUtils.generateDate(year),
                        archerId = 1,
                )
        )
        roundId?.let {
            db.shootRoundDao().insert(
                    DatabaseShootRound(
                            shootId = archerRoundId,
                            roundId = roundId,
                    )
            )
        }
    }

    private suspend fun addArrows(indexes: List<Int>, archerRoundId: Int = 1) {
        indexes
                .mapIndexed { index, it -> TestUtils.ARROWS[it].toArrowScore(archerRoundId, index + 1) }
                .forEach { db.arrowScoreDao().insert(it) }
    }

    private fun setupActivity(setupDb: suspend () -> Unit) {
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity { activity ->
            db = LocalDatabaseModule.scoresRoomDatabase!!
            runBlocking {
                setupDb()
            }
            resources = activity.resources

            activity.recreate()
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testTableValues() {
        val arrows = listOf(
                0, 1, 2, 3, 4, 5,
                6, 7, 8, 9, 10, 11,
                5, 5, 5, 5, 5, 5,
        )

        setupActivity {
            addArcherRound(archerRoundId = 1, roundId = null)
            addArrows(arrows)
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    waitForLoad()
                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-5", 5, 15, 0, 15),
                                    ExpectedRowData("2", "6-7-8-9-10-X", 6, 50, 3, 65),
                                    ExpectedRowData("3", "5-5-5-5-5-5", 6, 30, 0, 95),
                                    ExpectedRowData("GT", "Grand Total", 17, 95, 3, null),
                            )
                    )

                    clickNavBarSettings {
                        setScorePadEndSize(3)
                        clickNavBarScorePad()
                    }

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2", 2, 3, 0, 3),
                                    ExpectedRowData("2", "3-4-5", 3, 12, 0, 15),
                                    ExpectedRowData("3", "6-7-8", 3, 21, 0, 36),
                                    ExpectedRowData("4", "9-10-X", 3, 29, 3, 65),
                                    ExpectedRowData("5", "5-5-5", 3, 15, 0, 80),
                                    ExpectedRowData("6", "5-5-5", 3, 15, 0, 95),
                                    ExpectedRowData("GT", "Grand Total", 17, 95, 3, null),
                            )
                    )
                }
            }
        }
    }

    @Test
    fun testTableValuesWithTotals() {
        val arrows = listOf(
                0, 1, 2, 3, 4, 5,
                6, 7, 8, 9, 10, 11,
                5, 5, 5, 5, 5, 5,

                5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5,

                5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5,
        )
        val arrowCounts = listOf(
                RoundArrowCount(1, 1, 1.0, 18),
                RoundArrowCount(1, 2, 1.0, 18),
                RoundArrowCount(2, 1, 1.0, 18),
                RoundArrowCount(2, 2, 1.0, 18),
        )
        val roundDistances = listOf(
                RoundDistance(1, 1, 1, 60),
                RoundDistance(1, 2, 1, 50),
                RoundDistance(2, 1, 1, 60),
                RoundDistance(2, 2, 1, 50),
        )

        setupActivity {
            db.roundDao().insert(Round(1, "RoundName1", "Round Name 1", false, true))
            db.roundDao().insert(Round(2, "RoundName2", "Round Name 2", true, false))

            arrowCounts.forEach { db.roundArrowCountDao().insert(it) }
            roundDistances.forEach { db.roundDistanceDao().insert(it) }

            addArcherRound(archerRoundId = 1, roundId = 1, year = 2022)
            addArrows(indexes = arrows, archerRoundId = 1)

            addArcherRound(archerRoundId = 2, roundId = 2, year = 2021)
            addArrows(indexes = arrows, archerRoundId = 2)
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    waitForLoad()
                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "10", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-5", 5, 15, 0, 15),
                                    ExpectedRowData("2", "6-7-8-9-10-X", 6, 50, 2, 65),
                                    ExpectedRowData("3", "5-5-5-5-5-5", 6, 30, 0, 95),
                                    ExpectedRowData("T", "Total at 60m", 17, 95, 2, null),
                                    ExpectedRowData("4", "5-5-5-5-5-5", 6, 30, 0, 125),
                                    ExpectedRowData("5", "5-5-5-5-5-5", 6, 30, 0, 155),
                                    ExpectedRowData("6", "5-5-5-5-5-5", 6, 30, 0, 185),
                                    ExpectedRowData("T", "Total at 50m", 18, 90, 0, null),
                                    ExpectedRowData("7", "5-5-5-5-5-5", 6, 30, 0, 215),
                                    ExpectedRowData("8", "5-5-5-5-5-5", 6, 30, 0, 245),
                                    ExpectedRowData("T", "Surplus Total", 12, 60, 0, null),
                                    ExpectedRowData("GT", "Grand Total", 47, 245, 2, null),
                            )
                    )
                    pressBack()
                }

                clickRow(1) {
                    waitForLoad()
                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-5", 5, 15, 0, 15),
                                    ExpectedRowData("2", "6-7-8-9-10-X", 6, 50, 3, 65),
                                    ExpectedRowData("3", "5-5-5-5-5-5", 6, 30, 0, 95),
                                    ExpectedRowData("T", "Total at 60yd", 17, 95, 3, null),
                                    ExpectedRowData("4", "5-5-5-5-5-5", 6, 30, 0, 125),
                                    ExpectedRowData("5", "5-5-5-5-5-5", 6, 30, 0, 155),
                                    ExpectedRowData("6", "5-5-5-5-5-5", 6, 30, 0, 185),
                                    ExpectedRowData("T", "Total at 50yd", 18, 90, 0, null),
                                    ExpectedRowData("7", "5-5-5-5-5-5", 6, 30, 0, 215),
                                    ExpectedRowData("8", "5-5-5-5-5-5", 6, 30, 0, 245),
                                    ExpectedRowData("T", "Surplus Total", 12, 60, 0, null),
                                    ExpectedRowData("GT", "Grand Total", 47, 245, 3, null),
                            )
                    )
                }
            }
        }
    }

    @Test
    fun testEmptyTable() {
        setupActivity {
            addArcherRound(archerRoundId = 1, roundId = null)
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    clickOkOnNoDataDialog()
                }
            }
        }
    }

    @Test
    fun testEditEnd() {
        val arrows = listOf(
                11, 9, 9, 9, 7, 6,
                1, 1, 1, 1, 1, 1,
                2, 2, 2, 2, 2, 2,
        )

        setupActivity {
            addArcherRound(archerRoundId = 1, roundId = null)
            addArrows(arrows)
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    waitForLoad()

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "X-9-9-9-7-6", 6, 50, 4, 50),
                                    ExpectedRowData("2", "1-1-1-1-1-1", 6, 6, 0, 56),
                                    ExpectedRowData("3", "2-2-2-2-2-2", 6, 12, 0, 68),
                                    ExpectedRowData("GT", "Grand Total", 18, 68, 4, null),
                            )
                    )

                    clickRow(2)
                    clickEditDropdownMenuItem {
                        clickClear()
                        repeat(3) {
                            clickScoreButton(3)
                        }
                        repeat(3) {
                            clickScoreButton(4)
                        }
                        clickComplete()
                    }

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "X-9-9-9-7-6", 6, 50, 4, 50),
                                    ExpectedRowData("2", "3-3-3-4-4-4", 6, 21, 0, 71),
                                    ExpectedRowData("3", "2-2-2-2-2-2", 6, 12, 0, 83),
                                    ExpectedRowData("GT", "Grand Total", 18, 83, 4, null),
                            )
                    )
                }
            }
        }
    }

    @Test
    fun testEditEndCancel() {
        cancelEditEndTest {
            clickCancel()
        }
    }

    @Test
    fun testEditEndBackButtonPress() {
        cancelEditEndTest {
            pressBack()
        }
    }

    private fun cancelEditEndTest(block: EditEndRobot.() -> Unit) {
        val arrows = listOf(
                0, 1, 2, 3, 4, 5,
                6, 7, 8, 9, 10, 11,
                5, 5, 5, 5, 5, 5,
        )

        setupActivity {
            addArcherRound(archerRoundId = 1, roundId = null)
            addArrows(arrows)
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    waitForLoad()

                    clickRow(1)
                    clickEditDropdownMenuItem {
                        checkEditEnd(1)
                        checkInputtedArrows(listOf("m", "1", "2", "3", "4", "5"))
                        block()
                    }

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-5", 5, 15, 0, 15),
                                    ExpectedRowData("2", "6-7-8-9-10-X", 6, 50, 3, 65),
                                    ExpectedRowData("3", "5-5-5-5-5-5", 6, 30, 0, 95),
                                    ExpectedRowData("GT", "Grand Total", 17, 95, 3, null),
                            )
                    )
                }
            }
        }
    }

    @Test
    fun testDeleteEnd() {
        val arrows = listOf(
                0, 1, 2, 3, 4, 4,
                6, 7, 8, 9, 10, 11,
                5, 5, 5, 5, 5, 5,
        )

        setupActivity {
            addArcherRound(archerRoundId = 1, roundId = null)
            addArrows(arrows)
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    waitForLoad()

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-4", 5, 14, 0, 14),
                                    ExpectedRowData("2", "6-7-8-9-10-X", 6, 50, 3, 64),
                                    ExpectedRowData("3", "5-5-5-5-5-5", 6, 30, 0, 94),
                                    ExpectedRowData("GT", "Grand Total", 17, 94, 3, null),
                            )
                    )

                    clickRow(2)
                    clickDeleteDropdownMenuItem(true, 2)

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-4", 5, 14, 0, 14),
                                    ExpectedRowData("2", "5-5-5-5-5-5", 6, 30, 0, 44),
                                    ExpectedRowData("GT", "Grand Total", 11, 44, 0, null),
                            )
                    )
                }
            }
        }
    }

    @Test
    fun testDeleteEnd_Partial() {
        val arrows = listOf(
                0, 1, 2, 3, 4, 4,
                6, 7, 8, 9, 10, 11,
                5, 5, 5
        )

        setupActivity {
            addArcherRound(archerRoundId = 1, roundId = null)
            addArrows(arrows)
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    waitForLoad()

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-4", 5, 14, 0, 14),
                                    ExpectedRowData("2", "6-7-8-9-10-X", 6, 50, 3, 64),
                                    ExpectedRowData("3", "5-5-5", 3, 15, 0, 79),
                                    ExpectedRowData("GT", "Grand Total", 14, 79, 3, null),
                            )
                    )

                    clickRow(3)
                    clickDeleteDropdownMenuItem(true, 3)

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-4", 5, 14, 0, 14),
                                    ExpectedRowData("2", "6-7-8-9-10-X", 6, 50, 3, 64),
                                    ExpectedRowData("GT", "Grand Total", 11, 64, 3, null),
                            )
                    )
                }
            }
        }
    }

    @Test
    fun testInsertEnd() {
        val arrows = listOf(
                0, 1, 2, 3, 4, 5,
                6, 7, 8, 9, 10, 11,
                5, 5, 5, 5, 5, 5,
        )

        setupActivity {
            addArcherRound(archerRoundId = 1, roundId = null)
            addArrows(arrows)
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    waitForLoad()

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-5", 5, 15, 0, 15),
                                    ExpectedRowData("2", "6-7-8-9-10-X", 6, 50, 3, 65),
                                    ExpectedRowData("3", "5-5-5-5-5-5", 6, 30, 0, 95),
                                    ExpectedRowData("GT", "Grand Total", 17, 95, 3, null),
                            )
                    )

                    clickRow(2)
                    clickInsertDropdownMenuItem {
                        checkInsertEndBefore(2)

                        checkInputtedArrows(6)
                        repeat(6) {
                            clickScoreButton(2)
                        }
                        clickComplete()
                    }

                    checkScorePadData(
                            listOf(
                                    ExpectedRowData(null, "Arrows", "H", "S", "G", "R/T"),
                                    ExpectedRowData("1", "m-1-2-3-4-5", 5, 15, 0, 15),
                                    ExpectedRowData("2", "2-2-2-2-2-2", 6, 12, 0, 27),
                                    ExpectedRowData("3", "6-7-8-9-10-X", 6, 50, 3, 77),
                                    ExpectedRowData("4", "5-5-5-5-5-5", 6, 30, 0, 107),
                                    ExpectedRowData("GT", "Grand Total", 23, 107, 3, null),
                            )
                    )
                }
            }
        }
    }
}
