package eywa.projectcodex.instrumentedTests

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import eywa.projectcodex.core.mainActivity.MainActivity
import org.junit.Rule

class ActivityScenarioTest {
    @get:Rule
    var rule = ActivityScenarioRule(MainActivity::class.java)

    // TODO Compose test rule

    private lateinit var scenario: ActivityScenario<MainActivity>
}
