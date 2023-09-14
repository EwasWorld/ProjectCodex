package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.ClassificationTablesRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ClassificationTablesE2eTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

    private fun setup() {
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!

            runBlocking {
                listOf(
                        RoundPreviewHelper.yorkRoundData,
                        RoundPreviewHelper.wa25RoundData,
                ).forEach { db.add(it) }
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testHandicaps() {
        setup()

        composeTestRule.mainMenuRobot {
            clickHandicapTables {
                clickClassificationTables {
                    checkNoClassifications()

                    roundRobot.clickSelectedRound()
                    roundRobot.clickRoundDialogRound("York")
                    roundRobot.checkSelectedRound("York")
                    checkClassifications(
                            listOf(
                                    ClassificationTablesRobot.TableRow("Archer 3rd", 278, 72),
                                    ClassificationTablesRobot.TableRow("Archer 2nd", 394, 65),
                                    ClassificationTablesRobot.TableRow("Archer 1st", 534, 58),
                                    ClassificationTablesRobot.TableRow("Bowman 3rd", 684, 51),
                                    ClassificationTablesRobot.TableRow("Bowman 2nd", 829, 44),
                                    ClassificationTablesRobot.TableRow("Bowman 1st", 957, 37),
                                    ClassificationTablesRobot.TableRow("Master Bowman", 1061, 30),
                                    ClassificationTablesRobot.TableRow("Grand MB", 1142, 23),
                                    ClassificationTablesRobot.TableRow("Elite GMB", 1205, 16),
                            )
                    )

                    clickGender()
                    setAge("U15")
                    setBowStyle("Compound")
                    checkClassifications(
                            listOf(
                                    ClassificationTablesRobot.TableRow("Archer 3rd", 236, 75),
                                    ClassificationTablesRobot.TableRow("Archer 2nd", 325, 69),
                                    ClassificationTablesRobot.TableRow("Archer 1st", 432, 63),
                                    ClassificationTablesRobot.TableRow("Bowman 3rd", 555, 57),
                                    ClassificationTablesRobot.TableRow("Bowman 2nd", 684, 51),
                                    ClassificationTablesRobot.TableRow("Bowman 1st", 809, 45),
                                    ClassificationTablesRobot.TableRow("Master Bowman", 923, 39),
                                    ClassificationTablesRobot.TableRow("Grand MB", 1019, 33),
                                    ClassificationTablesRobot.TableRow("Elite GMB", 1098, 27),
                            )
                    )

                    roundRobot.clickSelectedSubtype()
                    roundRobot.clickSubtypeDialogSubtype("Hereford")
                    checkClassifications(
                            listOf(
                                    ClassificationTablesRobot.TableRow("Archer 3rd", 370, 75),
                                    ClassificationTablesRobot.TableRow("Archer 2nd", 487, 69),
                                    ClassificationTablesRobot.TableRow("Archer 1st", 614, 63),
                                    ClassificationTablesRobot.TableRow("Bowman 3rd", 742, 57),
                                    ClassificationTablesRobot.TableRow("Bowman 2nd", 863, 51),
                                    ClassificationTablesRobot.TableRow("Bowman 1st", 969, 45),
                                    ClassificationTablesRobot.TableRow("Master Bowman", 1057, 39),
                                    ClassificationTablesRobot.TableRow("Grand MB", 1129, 33),
                                    ClassificationTablesRobot.TableRow("Elite GMB", 1187, 27),
                            )
                    )
                }
            }
        }
    }
}
