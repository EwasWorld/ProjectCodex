package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHead.addMatch.HeadToHeadAddMatchTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.SightMarkIndicatorRobot

class HeadToHeadAddMatchRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadAddMatchTestTag.SCREEN) {
    val sightMarkIndicatorRobot = SightMarkIndicatorRobot(this, HeadToHeadAddMatchTestTag.SCREEN)

    fun checkPrevious(match: String, result: String) {
        checkElementText(HeadToHeadAddMatchTestTag.PREVIOUS_MATCH_INFO, match)
        checkDataRowContentDescription(HeadToHeadAddMatchTestTag.PREVIOUS_MATCH_RESULT, text = result)
    }

    fun setHeat(heat: String?) {
        clickDataRowValue(HeadToHeadAddMatchTestTag.HEAT)

        if (heat == null) {
            clickDialogCancel("Match:")
        }
        else {
            performSingle {
                +CodexNodeMatcher.HasTestTag(HeadToHeadAddMatchTestTag.HEAT_SELECTOR_DIALOG_ITEM)
                +CodexNodeMatcher.HasText(heat)
                +CodexNodeInteraction.PerformClick().waitFor()
            }
        }

        checkHeat(heat)
    }

    fun checkHeat(heat: String?) {
        checkDataRowValueText(HeadToHeadAddMatchTestTag.HEAT, heat ?: "Not selected")
    }

    fun checkOpponentRank(rank: Int?) {
        checkInputtedText(HeadToHeadAddMatchTestTag.OPPONENT_QUALI_RANK_INPUT, rank?.toString() ?: "")
    }

    fun checkOpponent(opponent: String?) {
        checkInputtedText(HeadToHeadAddMatchTestTag.OPPONENT_INPUT, opponent ?: "")
    }

    fun checkOpponentRankIsError() {
        checkElementIsDisplayed(HeadToHeadAddMatchTestTag.OPPONENT_QUALI_RANK_ERROR)
    }

    fun setIsBye(newValue: Boolean) {
        setChip(HeadToHeadAddMatchTestTag.IS_BYE_CHECKBOX, newValue, !newValue)
    }

    fun checkByeWithSetsWarningShown(isShown: Boolean = true) {
        checkElementIsDisplayedOrDoesNotExist(HeadToHeadAddMatchTestTag.BYE_WITH_SETS_WARNING_TEXT, isShown)
    }

    fun checkIsBye(isBye: Boolean) {
        checkCheckboxState(HeadToHeadAddMatchTestTag.IS_BYE_CHECKBOX, isBye, useUnmergedTree = true)
    }

    fun checkMaxRank(rank: Int?) {
        checkInputtedText(HeadToHeadAddMatchTestTag.MAX_RANK_INPUT, rank?.toString() ?: "")
    }

    fun setMaxRank(rank: Int?) {
        setText(HeadToHeadAddMatchTestTag.MAX_RANK_INPUT, rank?.toString() ?: "")
    }

    fun setOpponent(name: String?, rank: Int?) {
        setText(HeadToHeadAddMatchTestTag.OPPONENT_INPUT, name ?: "")
        setText(HeadToHeadAddMatchTestTag.OPPONENT_QUALI_RANK_INPUT, rank?.toString() ?: "")
    }

    fun clickStartMatch(block: HeadToHeadAddEndRobot.() -> Unit = {}) {
        clickElement(HeadToHeadAddMatchTestTag.SAVE_BUTTON)
        createRobot(HeadToHeadAddEndRobot::class, block)
    }

    fun clickResetEdit() {
        clickElement(HeadToHeadAddMatchTestTag.RESET_BUTTON)
    }

    fun clickSaveEdit() {
        clickElement(HeadToHeadAddMatchTestTag.SAVE_BUTTON)
    }
}
