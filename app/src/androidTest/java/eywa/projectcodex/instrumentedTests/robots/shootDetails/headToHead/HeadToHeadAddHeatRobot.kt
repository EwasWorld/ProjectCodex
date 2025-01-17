package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.SightMarkIndicatorRobot

class HeadToHeadAddHeatRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadAddHeatTestTag.SCREEN) {
    val sightMarkIndicatorRobot = SightMarkIndicatorRobot(this, HeadToHeadAddHeatTestTag.SCREEN)

    fun checkPrevious(match: String, result: String) {
        checkElementText(HeadToHeadAddHeatTestTag.PREVIOUS_MATCH_INFO, match)
        checkDataRowContentDescription(HeadToHeadAddHeatTestTag.PREVIOUS_MATCH_RESULT, text = result)
    }

    fun setHeat(heat: String?) {
        clickDataRowValue(HeadToHeadAddHeatTestTag.HEAT)

        if (heat == null) {
            clickDialogCancel("Match:")
        }
        else {
            performSingle {
                +CodexNodeMatcher.HasTestTag(HeadToHeadAddHeatTestTag.HEAT_SELECTOR_DIALOG_ITEM)
                +CodexNodeMatcher.HasText(heat)
                +CodexNodeInteraction.PerformClick().waitFor()
            }
        }

        checkHeat(heat)
    }

    fun checkHeat(heat: String?) {
        checkDataRowValueText(HeadToHeadAddHeatTestTag.HEAT, heat ?: "Not selected")
    }

    fun checkOpponentRank(rank: Int?) {
        checkInputtedText(HeadToHeadAddHeatTestTag.OPPONENT_QUALI_RANK_INPUT, rank?.toString() ?: "")
    }

    fun checkOpponentRankIsError() {
        checkElementIsDisplayed(HeadToHeadAddHeatTestTag.OPPONENT_QUALI_RANK_ERROR)
    }

    fun setIsBye(newValue: Boolean) {
        setChip(HeadToHeadAddHeatTestTag.IS_BYE_CHECKBOX, newValue, !newValue)
    }

    fun checkIsBye(isBye: Boolean) {
        checkCheckboxState(HeadToHeadAddHeatTestTag.IS_BYE_CHECKBOX, isBye, useUnmergedTree = true)
    }

    fun checkMaxRank(rank: Int?) {
        checkInputtedText(HeadToHeadAddHeatTestTag.MAX_RANK_INPUT, rank?.toString() ?: "")
    }

    fun setMaxRank(rank: Int?) {
        setText(HeadToHeadAddHeatTestTag.MAX_RANK_INPUT, rank?.toString() ?: "")
    }

    fun setOpponent(name: String?, rank: Int?) {
        setText(HeadToHeadAddHeatTestTag.OPPONENT_INPUT, name ?: "")
        setText(HeadToHeadAddHeatTestTag.OPPONENT_QUALI_RANK_INPUT, rank?.toString() ?: "")
    }

    fun clickStartMatch(block: HeadToHeadAddEndRobot.() -> Unit = {}) {
        clickElement(HeadToHeadAddHeatTestTag.SAVE_BUTTON)
        createRobot(HeadToHeadAddEndRobot::class, block)
    }
}
