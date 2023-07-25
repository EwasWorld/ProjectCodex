package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.handicapTables.HandicapTablesTestTag
import eywa.projectcodex.core.mainActivity.MainActivity

class HandicapTablesRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, HandicapTablesTestTag.SCREEN)
