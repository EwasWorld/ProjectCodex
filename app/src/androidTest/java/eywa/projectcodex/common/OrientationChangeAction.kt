package eywa.projectcodex.common

import android.content.pm.ActivityInfo
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import eywa.projectcodex.components.mainActivity.MainActivity
import org.hamcrest.Matcher


class OrientationChangeAction(
        private val activityScenario: ActivityScenario<MainActivity>,
        private val orientation: Orientation
) : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isRoot()
    }

    override fun getDescription(): String {
        return "rotate to $orientation"
    }

    override fun perform(uiController: UiController?, view: View?) {
        uiController!!.loopMainThreadUntilIdle()
        activityScenario.onActivity {
            it.requestedOrientation = orientation.value
        }
    }

    enum class Orientation(val value: Int) {
        PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT), LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    }
}
