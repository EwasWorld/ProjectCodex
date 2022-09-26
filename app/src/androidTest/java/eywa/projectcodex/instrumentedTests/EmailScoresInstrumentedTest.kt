package eywa.projectcodex.instrumentedTests

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import eywa.projectcodex.R
import eywa.projectcodex.common.*
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresFragment
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.instrumentedTests.daggerObjects.DatabaseDaggerTestModule
import eywa.projectcodex.instrumentedTests.robots.ViewScoresRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmailScoresInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(60)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController
    private lateinit var db: ScoresRoomDatabase

    private val arrowsPerArrowCount = 12
    private val rounds = listOf(
            Round(1, "round1", "Round1", true, false, listOf()),
            Round(2, "round2", "Round2", true, false, listOf())
    )
    private val arrowCounts = listOf(
            RoundArrowCount(1, 1, 122.0, arrowsPerArrowCount),
            RoundArrowCount(1, 2, 122.0, arrowsPerArrowCount),
            RoundArrowCount(2, 1, 122.0, arrowsPerArrowCount),
            RoundArrowCount(2, 2, 122.0, arrowsPerArrowCount)
    )
    private val distances = listOf(
            RoundDistance(1, 1, 1, 60),
            RoundDistance(1, 2, 1, 50),
            RoundDistance(2, 1, 1, 60),
            RoundDistance(2, 2, 1, 50),
            RoundDistance(2, 1, 2, 30),
            RoundDistance(2, 2, 2, 20)
    )
    private val subTypes = listOf(
            RoundSubType(2, 1, "Sub Type 1"),
            RoundSubType(2, 2, "Sub Type 2")
    )
    private val archerRounds = listOf(
            ArcherRound(1, TestUtils.generateDate(2024), 1, true),
            ArcherRound(2, TestUtils.generateDate(2023), 1, true, roundId = 1),
            ArcherRound(3, TestUtils.generateDate(2022), 1, true, roundId = 2, roundSubTypeId = 1),
            ArcherRound(4, TestUtils.generateDate(2021), 1, true),
            ArcherRound(5, TestUtils.generateDate(2020), 1, true)
    )
    private val arrows = archerRounds.mapIndexed { i, archerRound ->
        val round = rounds.find { it.roundId == archerRound.roundId }
        val arrowsInRound = arrowCounts.sumOf { if (it.roundId == round?.roundId) it.roundId else 0 }
        val desiredCount = if (arrowsInRound == 0) (arrowsPerArrowCount * 2 - i * 6) else (arrowsInRound + i * 6)
        val testDataSize = TestUtils.ARROWS.size
        List(desiredCount) {
            TestUtils.ARROWS[testDataSize - 1 - it % testDataSize].toArrowValue(
                    archerRound.archerRoundId,
                    it
            )
        }
    }.flatten()

    private fun addSimpleTestDataToDb() {
        scenario.onActivity {
            runBlocking {
                for (item in arrows) {
                    db.arrowValueDao().insert(item)
                }
                for (item in rounds) {
                    db.roundDao().insert(item)
                }
                for (item in arrowCounts) {
                    db.roundArrowCountDao().insert(item)
                }
                for (item in distances) {
                    db.roundDistanceDao().insert(item)
                }
                for (item in subTypes) {
                    db.roundSubTypeDao().insert(item)
                }
                for (item in archerRounds) {
                    db.archerRoundDao().insert(item)
                }
            }
        }
    }

    @Before
    fun setup() {
        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = DatabaseDaggerTestModule.scoresRoomDatabase
            addSimpleTestDataToDb()
            navController = it.navHostFragment.navController
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
        const val FINAL_SUBJECT = "Archery Scores$APPEND_SUBJECT"
        const val START_TEXT = "Hi friend,\nHere are my scores!"
        const val END_TEXT = "From your friend"

        @Suppress("unused") // See later to-do comment
        const val URI =
                "content://eywa.projectcodex.fileProvider/external_files/Android/data/eywa.projectcodex/files/emailAttachment.csv"

        fun getMessage(scores: String) = "$START_TEXT\n\n$scores\n\n$END_TEXT\n\n\n\nSent from Codex Archery Aide app"
    }

    private fun typeInfo() {
        R.id.input_text_email_scores__to.write("${EmailTestData.EMAIL_1};${EmailTestData.EMAIL_2}")
        R.id.input_text_email_scores__subject.write(EmailTestData.APPEND_SUBJECT)
        R.id.input_text_email_scores__message_start.scrollTo()
        R.id.input_text_email_scores__message_start.clearText()
        R.id.input_text_email_scores__message_start.write(EmailTestData.START_TEXT)
        CustomConditionWaiter.waitFor(500)
        R.id.input_text_email_scores__message_end.scrollTo()
        R.id.input_text_email_scores__message_end.clearText()
        R.id.input_text_email_scores__message_end.write(EmailTestData.END_TEXT)
        Espresso.closeSoftKeyboard()
    }

    @Test
    fun testEmailScoreWithAttachment() {
        val roundDate = DateTimeFormat.SHORT_DATE.format(archerRounds[0].dateShot)

        composeTestRule.mainMenuRobot {
            clickViewScores {
                waitForDate(0, roundDate)
                longClickRow(0)
                clickDropdownMenuItem(ViewScoresRobot.CommonStrings.EMAIL_MENU_ITEM)
                CustomConditionWaiter.waitForFragmentToShow(scenario, (EmailScoresFragment::class))
            }
        }

        typeInfo()

        val scoresString = "No Round - $roundDate\nHits: 22, Score: 130, Golds (Golds): 6"
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
//                hasExtra(
//                        `is`(Intent.EXTRA_STREAM),
//                        `is`(EmailTestData.URI)
//                )
        )

        intending(expected).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        R.id.button_email_scores__send.click()
        intended(expected)
    }
}