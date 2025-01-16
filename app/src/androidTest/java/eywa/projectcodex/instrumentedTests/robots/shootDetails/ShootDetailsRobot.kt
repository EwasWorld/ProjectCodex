package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.commonUi.StandardBottomNavBarItem
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadBottomNavBarItem
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadAddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadAddHeatRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadScorePadRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadStatsRobot

abstract class ShootDetailsRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: CodexTestTag,
) : BaseRobot(composeTestRule, screenTestTag) {
    inline fun <reified R : ShootDetailsRobot> clickNavBarItem(noinline block: R.() -> Unit = {}) {
        val screenItem = when (R::class) {
            ShootDetailsStatsRobot::class -> StandardBottomNavBarItem.STATS
            ShootDetailsSettingsRobot::class -> StandardBottomNavBarItem.SETTINGS
            ShootDetailsAddEndRobot::class -> StandardBottomNavBarItem.ADD_END
            ShootDetailsScorePadRobot::class -> StandardBottomNavBarItem.SCORE_PAD
            HeadToHeadAddEndRobot::class -> HeadToHeadBottomNavBarItem.ADD_END
            HeadToHeadAddHeatRobot::class -> HeadToHeadBottomNavBarItem.ADD_END
            HeadToHeadScorePadRobot::class -> HeadToHeadBottomNavBarItem.SCORE_PAD
            HeadToHeadStatsRobot::class -> HeadToHeadBottomNavBarItem.STATS
            else -> throw NotImplementedError()
        }

        checkElementIsDisplayed(screenItem)
        clickElement(screenItem)
        createRobot(R::class, block)
    }
}
