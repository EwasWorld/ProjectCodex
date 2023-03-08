package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.CommonStrings
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.hiltModules.LocalUpdateDefaultRoundsModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import org.junit.*
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UpdateDefaultRoundsActualE2eTests {
    companion object {
        init {
            SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
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
    val testTimeout: Timeout = Timeout.seconds(60)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var rule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    /**
     * Test that an UpdateDefaultRounds task completes in good time
     */
    @Test
    fun testUpdateTime() {
        scenario = rule.scenario

        composeTestRule.mainMenuRobot {
            clickAboutIcon {
                checkRoundStatusMessage("Up to date")
            }
        }
    }
}
