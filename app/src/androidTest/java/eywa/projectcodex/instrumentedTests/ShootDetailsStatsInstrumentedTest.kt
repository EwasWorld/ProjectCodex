package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.TestUtils.parseDate
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archer.DEFAULT_ARCHER_ID
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.archer.HandicapType
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.hiltModules.LocalDatastoreModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsStatsRobot.PastRecordsDialogItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ShootDetailsStatsInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(15)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

    private val arrowsPerArrowCount = 12
    private val archerHandicap = DatabaseArcherHandicap(
            archerHandicapId = 1,
            archerId = DEFAULT_ARCHER_ID,
            bowStyle = ClassificationBow.RECURVE,
            handicapType = HandicapType.OUTDOOR,
            handicap = 40,
            dateSet = Calendar.getInstance(),
    )
    private val rounds = listOf(
            FullRoundInfo(
                    round = Round(1, "round1", "Round1", true, false),
                    roundSubTypes = listOf(),
                    roundArrowCounts = listOf(
                            RoundArrowCount(1, 1, 122.0, arrowsPerArrowCount),
                            RoundArrowCount(1, 2, 122.0, arrowsPerArrowCount),
                    ),
                    roundDistances = listOf(
                            RoundDistance(1, 1, 1, 60),
                            RoundDistance(1, 2, 1, 50),
                    ),
            ),
            FullRoundInfo(
                    round = Round(2, "round2", "Round2", true, false),
                    roundSubTypes = listOf(
                            RoundSubType(2, 1, "Sub Type 1"),
                            RoundSubType(2, 2, "Sub Type 2"),
                    ),
                    roundArrowCounts = listOf(
                            RoundArrowCount(2, 1, 122.0, arrowsPerArrowCount),
                            RoundArrowCount(2, 2, 122.0, arrowsPerArrowCount),
                    ),
                    roundDistances = listOf(
                            RoundDistance(2, 1, 1, 60),
                            RoundDistance(2, 2, 1, 50),
                            RoundDistance(2, 1, 2, 30),
                            RoundDistance(2, 2, 2, 20),
                    ),
            ),
            RoundPreviewHelper.yorkRoundData,
            RoundPreviewHelper.wa1440RoundData,
    )
    private var shoots = listOf(
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(
                        shootId = 1,
                        dateShot = DateTimeFormat.SHORT_DATE_TIME.parse("17/7/2014 15:21"),
                )
                faces = listOf(RoundFace.HALF)
                arrows = listOf(List(6) { 10 }, List(38) { 5 }, List(4) { 0 })
                        .flatten()
                        .mapIndexed { index, score -> DatabaseArrowScore(1, index + 1, score) }
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(
                        shootId = 2,
                        dateShot = TestUtils.generateDate(2013),
                )
                round = rounds[0]
                addIdenticalArrows(size = arrowsPerArrowCount, score = 8)
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(
                        shootId = 3,
                        dateShot = TestUtils.generateDate(2012),
                )
                round = rounds[1]
                roundSubTypeId = 1
                faces = listOf(RoundFace.FULL, RoundFace.HALF)
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(
                        shootId = 4,
                        dateShot = DateTimeFormat.SHORT_DATE_TIME.parse("20/12/2011 15:21"),
                        archerId = DEFAULT_ARCHER_ID,
                )
                round = rounds[2]
                completeRoundWithFinalScore(1264) // 6 HC
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(
                        shootId = 5,
                        dateShot = DateTimeFormat.SHORT_DATE_TIME.parse("19/12/2011 15:21"),
                )
                round = rounds[2]
                completeRoundWithFinalScore(1239) // 11 HC
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(
                        shootId = 6,
                        dateShot = DateTimeFormat.SHORT_DATE_TIME.parse("18/12/2011 15:21"),
                )
                round = rounds[2]
                completeRoundWithFinalScore(1250) // 9 HC
            },
    )

    /**
     * Set up [scenario] with desired fragment in the resumed state, and [db]
     * with all desired information
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
                db.archerRepo().insert(archerHandicap)
            }
        }

        CustomConditionWaiter.waitFor(500)
    }

    @After
    fun teardown() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testAllStatsNoRound() {
        setup()
        val expectedScore = 38 * 5 + 6 * 10

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(0) {
                    waitForLoad()
                    clickNavBarStats {
                        checkDate("17 Jul 14 15:21")
                        checkHits("44 (of 48)")
                        checkScore(expectedScore)
                        checkGolds(6)
                        checkRound(null)
                        checkRemainingArrows(null)
                        checkHandicap(null)
                        checkPredictedScore(null)
                        checkPb(isPb = false)
                        checkAllowance(null)
                        checkPastRecordsTextNotShown()
                        checkAdjustedScore(null)
                        facesRobot.checkFaces("Half")
                    }
                }
            }
        }
    }

    @Test
    fun testHasRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(1) {
                    waitForLoad()
                    clickNavBarStats {
                        val predictedScore = ceil((192 + 201) / 2f).roundToInt()
                        val allowance = 1250

                        checkRound(shoots[1].round!!.displayName)
                        checkRemainingArrows(arrowsPerArrowCount)
                        // Checked these values in the handicap tables (2023) - double and use score for 2 doz as only
                        // the first distance has been shot so this is what's being use to calculate the handicap
                        checkHandicap(36)
                        // divide by 2 because only one dozen was shot
                        checkPredictedScore(predictedScore)
                        facesRobot.checkFaces("Full")
                        checkPb(isPb = false)
                        checkAllowance(allowance)
                        checkPastRecordsTextNotShown()
                        checkArcherHandicap(40)
                        checkAdjustedScore(allowance + predictedScore)
                    }
                }
            }
        }
    }

    @Test
    fun testOldHandicapSystem() {
        LocalDatastoreModule.datastore.setValues(mapOf(DatastoreKey.Use2023HandicapSystem to false))
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(1) {
                    waitForLoad()
                    clickNavBarStats {
                        checkRound(shoots[1].round!!.displayName)
                        checkRemainingArrows(arrowsPerArrowCount)
                        // Checked these values in the handicap tables (1998) - double and use score for 2 doz as only
                        // the first distance has been shot so this is what's being use to calculate the handicap
                        checkHandicap(32)
                        // divide by 2 because only one dozen was shot
                        checkPredictedScore(floor((192 + 201) / 2f).roundToInt())
                    }
                }
            }
        }
    }

    @Test
    fun testRoundWithSubTypeEmptyScore() {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                longClickRow(2)
                clickContinueDropdownMenuItem {
                    waitForLoad()
                    clickNavBarStats {
                        checkRound(shoots[2].roundSubType!!.name!!)
                        checkRemainingArrows(arrowsPerArrowCount * 2)
                        checkHandicap(null)
                        facesRobot.checkFaces("Full, Half")
                    }
                }
            }
        }
    }

    @Test
    fun testAllowanceAndPastScores() {
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForLoad()
                clickRow(3) {
                    clickNavBarStats {
                        checkScore(1264)
                        checkHandicap(6)
                        checkPb()
                        checkArcherHandicap(40)
                        checkAllowance(535)
                        checkAdjustedScore(1264 + 535)
                        clickPastRecordsText()
                        checkPastRecordsDialogItems(
                                listOf(
                                        PastRecordsDialogItem("20/12/11", 1264, "Personal best! - Current"),
                                        PastRecordsDialogItem("18/12/11", 1250),
                                        PastRecordsDialogItem("19/12/11", 1239),
                                )
                        )
                        clickPastRecordsRecentTab()
                        checkPastRecordsDialogItems(
                                listOf(
                                        PastRecordsDialogItem("20/12/11", 1264, "Personal best! - Current"),
                                        PastRecordsDialogItem("19/12/11", 1239),
                                        PastRecordsDialogItem("18/12/11", 1250),
                                )
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testEditAndSwapToCount() {
        setup()

        composeTestRule.mainMenuRobot {
            clickNewScore {
                clickSubmitNewScore {
                    clickNavBarStats {
                        clickEditRoundData {
                            clickType(false)

                            // Changing type opens up the count screen
                            clickSubmitEditScoreChangeToCount {
                                Espresso.pressBack()
                            }
                        }
                    }
                }
            }
            // New score and such should all have been removed from the backstack
            checkScreenIsShown()
        }
    }

    @Test
    fun testClassifications() {
        shoots = listOf(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1, dateShot = "10/10/2023 10:00".parseDate())
                    round = RoundPreviewHelper.yorkRoundData
                    completeRoundWithFinalScore(800)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2, dateShot = "10/10/2022 10:00".parseDate())
                    round = RoundPreviewHelper.yorkRoundData
                    completeRoundWithFinalScore(800)
                    arrows = arrows!!.drop(1)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 3, dateShot = "10/10/2021 10:00".parseDate())
                    round = rounds[0]
                    completeRoundWithFinalScore(200)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 4, dateShot = "10/10/2020 10:00".parseDate())
                    round = RoundPreviewHelper.yorkRoundData
                    completeRoundWithFinalScore(200)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 5, dateShot = "10/10/2019 10:00".parseDate())
                    round = RoundPreviewHelper.yorkRoundData
                    roundSubTypeId = 2
                    completeRound(9)
                },
        )
        setup()

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForRowCount(5)
                clickRow(0) {
                    clickNavBarStats {
                        checkRound("York")
                        checkScore(800)
                        checkClassification(
                                classification = Classification.BOWMAN_3RD_CLASS,
                                isOfficial = true,
                                isPredicted = false,
                        )
                        checkClassificationCategory("Senior Gentleman Recurve")
                        pressBack()
                    }
                }

                clickRow(1) {
                    clickNavBarStats {
                        checkRound("York")
                        checkScore(790)
                        checkClassification(
                                classification = Classification.BOWMAN_3RD_CLASS,
                                isOfficial = true,
                                isPredicted = true,
                        )
                        checkClassificationCategory("Senior Gentleman Recurve")
                        pressBack()
                    }
                }

                clickRow(2) {
                    clickNavBarStats {
                        checkRound("Round1")
                        checkScore(200)
                        checkClassification(
                                classification = Classification.BOWMAN_1ST_CLASS,
                                isOfficial = false,
                                isPredicted = false,
                        )
                        checkClassificationCategory("Senior Gentleman Recurve")
                        pressBack()
                    }
                }

                clickRow(3) {
                    clickNavBarStats {
                        checkRound("York")
                        checkScore(200)
                        checkClassification(
                                classification = null,
                                isOfficial = true,
                                isPredicted = false,
                        )
                        checkClassificationCategory("Senior Gentleman Recurve")
                        pressBack()
                    }
                }

                clickRow(4) {
                    clickNavBarStats {
                        checkRound("Hereford")
                        checkScore(1296)
                        checkClassification(
                                classification = Classification.ELITE_MASTER_BOWMAN,
                                isOfficial = false,
                                isPredicted = false,
                        )
                        checkClassificationCategory("Senior Gentleman Recurve")
                        pressBack()
                    }
                }
            }
        }
    }
}
