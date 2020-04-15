package eywa.projectcodex

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import eywa.projectcodex.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A quick test to make sure that the default_round_data parses
 */
@RunWith(AndroidJUnit4::class)
class DefaultRoundsJsonUnitTest {
    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)

    @Test
    fun parseDefaultRounds() {
        roundsFromJson(activity.activity.resources.openRawResource(R.raw.default_rounds_data).bufferedReader()
                               .use { it.readText() })
    }
}