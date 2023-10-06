package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.instrumentedTests.robots.ArcherHandicapRobot
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ArcherHandicapE2eTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(9020)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

    private fun setup() {
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario
        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!
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
            clickArcherInfo {
                clickTab(ArcherHandicapRobot::class) {
                    checkNoHandicapsMessageShown()

                    ConditionWatcher.setTimeoutLimit(10_000)

                    clickAdd()
                    val date1 = DateTimeFormat.SHORT_DATE_TIME.parse("30/5/2020 10:20")
                    setAddDate(date1)
                    setAddHandicap(-1, "Must be between 0 and 150 (inclusive)")
                    setAddHandicap(50)
                    clickAddSubmit()

                    checkHandicap(0, date1, 50)
                    checkNoHandicapsMessageShown(false)

                    clickAdd()
                    val date2 = DateTimeFormat.SHORT_DATE_TIME.parse("30/4/2020 10:20")
                    setAddDate(date2)
                    setAddHandicap(25)
                    clickAddSubmit()
                    checkHandicap(1, date2, 25)

                    checkPastHeader(1)
                }
            }
        }
    }
}
