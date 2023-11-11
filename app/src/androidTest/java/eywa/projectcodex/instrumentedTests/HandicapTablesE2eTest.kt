package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.HandicapTablesRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HandicapTablesE2eTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

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
                TestUtils.ROUNDS.forEach { db.add(it) }
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
                checkNoDataInTable()

                setInputText("120")


                selectRoundBaseRobot.clickSelectedRound {
                    clickRound("WA 25")
                }
                selectFaceBaseRobot.openSingleSelectDialog {
                    clickOption("Triple")
                }

                checkTableData(
                        listOf(
                                HandicapTablesRobot.TableRow(114, 15),
                                HandicapTablesRobot.TableRow(115, 14),
                                HandicapTablesRobot.TableRow(116, 13),
                                HandicapTablesRobot.TableRow(117, 12),
                                HandicapTablesRobot.TableRow(119, 11),

                                HandicapTablesRobot.TableRow(120, 10),

                                HandicapTablesRobot.TableRow(122, 9),
                                HandicapTablesRobot.TableRow(124, 8),
                                HandicapTablesRobot.TableRow(126, 7),
                                HandicapTablesRobot.TableRow(129, 6),
                                HandicapTablesRobot.TableRow(132, 5),
                        )
                )

                clickInputMethod()
                setInputText("10")

                checkTableData(
                        listOf(
                                HandicapTablesRobot.TableRow(114, 15),
                                HandicapTablesRobot.TableRow(115, 14),
                                HandicapTablesRobot.TableRow(116, 13),
                                HandicapTablesRobot.TableRow(117, 12),
                                HandicapTablesRobot.TableRow(119, 11),

                                HandicapTablesRobot.TableRow(120, 10),

                                HandicapTablesRobot.TableRow(122, 9),
                                HandicapTablesRobot.TableRow(124, 8),
                                HandicapTablesRobot.TableRow(126, 7),
                                HandicapTablesRobot.TableRow(129, 6),
                                HandicapTablesRobot.TableRow(132, 5),
                        )
                )
            }
        }
    }
}
