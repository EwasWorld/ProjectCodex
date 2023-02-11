package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.handicapTables.HandicapTablesFragment
import eywa.projectcodex.components.mainActivity.MainActivity

class HandicapTablesRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, HandicapTablesFragment::class)
