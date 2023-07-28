package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.core.mainActivity.MainActivity

fun ComposeTestRule<MainActivity>.mainMenuRobot(block: MainMenuRobot.() -> Unit) {
    MainMenuRobot(this).apply {
        try {
            clickCancelWhatsNewDialog()
        }
        catch (_: AssertionError) {
        }

        block()
    }
}

class AppRobot {

}
