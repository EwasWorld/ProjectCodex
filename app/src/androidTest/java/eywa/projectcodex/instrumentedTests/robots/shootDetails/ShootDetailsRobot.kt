package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsBottomNavBarItem
import eywa.projectcodex.components.shootDetails.commonUi.StandardBottomNavBarItem
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.BaseRobot

abstract class ShootDetailsRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: CodexTestTag,
) : BaseRobot(composeTestRule, screenTestTag) {
    fun clickNavBarAddEnd(block: AddEndRobot.() -> Unit = {}) = clickNavBarItem(block)

    fun clickNavBarScorePad(block: ScorePadRobot.() -> Unit = {}) = clickNavBarItem(block)
    fun clickNavBarStats(block: ShootDetailsStatsRobot.() -> Unit = {}) = clickNavBarItem(block)
    fun clickNavBarSettings(block: ShootDetailsSettingsRobot.() -> Unit = {}) = clickNavBarItem(block)

    private inline fun <reified R : ShootDetailsRobot> clickNavBarItem(noinline block: R.() -> Unit = {}) {
        val screenItem = when (R::class) {
            ShootDetailsStatsRobot::class -> StandardBottomNavBarItem.STATS
            ShootDetailsSettingsRobot::class -> StandardBottomNavBarItem.SETTINGS
            AddEndRobot::class -> StandardBottomNavBarItem.ADD_END
            ScorePadRobot::class -> StandardBottomNavBarItem.SCORE_PAD
            else -> throw NotImplementedError()
        }

        clickElement(screenItem)
        createRobot(R::class, block)
    }
}
