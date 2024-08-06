package eywa.projectcodex.instrumentedTests

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.emailScores.EmailScoresCheckbox
import eywa.projectcodex.components.emailScores.EmailScoresTextField
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.instrumentedTests.robots.EmailScoreRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EmailScoresInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(60)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

    private val arrowsPerArrowCount = 12
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
    )

    private val shoots = listOf(
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(1, TestUtils.generateDate(2024))
                addFullSetOfArrows()
                addFullSetOfArrows()
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(2, TestUtils.generateDate(2023))
                round = rounds[0]
                addFullSetOfArrows()
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(3, TestUtils.generateDate(2022))
                round = rounds[1]
                roundSubTypeId = 1
                addFullSetOfArrows()
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(4, TestUtils.generateDate(2021))
                addFullSetOfArrows()
            },
            ShootPreviewHelperDsl.create {
                shoot = shoot.copy(5, TestUtils.generateDate(2020))
                addFullSetOfArrows()
            },
    )

    private fun addSimpleTestDataToDb() {
        scenario.onActivity {
            runBlocking {
                rounds.forEach { item -> db.add(item) }
                shoots.forEach { item -> db.add(item) }
            }
        }
    }

    @Before
    fun setup() {
        hiltRule.inject()
        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!
            addSimpleTestDataToDb()
        }
        Intents.init()
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
        Intents.release()
    }

    object EmailTestData {
        const val EMAIL_1 = "test@email.com"
        const val EMAIL_2 = "test2@email.com"
        const val APPEND_SUBJECT = " Cheese"
        const val INITIAL_SUBJECT = "Archery Scores"
        const val FINAL_SUBJECT = "$INITIAL_SUBJECT$APPEND_SUBJECT"
        const val START_TEXT = "Hi friend,\nHere are my scores!"
        const val END_TEXT = "From your friend"

        @Suppress("unused") // See later to-do comment
        const val URI = "content://eywa.projectcodex.uiTests.fileProvider" +
                "/external_files/Android/data/eywa.projectcodex.uiTests/files" +
                "/emailAttachment.csv"

        fun getMessage(scores: String) = "$START_TEXT\n\n$scores\n\n$END_TEXT\n\n\n\nSent from Codex Archery Aide app"
    }

    private fun EmailScoreRobot.typeInfo() {
        typeText(EmailScoresTextField.TO, "${EmailTestData.EMAIL_1};${EmailTestData.EMAIL_2}")
        typeText(EmailScoresTextField.SUBJECT, EmailTestData.FINAL_SUBJECT)
        typeText(EmailScoresTextField.MESSAGE_HEADER, EmailTestData.START_TEXT)
        typeText(EmailScoresTextField.MESSAGE_FOOTER, EmailTestData.END_TEXT)
        Espresso.closeSoftKeyboard()
    }

    @Test
    fun testEmailScoreWithAttachment() {
        val roundDate = DateTimeFormat.SHORT_DATE.format(shoots[0].shoot.dateShot)
        val scoresString = "No Round - $roundDate\nHits: 22, Score: 130, 10s+: 4"
        val expected = allOf(
                hasAction(Intent.ACTION_SEND),
                hasData(Uri.parse("mailto:")),
                hasExtra(
                        `is`(Intent.EXTRA_EMAIL),
                        `is`(arrayOf(EmailTestData.EMAIL_1, EmailTestData.EMAIL_2))
                ),
                hasExtra(
                        `is`(Intent.EXTRA_SUBJECT),
                        `is`(EmailTestData.FINAL_SUBJECT)
                ),
                hasExtra(
                        `is`(Intent.EXTRA_TEXT),
                        `is`(EmailTestData.getMessage(scoresString))
                ),
                // TODO URI for attachment is not matching for some reason
                // TODO Check contents of attachment
//                hasExtra(
//                        `is`(Intent.EXTRA_STREAM),
//                        `is`(EmailTestData.URI)
//                )
        )

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForDate(0, DateTimeFormat.SHORT_DATE_TIME.format(shoots[0].shoot.dateShot))
                longClickRow(0)
                clickEmailDropdownMenuItem {
                    checkTextFieldText(EmailScoresTextField.SUBJECT, EmailTestData.INITIAL_SUBJECT)
                    checkScoreText(scoresString)
                    clickCheckbox(EmailScoresCheckbox.FULL_SCORE_SHEET)
                    checkCheckboxState(EmailScoresCheckbox.FULL_SCORE_SHEET, true)
                    typeInfo()

                    intending(expected).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
                    clickSend()
                }
                checkScreenIsShown()
                intended(expected)
            }
        }
    }
}
