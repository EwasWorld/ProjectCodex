package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils.parseDate
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.ClassificationTablesRobot
import eywa.projectcodex.instrumentedTests.robots.HandicapTablesRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.model.FullShootInfo
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

    private var shoots: List<FullShootInfo>? = null
    private var archerInfo: DatabaseArcher? = null
    private var bow: ClassificationBow? = null

    private fun setup() {
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!

            runBlocking {
                listOf(
                        RoundPreviewHelper.yorkRoundData,
                        RoundPreviewHelper.wa25RoundData,
                        RoundPreviewHelper.wa1440RoundData,
                ).forEach { db.add(it) }
                shoots?.forEach { db.add(it) }
                archerInfo?.let {
                    db.archerRepo().updateDefaultArcher(it.isGent)
                    db.archerRepo().updateDefaultArcher(it.age)
                }
                bow?.let { db.bowRepo().updateDefaultBow(it) }
            }
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    /**
     * - Check tables load based on inputs
     * - Check info stays between navigation to HandicapTables
     */
    @Test
    fun testClassifications() {
        setup()

        composeTestRule.mainMenuRobot {
            clickHandicapTables {
                clickTab(ClassificationTablesRobot::class) {
                    checkAge("Senior")
                    checkGender()
                    checkBowStyle("Recurve")

                    checkClassifications(
                            listOf(
                                    ClassificationTablesRobot.TableRow("Archer 3rd", null, 72, false),
                                    ClassificationTablesRobot.TableRow("Archer 2nd", null, 65, false),
                                    ClassificationTablesRobot.TableRow("Archer 1st", null, 58, false),
                                    ClassificationTablesRobot.TableRow("Bowman 3rd", null, 51, false),
                                    ClassificationTablesRobot.TableRow("Bowman 2nd", null, 44, false),
                                    ClassificationTablesRobot.TableRow("Bowman 1st", null, 37, false),
                                    ClassificationTablesRobot.TableRow("Master Bowman", null, 30, false),
                                    ClassificationTablesRobot.TableRow("Grand MB", null, 23, false),
                                    ClassificationTablesRobot.TableRow("Elite GMB", null, 16, false),
                            )
                    )

                    selectRoundsRobot.clickSelectedRound {
                        clickRound("York")
                    }
                    selectRoundsRobot.checkSelectedRound("York")
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

                    selectRoundsRobot.clickSelectedSubtype {
                        clickSubtypeDialogSubtype("Hereford")
                    }
                    checkClassifications(
                            listOf(
                                    ClassificationTablesRobot.TableRow("Archer 3rd", 427, 72),
                                    ClassificationTablesRobot.TableRow("Archer 2nd", 571, 65),
                                    ClassificationTablesRobot.TableRow("Archer 1st", 721, 58),
                                    ClassificationTablesRobot.TableRow("Bowman 3rd", 863, 51),
                                    ClassificationTablesRobot.TableRow("Bowman 2nd", 985, 44),
                                    ClassificationTablesRobot.TableRow("Bowman 1st", 1083, 37, false),
                                    ClassificationTablesRobot.TableRow("Master Bowman", 1160, 30, false),
                                    ClassificationTablesRobot.TableRow("Grand MB", 1218, 23, false),
                                    ClassificationTablesRobot.TableRow("Elite GMB", 1259, 16, false),
                            )
                    )

                    selectRoundsRobot.clickSelectedSubtype {
                        clickSubtypeDialogSubtype("York")
                    }
                    clickGender(false)
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

                    selectRoundsRobot.clickSelectedSubtype {
                        clickSubtypeDialogSubtype("Hereford")
                    }
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

                    clickTab(HandicapTablesRobot::class) {}
                }
                clickTab(ClassificationTablesRobot::class) {
                    selectRoundsRobot.checkSelectedRound("York")
                    selectRoundsRobot.checkSelectedSubtype("Hereford")
                    checkGender(false)
                    checkAge("U15")
                    checkBowStyle("Compound")
                }
            }
        }
    }

    @Test
    fun testScreenStartsWithMostRecentRound() {
        shoots = listOf(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1, dateShot = "10/12/2020 10:00".parseDate())
                    round = RoundPreviewHelper.wa1440RoundData
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2, dateShot = "11/12/2020 10:00".parseDate())
                    round = RoundPreviewHelper.yorkRoundData
                },
        )
        archerInfo = DatabaseArcherPreviewHelper.default.copy(isGent = false, age = ClassificationAge.U15)
        bow = ClassificationBow.BAREBOW
        setup()

        composeTestRule.mainMenuRobot {
            clickHandicapTables {
                clickTab(ClassificationTablesRobot::class) {
                    checkAge("U15")
                    checkGender(false)
                    checkBowStyle("Barebow")

                    selectRoundsRobot.checkSelectedRound("York")

                    selectRoundsRobot.clickSelectedRound {
                        clickRound("WA 25")
                    }
                    clickTab(HandicapTablesRobot::class) {
                        clickTab(ClassificationTablesRobot::class) {}
                    }
                    selectRoundsRobot.checkSelectedRound("WA 25")
                }
            }
        }
    }
}
