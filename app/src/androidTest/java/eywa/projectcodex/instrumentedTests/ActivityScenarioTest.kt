package eywa.projectcodex.instrumentedTests

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import eywa.projectcodex.common.CommonStrings
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.components.mainActivity.MainActivity
import org.junit.Rule

class ActivityScenarioTest {
    init {
        SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
    }

    @get:Rule
    var rule = ActivityScenarioRule(MainActivity::class.java)

    // TODO Compose test rule

    private lateinit var scenario: ActivityScenario<MainActivity>
}