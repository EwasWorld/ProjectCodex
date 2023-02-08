package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.mainActivity.MainActivity

fun ComposeTestRule<MainActivity>.mainMenuRobot(block: MainMenuRobot.() -> Unit) {
    MainMenuRobot(this).apply { block() }
}

class AppRobot {

}
