package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.sightMarks.SightMarksTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.utils.CodexNodeAction.*
import eywa.projectcodex.instrumentedTests.utils.CodexNodeMatcher.*
import eywa.projectcodex.instrumentedTests.utils.CodexNodeOptions
import eywa.projectcodex.model.SightMark

class SightMarksShiftAndScaleRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        previousScreen: BaseRobot,
        addScreenToStack: Boolean = true,
) : BaseRobot(composeTestRule, SightMarksTestTag.SCREEN, previousScreen, addScreenToStack) {
    fun checkSightMarkDisplayed(sightMark: SightMark, isLeft: Boolean = false) {
        val text = sightMark.asText(isLeft)

        perform(
                action = AssertIsDisplayed,
                options = listOf(CodexNodeOptions.UseUnmergedTree),
                HasTestTag(SightMarksTestTag.SIGHT_MARK_TEXT),
                HasText(text),
        )
        perform(
                action = if (sightMark.note == null) AssertDoesNotExist else AssertIsDisplayed,
                options = listOf(CodexNodeOptions.UseUnmergedTree),
                HasTestTag(SightMarksTestTag.DIAGRAM_NOTE_ICON),
                AnySibling(
                        listOf(
                                HasTestTag(SightMarksTestTag.SIGHT_MARK_TEXT),
                                HasText(text),
                        )
                ),
        )
    }

    fun clickComplete(): SightMarksRobot {
        perform(
                action = PerformClick,
                HasTestTag(SightMarksTestTag.SAS_COMPLETE_BUTTON),
        )
        return popRobot()
    }

    fun clickFlip() {
        perform(
                action = PerformClick,
                HasTestTag(SightMarksTestTag.SAS_FLIP_BUTTON),
        )
    }

    fun clickScaleReset() {
        perform(
                action = PerformClick,
                HasTestTag(SightMarksTestTag.SAS_RESET_BUTTON),
                AnyAncestor(HasTestTag(SightMarksTestTag.SAS_SCALE_BUTTONS)),
        )
    }

    fun clickShiftReset() {
        perform(
                action = PerformClick,
                HasTestTag(SightMarksTestTag.SAS_RESET_BUTTON),
                AnyAncestor(HasTestTag(SightMarksTestTag.SAS_SHIFT_BUTTONS)),
        )
    }

    fun clickScaleChange(isIncrease: Boolean, isLarge: Boolean) {
        perform(
                action = PerformClick,
                HasTestTag(getShifterTestTag(isIncrease, isLarge)),
                AnyAncestor(HasTestTag(SightMarksTestTag.SAS_SCALE_BUTTONS)),
        )
    }

    fun clickShiftChange(isIncrease: Boolean, isLarge: Boolean) {
        perform(
                action = PerformClick,
                HasTestTag(getShifterTestTag(isIncrease, isLarge)),
                AnyAncestor(HasTestTag(SightMarksTestTag.SAS_SHIFT_BUTTONS)),
        )
    }

    private fun getShifterTestTag(isIncrease: Boolean, isLarge: Boolean) = when {
        isIncrease && isLarge -> SightMarksTestTag.SAS_LARGE_INCREASE_BUTTON
        isIncrease -> SightMarksTestTag.SAS_SMALL_INCREASE_BUTTON
        isLarge -> SightMarksTestTag.SAS_LARGE_DECREASE_BUTTON
        else -> SightMarksTestTag.SAS_SMALL_DECREASE_BUTTON
    }
}
