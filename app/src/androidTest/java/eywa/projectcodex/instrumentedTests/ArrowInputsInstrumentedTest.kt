package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.checkContainsToast
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.WORCESTER_DEFAULT_ID
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ArrowInputsInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase
    private val arrowsPerArrowCount = 12
    val rounds = listOf(
            FullRoundInfo(
                    round = Round(1, "metric", "Metric", true, true),
                    roundSubTypes = listOf(),
                    roundArrowCounts = listOf(
                            RoundArrowCount(1, 1, 1.0, arrowsPerArrowCount),
                            RoundArrowCount(1, 2, 1.0, arrowsPerArrowCount),
                            RoundArrowCount(1, 3, 1.0, arrowsPerArrowCount),
                    ),
                    roundDistances = listOf(
                            RoundDistance(1, 1, 1, 90),
                            RoundDistance(1, 2, 1, 70),
                            RoundDistance(1, 3, 1, 50),
                    ),
            ),
            FullRoundInfo(
                    round = Round(2, "imperial", "Imperial", true, false),
                    roundSubTypes = listOf(),
                    roundArrowCounts = listOf(
                            RoundArrowCount(2, 1, 1.0, arrowsPerArrowCount),
                            RoundArrowCount(2, 2, 1.0, arrowsPerArrowCount),
                            RoundArrowCount(2, 3, 1.0, arrowsPerArrowCount),
                    ),
                    roundDistances = listOf(
                            RoundDistance(2, 1, 1, 90),
                            RoundDistance(2, 2, 1, 70),
                            RoundDistance(2, 3, 1, 50),
                    ),
            ),
            FullRoundInfo(
                    round = Round(3, "worcester", "Worcester", true, false, defaultRoundId = WORCESTER_DEFAULT_ID),
                    roundSubTypes = listOf(),
                    roundArrowCounts = listOf(
                            RoundArrowCount(3, 1, 1.0, arrowsPerArrowCount),
                    ),
                    roundDistances = listOf(
                            RoundDistance(3, 1, 1, 1),
                    ),
            ),
    )

    private val shoots = listOf(
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(shootId = 1, dateShot = TestUtils.generateDate(2020))
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(shootId = 2, dateShot = TestUtils.generateDate(2019))
                round = rounds[0]
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(shootId = 3, dateShot = TestUtils.generateDate(2018))
                round = rounds[1]
            },
    )

    /**
     * Set up [scenario] with desired fragment in the resumed state, and [db] with all desired information
     */
    private fun setup() {
        hiltRule.inject()

        // Start initialised so we can add to the database before the onCreate methods are called
        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!

            /*
             * Fill default rounds
             */
            runBlocking {
                rounds.forEach { db.add(it) }
                shoots.forEach { db.add(it) }
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testScoreButtons() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore {
                    /*
                     * Pressing each button
                     */
                    val buttons = listOf("m", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "X")
                    for (buttonText in buttons) {
                        val expected: Int = when (buttonText) {
                            "m" -> 0
                            "X" -> 10
                            else -> Integer.parseInt(buttonText)
                        }

                        clickScoreButton(buttonText)
                        checkInputtedArrows(listOf(buttonText))
                        checkEndTotal(expected)

                        clickScoreButton(buttonText)
                        checkInputtedArrows(listOf(buttonText, buttonText))
                        checkEndTotal(expected * 2)

                        clickClear()
                        checkInputtedArrows()
                        checkEndTotal(0)
                    }

                    /*
                     * Filling an end
                     */
                    clickScoreButton(3)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(3, 7))
                    checkEndTotal(10)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3))
                    checkEndTotal(13)
                    clickScoreButton(1)
                    clickScoreButton(1)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)

                    /*
                     * Too many arrows
                     */
                    clickScoreButton(7)
                    checkContainsToast("Arrows already added", composeTestRule)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)
                }
            }
        }
    }

    @Test
    fun testClearAndBackspace() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore {
                    /*
                     * Clear
                     */
                    // Full score
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(1)
                    clickScoreButton(1)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)
                    clickClear()
                    checkInputtedArrows()
                    checkEndTotal(0)

                    // Partial score
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(1)
                    checkInputtedArrows(listOf(3, 7, 3, 1))
                    checkEndTotal(14)
                    clickClear()
                    checkInputtedArrows()
                    checkEndTotal(0)

                    // No score
                    clickClear()
                    checkInputtedArrows()
                    checkEndTotal(0)

                    /*
                     * Backspace
                     */
                    // Full score
                    clickScoreButton(3)
                    clickScoreButton(7)
                    clickScoreButton(3)
                    clickScoreButton(1)
                    clickScoreButton(1)
                    clickScoreButton(3)
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1, 3))
                    checkEndTotal(18)

                    clickBackspace()
                    checkInputtedArrows(listOf(3, 7, 3, 1, 1))
                    checkEndTotal(15)

                    clickBackspace()
                    checkInputtedArrows(listOf(3, 7, 3, 1))
                    checkEndTotal(14)

                    clickBackspace()
                    clickBackspace()
                    checkInputtedArrows(listOf(3, 7))
                    checkEndTotal(10)

                    clickBackspace()
                    clickBackspace()
                    checkInputtedArrows()
                    checkEndTotal(0)

                    clickBackspace()
                    checkContainsToast("No arrows entered", composeTestRule)
                    checkInputtedArrows()
                    checkEndTotal(0)
                }
            }
        }
    }

    @Test
    fun testAbnormalEndSize() {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                longClickRow(1)

                clickContinueDropdownMenuItem {
                    checkInputtedArrows()
                    clickNavBarSettings {
                        setAddEndSize(5)
                        clickNavBarAddEnd { }
                    }

                    checkInputtedArrows(5)
                    completeEnd("1", 5)
                    checkInputtedArrows(5)
                    completeEnd("1", 5)
                    checkInputtedArrows(2)
                    completeEnd("1", 2)
                    checkInputtedArrows(5)
                }
            }
        }
    }

    @Test
    fun scoreButtonsChangeBasedOnRoundType() {
        setup()

        scenario.onActivity {
            runBlocking {
                listOf(
                        RoundFace.FULL,
                        RoundFace.TRIPLE,
                        RoundFace.HALF,
                        RoundFace.FITA_SIX,
                ).forEachIndexed { index, face ->
                    db.add(
                            ShootPreviewHelperDsl.create {
                                shoot = shoot.copy(
                                        shootId = 4 + index,
                                        dateShot = TestUtils.generateDate(2021, 1 + index),
                                )
                                faces = listOf(face)
                            }
                    )
                }

                db.add(
                        ShootPreviewHelperDsl.create {
                            shoot = shoot.copy(
                                    shootId = 8,
                                    dateShot = TestUtils.generateDate(2021, 5),
                            )
                            round = rounds[2]
                            faces = listOf(RoundFace.WORCESTER_FIVE)
                        }
                )
                db.add(
                        ShootPreviewHelperDsl.create {
                            shoot = shoot.copy(
                                    shootId = 9,
                                    dateShot = TestUtils.generateDate(2021, 6),
                            )
                            round = rounds[2]
                        }
                )
            }
        }

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()

                // Worcester full
                longClickRow(0)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("6")
                    clickScoreButton(2)
                    clickScoreButton(4)
                    clickScoreButton(5)
                    checkInputtedArrows(listOf(2, 4, 5))
                    pressBack()
                }

                // Worcester five
                longClickRow(1)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("3")
                    checkScoreButtonNotDisplayed("6")
                    clickScoreButton(4)
                    clickScoreButton(5)
                    checkInputtedArrows(listOf(4, 5))
                    pressBack()
                }

                // Fita six
                longClickRow(2)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("4")
                    clickScoreButton(5)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(5, 7))
                    pressBack()
                }

                // Half
                longClickRow(3)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("5")
                    clickScoreButton(6)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(6, 7))
                    pressBack()
                }

                // Half
                longClickRow(4)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("5")
                    clickScoreButton(6)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(6, 7))
                    pressBack()
                }

                // Full
                longClickRow(5)
                clickContinueDropdownMenuItem {
                    clickScoreButton(1)
                    clickScoreButton(2)
                    clickScoreButton(7)
                    clickScoreButton(8)
                    checkInputtedArrows(listOf(1, 2, 7, 8))
                    pressBack()
                }

                // Metric
                longClickRow(7)
                clickContinueDropdownMenuItem {
                    clickScoreButton(1)
                    clickScoreButton(2)
                    clickScoreButton(7)
                    clickScoreButton(8)
                    checkInputtedArrows(listOf(1, 2, 7, 8))
                    pressBack()
                }

                // Imperial
                longClickRow(8)
                clickContinueDropdownMenuItem {
                    checkScoreButtonNotDisplayed("2")
                    checkScoreButtonNotDisplayed("8")
                    clickScoreButton(1)
                    clickScoreButton(3)
                    clickScoreButton(7)
                    checkInputtedArrows(listOf(1, 3, 7))
                    pressBack()
                }
            }
        }
    }
}
