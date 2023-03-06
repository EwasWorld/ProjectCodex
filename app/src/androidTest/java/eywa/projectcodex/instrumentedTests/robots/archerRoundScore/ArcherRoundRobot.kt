package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.archerRoundScore.ArcherRoundMainScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen.*
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.BaseRobot

abstract class ArcherRoundRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: String? = null,
) : BaseRobot(composeTestRule, screenTestTag) {
    fun clickCannotInputMoreEndsOk() = clickDialogOk(CANNOT_INPUT_END_DIALOG_TITLE)

    fun clickNavBarInputEnd(block: InputEndRobot.() -> Unit = {}) = clickNavBarItem(block)

    fun clickNavBarInputEndWhileRoundComplete() {
        composeTestRule
                .onNodeWithTag(ArcherRoundMainScreen.TestTag.bottomNavBarItem(INPUT_END))
                .performClick()
        checkElementDoesNotExist(ArrowInputsTestTag.INPUT_SCREEN)
        clickCannotInputMoreEndsOk()
    }

    fun clickNavBarScorePad(block: ScorePadRobot.() -> Unit = {}) = clickNavBarItem(block)
    fun clickNavBarStats(block: ArcherRoundStatsRobot.() -> Unit = {}) = clickNavBarItem(block)
    fun clickNavBarSettings(block: ArcherRoundSettingsRobot.() -> Unit = {}) = clickNavBarItem(block)

    private inline fun <reified R : ArcherRoundRobot> clickNavBarItem(block: R.() -> Unit = {}) {
        val screen = when (R::class) {
            ArcherRoundStatsRobot::class -> STATS
            ArcherRoundSettingsRobot::class -> SETTINGS
            InputEndRobot::class -> INPUT_END
            ScorePadRobot::class -> SCORE_PAD
            else -> throw NotImplementedError()
        }

        composeTestRule
                .onNodeWithTag(ArcherRoundMainScreen.TestTag.bottomNavBarItem(screen))
                .performClick()
        R::class.constructors.first().call(composeTestRule).apply { block() }
    }

    companion object {
        private const val CANNOT_INPUT_END_DIALOG_TITLE = "Round is complete"
    }
}
