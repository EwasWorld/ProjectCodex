package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.sightMarks.SightMarksTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction.AssertDoesNotExist
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction.AssertIsDisplayed
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction.PerformClick
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher.HasAnyAncestor
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher.HasAnySibling
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher.HasTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher.HasText
import eywa.projectcodex.model.SightMark

class SightMarksShiftAndScaleRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        previousScreen: BaseRobot,
        addScreenToStack: Boolean = true,
) : BaseRobot(composeTestRule, SightMarksTestTag.SCREEN, previousScreen, addScreenToStack) {
    fun checkSightMarkDisplayed(sightMark: SightMark, isLeft: Boolean = false) {
        val text = sightMark.asText(isLeft)

        perform {
            useUnmergedTree = true
            +HasTestTag(SightMarksTestTag.SIGHT_MARK_TEXT)
            +HasText(text)
            +AssertIsDisplayed
        }

        perform {
            useUnmergedTree = true
            +HasTestTag(SightMarksTestTag.DIAGRAM_NOTE_ICON)
            +HasAnySibling(
                    listOf(
                            HasTestTag(SightMarksTestTag.SIGHT_MARK_TEXT),
                            HasText(text),
                    )
            )
            +if (sightMark.note == null) AssertDoesNotExist else AssertIsDisplayed
        }
    }

    fun clickComplete(): SightMarksRobot {
        perform {
            +HasTestTag(SightMarksTestTag.SAS_COMPLETE_BUTTON)
            +PerformClick
        }
        return popRobot()
    }

    fun clickFlip() {
        perform {
            +HasTestTag(SightMarksTestTag.SAS_FLIP_BUTTON)
            +PerformClick
        }
    }

    fun clickScaleReset() {
        perform {
            +HasTestTag(SightMarksTestTag.SAS_RESET_BUTTON)
            +HasAnyAncestor(HasTestTag(SightMarksTestTag.SAS_SCALE_BUTTONS))
            +PerformClick
        }
    }

    fun clickShiftReset() {
        perform {
            +HasTestTag(SightMarksTestTag.SAS_RESET_BUTTON)
            +HasAnyAncestor(HasTestTag(SightMarksTestTag.SAS_SHIFT_BUTTONS))
            +PerformClick
        }
    }

    fun clickScaleChange(isIncrease: Boolean, isLarge: Boolean) {
        clickShifterButton(isIncrease, isLarge, SightMarksTestTag.SAS_SCALE_BUTTONS)
    }

    fun clickShiftChange(isIncrease: Boolean, isLarge: Boolean) {
        clickShifterButton(isIncrease, isLarge, SightMarksTestTag.SAS_SHIFT_BUTTONS)
    }

    private fun clickShifterButton(isIncrease: Boolean, isLarge: Boolean, groupTestTag: CodexTestTag) {
        val buttonTag = when {
            isIncrease && isLarge -> SightMarksTestTag.SAS_LARGE_INCREASE_BUTTON
            isIncrease -> SightMarksTestTag.SAS_SMALL_INCREASE_BUTTON
            isLarge -> SightMarksTestTag.SAS_LARGE_DECREASE_BUTTON
            else -> SightMarksTestTag.SAS_SMALL_DECREASE_BUTTON
        }
        perform {
            +HasTestTag(buttonTag)
            +HasAnyAncestor(HasTestTag(groupTestTag))
            +PerformClick
        }
    }
}
