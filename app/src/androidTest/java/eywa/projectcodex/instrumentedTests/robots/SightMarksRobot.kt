package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.*
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.model.SightMark

internal fun SightMark.asText(isLeft: Boolean = true) =
        listOf(
                sightMark.toString(),
                "-",
                "$distance" + if (isMetric) "m" else "yd",
        )
                .let { if (isLeft) it.asReversed() else it }
                .joinToString(" ")


class SightMarksRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
) : BaseRobot(composeTestRule, SCREEN) {
    fun checkEmptyMessage() {
        checkElementIsDisplayed(NO_SIGHT_MARKS_TEXT)
        checkElementDoesNotExist(DIAGRAM_TICK_LABEL)
    }

    fun clickAdd(block: SightMarkDetailRobot.() -> Unit) {
        clickElement(ADD_BUTTON)
        createRobot(SightMarkDetailRobot::class) {
            checkButtons(true)
            block()
        }
    }

    fun checkSightMarkDisplayed(sightMark: SightMark, isLeft: Boolean = false) {
        val text = sightMark.asText(isLeft)

        performSingle {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(SIGHT_MARK_TEXT)
            +CodexNodeMatcher.HasText(text)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.AssertIsDisplayed()
        }
        performSingle {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(DIAGRAM_NOTE_ICON)
            +CodexNodeMatcher.HasAnySibling(
                    listOf(
                            CodexNodeMatcher.HasTestTag(SIGHT_MARK_TEXT),
                            CodexNodeMatcher.HasText(text),
                    )
            )
            if (sightMark.note == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun clickSightMark(
            sightMark: SightMark,
            isLeft: Boolean = false,
            block: SightMarkDetailRobot.() -> Unit,
    ) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(SIGHT_MARK_TEXT)
            +CodexNodeMatcher.HasText(sightMark.asText(isLeft))
            useUnmergedTree()
            +CodexNodeInteraction.PerformClick()
        }
        createRobot(SightMarkDetailRobot::class) {
            checkInfo(sightMark, false)
            checkButtons(false)
            block()
        }
    }

    fun checkDiagramTickLabelRange(topTick: String, bottomTick: String) {
        performGroup {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(DIAGRAM_TICK_LABEL)
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.AssertTextEquals(topTick)
            }
        }
        performGroup {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(DIAGRAM_TICK_LABEL)
            toSingle(CodexNodeGroupToOne.Last) {
                +CodexNodeInteraction.AssertTextEquals(bottomTick)
            }
        }
    }

    fun flipDiagram() {
        clickOptions()
        clickElement(FLIP_DIAGRAM_MENU_BUTTON)
    }

    fun shiftAndScale(block: SightMarksShiftAndScaleRobot.() -> Unit) {
        clickOptions()
        clickElement(SHIFT_AND_SCALE_MENU_BUTTON)
        createRobot(SightMarksShiftAndScaleRobot::class, block)
    }

    fun archiveAll() {
        clickOptions()
        clickElement(ARCHIVE_MENU_BUTTON)
        clickDialogOk("Archive all")
    }

    private fun clickOptions() {
        clickElement(OPTIONS_BUTTON, scrollTo = true)
    }
}
