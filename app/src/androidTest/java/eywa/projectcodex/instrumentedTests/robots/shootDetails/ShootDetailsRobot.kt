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

abstract class ShootDetailsRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: CodexTestTag,
) : BaseRobot(composeTestRule, screenTestTag) {
    @Deprecated("Use clickNavBarItem instead", ReplaceWith(""))
    fun clickNavBarAddEnd(block: AddEndRobot.() -> Unit = {}) = clickNavBarItem(block)

    @Deprecated("Use clickNavBarItem instead", ReplaceWith(""))
    fun clickNavBarScorePad(block: ScorePadRobot.() -> Unit = {}) = clickNavBarItem(block)

    @Deprecated("Use clickNavBarItem instead", ReplaceWith(""))
    fun clickNavBarStats(block: ShootDetailsStatsRobot.() -> Unit = {}) = clickNavBarItem(block)

    @Deprecated("Use clickNavBarItem instead", ReplaceWith(""))
    fun clickNavBarSettings(block: ShootDetailsSettingsRobot.() -> Unit = {}) = clickNavBarItem(block)

    inline fun <reified R : ShootDetailsRobot> clickNavBarItem(noinline block: R.() -> Unit = {}) {
        val screenItem = when (R::class) {
            ShootDetailsStatsRobot::class -> StandardBottomNavBarItem.STATS
            ShootDetailsSettingsRobot::class -> StandardBottomNavBarItem.SETTINGS
            AddEndRobot::class -> StandardBottomNavBarItem.ADD_END
            ScorePadRobot::class -> StandardBottomNavBarItem.SCORE_PAD
            HeadToHeadAddEndRobot::class -> HeadToHeadBottomNavBarItem.ADD_END
            HeadToHeadAddHeatRobot::class -> HeadToHeadBottomNavBarItem.ADD_HEAT
            HeadToHeadScorePadRobot::class -> HeadToHeadBottomNavBarItem.SCORE_PAD
            else -> throw NotImplementedError()
        }

        clickElement(screenItem)
        createRobot(R::class, block)
    }
}
