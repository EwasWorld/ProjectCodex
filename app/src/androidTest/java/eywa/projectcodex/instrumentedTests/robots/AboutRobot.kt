package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.about.AboutFragment
import eywa.projectcodex.components.mainActivity.MainActivity

class AboutRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, AboutFragment::class)
