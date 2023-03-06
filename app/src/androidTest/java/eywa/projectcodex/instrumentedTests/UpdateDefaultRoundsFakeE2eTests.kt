package eywa.projectcodex.instrumentedTests

import android.util.Log
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
    companion object {
        init {
            SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
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
        Log.i("UpdateFakeTest", "Point a")
        hiltRule.inject()
        Log.i("UpdateFakeTest", "Point b")
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
        Log.i("UpdateFakeTest", "Point 1")
        scenario = rule.scenario
        Log.i("UpdateFakeTest", "Point 2")
        assertEquals(1, LocalUpdateDefaultRoundsModule.mockedTask!!.runTaskCalls)
        Log.i("UpdateFakeTest", "Point 3")
    }
}
