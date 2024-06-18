package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsBottomNavBarItem
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.BaseRobot

abstract class ShootDetailsRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: CodexTestTag,
) : BaseRobot(composeTestRule, screenTestTag) {
    private fun clickCannotInputMoreEndsOk() = clickDialogOk(CANNOT_INPUT_END_DIALOG_TITLE)

    fun clickNavBarAddEnd(block: AddEndRobot.() -> Unit = {}) = clickNavBarItem(block)

    fun clickNavBarAddEndWhileRoundComplete() {
        clickElement(ShootDetailsBottomNavBarItem.ADD_END)
        clickCannotInputMoreEndsOk()
    }

    fun clickNavBarScorePad(block: ScorePadRobot.() -> Unit = {}) = clickNavBarItem(block)
    fun clickNavBarStats(block: ShootDetailsStatsRobot.() -> Unit = {}) = clickNavBarItem(block)
    fun clickNavBarSettings(block: ShootDetailsSettingsRobot.() -> Unit = {}) = clickNavBarItem(block)

    private inline fun <reified R : ShootDetailsRobot> clickNavBarItem(noinline block: R.() -> Unit = {}) {
        val screenItem = when (R::class) {
            ShootDetailsStatsRobot::class -> ShootDetailsBottomNavBarItem.STATS
            ShootDetailsSettingsRobot::class -> ShootDetailsBottomNavBarItem.SETTINGS
            AddEndRobot::class -> ShootDetailsBottomNavBarItem.ADD_END
            ScorePadRobot::class -> ShootDetailsBottomNavBarItem.SCORE_PAD
            else -> throw NotImplementedError()
        }

        clickElement(screenItem)
        createRobot(R::class, block)
    }

    companion object {
        private const val CANNOT_INPUT_END_DIALOG_TITLE = "Round is complete"
    }
}
