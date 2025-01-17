package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.sightMarks.SightMarksTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction.*
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher.*
import eywa.projectcodex.model.SightMark

class SightMarksShiftAndScaleRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
) : BaseRobot(composeTestRule, SightMarksTestTag.SCREEN) {
    fun checkSightMarkDisplayed(sightMark: SightMark, isLeft: Boolean = false) {
        val text = sightMark.asText(isLeft)

        performSingle {
            useUnmergedTree()
            +HasTestTag(SightMarksTestTag.SIGHT_MARK_TEXT)
            +HasText(text)
            +PerformScrollTo()
            +AssertIsDisplayed()
        }

        performSingle {
            useUnmergedTree()
            +HasTestTag(SightMarksTestTag.DIAGRAM_NOTE_ICON)
            +HasAnySibling(
                    listOf(
                            HasTestTag(SightMarksTestTag.SIGHT_MARK_TEXT),
                            HasText(text),
                    )
            )
            +if (sightMark.note == null) AssertDoesNotExist() else AssertIsDisplayed()
        }
    }

    fun clickComplete() {
        performSingle {
            +HasTestTag(SightMarksTestTag.SAS_COMPLETE_BUTTON)
            +PerformClick()
        }
    }

    fun clickFlip() {
        performSingle {
            +HasTestTag(SightMarksTestTag.SAS_FLIP_BUTTON)
            +PerformClick()
        }
    }

    fun clickScaleReset() {
        performSingle {
            +HasTestTag(SightMarksTestTag.SAS_RESET_BUTTON)
            +HasAnyAncestor(HasTestTag(SightMarksTestTag.SAS_SCALE_BUTTONS))
            +PerformClick()
        }
    }

    fun clickShiftReset() {
        performSingle {
            +HasTestTag(SightMarksTestTag.SAS_RESET_BUTTON)
            +HasAnyAncestor(HasTestTag(SightMarksTestTag.SAS_SHIFT_BUTTONS))
            +PerformClick()
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
        performSingle {
            +HasTestTag(buttonTag)
            +HasAnyAncestor(HasTestTag(groupTestTag))
            +PerformClick()
        }
    }
}
