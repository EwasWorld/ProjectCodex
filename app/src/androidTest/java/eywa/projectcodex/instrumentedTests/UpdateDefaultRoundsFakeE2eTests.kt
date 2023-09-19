package eywa.projectcodex.instrumentedTests

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.hiltModules.LocalUpdateDefaultRoundsModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UpdateDefaultRoundsFakeE2eTests {
    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

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
     * Test that an UpdateDefaultRounds task is started when the activity is started
     */
    @Test
    fun testUpdateIsCalledOnActivityStart() {
        scenario = rule.scenario
        assertEquals(1, LocalUpdateDefaultRoundsModule.mockedTask!!.runTaskCalls)
    }
}
