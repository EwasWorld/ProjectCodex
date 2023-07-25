package eywa.projectcodex

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.hiltModules.LocalUpdateDefaultRoundsModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UpdateDefaultRoundsActualE2eTests {
    companion object {
        init {
            LocalUpdateDefaultRoundsModule.useActual = true
        }

        @Suppress("unused")
        @BeforeClass
        fun classSetup() {
            LocalUpdateDefaultRoundsModule.useActual = true
        }

        @Suppress("unused")
        @AfterClass
        fun classTeardown() {
            LocalUpdateDefaultRoundsModule.useActual = false
        }
    }

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(120)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        hiltRule.inject()
        ConditionWatcher.setTimeoutLimit(5 * 60 * 1000)
        ConditionWatcher.setWatchInterval(5 * 1000)
    }

    @After
    fun teardown() {
        CommonSetupTeardownFns.teardownScenario(scenario)
        ConditionWatcher.setTimeoutLimit(ConditionWatcher.DEFAULT_TIMEOUT_LIMIT)
        ConditionWatcher.setWatchInterval(ConditionWatcher.DEFAULT_INTERVAL)
    }

    /**
     * Test that an UpdateDefaultRounds task completes in good time
     */
    @Test
    fun testUpdateTime() {
        scenario = composeTestRule.activityRule.scenario

        composeTestRule.mainMenuRobot {
            clickAboutIcon {
                checkRoundStatusMessage("Up to date")
            }
        }
    }
}
