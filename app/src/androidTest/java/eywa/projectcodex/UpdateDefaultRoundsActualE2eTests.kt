package eywa.projectcodex

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.hiltModules.LocalUpdateDefaultRoundsModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UpdateDefaultRoundsActualE2eTests {
    companion object {
        init {
            LocalUpdateDefaultRoundsModule.useActual = true
        }

        @JvmStatic
        @Suppress("unused")
        @BeforeClass
        fun classSetup(): Unit {
            LocalUpdateDefaultRoundsModule.useActual = true
        }

        @JvmStatic
        @Suppress("unused")
        @AfterClass
        fun classTeardown(): Unit {
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
