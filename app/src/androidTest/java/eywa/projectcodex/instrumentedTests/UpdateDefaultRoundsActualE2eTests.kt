package eywa.projectcodex.instrumentedTests

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.R
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.common.CommonStrings
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.click
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.hiltModules.LocalUpdateDefaultRoundsModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UpdateDefaultRoundsActualE2eTests {
    companion object {
        init {
            SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
            LocalUpdateDefaultRoundsModule.useActual = true
        }
    }

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(60)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var rule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    /**
     * Test that an UpdateDefaultRounds task completes in good time
     */
    @Test
    fun testUpdateTime() {
        scenario = rule.scenario

        R.id.action_bar__about.click()
        CustomConditionWaiter.waitForTextToAppear("Up to date", R.id.text_about__update_default_rounds_progress, 0)
    }
}