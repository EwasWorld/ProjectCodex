package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.ADD_BUTTON
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.ARCHIVE_MENU_BUTTON
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.DIAGRAM_NOTE_ICON
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.DIAGRAM_TICK_LABEL
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.FLIP_DIAGRAM_MENU_BUTTON
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.NO_SIGHT_MARKS_TEXT
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.OPTIONS_BUTTON
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.SCREEN
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.SHIFT_AND_SCALE_MENU_BUTTON
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.SIGHT_MARK_TEXT
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
        previousScreen: BaseRobot,
        addScreenToStack: Boolean = true,
) : BaseRobot(composeTestRule, SCREEN, previousScreen, addScreenToStack) {
    fun checkEmptyMessage() {
        checkElementIsDisplayed(NO_SIGHT_MARKS_TEXT)
        checkElementDoesNotExist(DIAGRAM_TICK_LABEL)
    }

    fun clickAdd(): SightMarkDetailRobot {
        clickElement(ADD_BUTTON)
        return SightMarkDetailRobot(composeTestRule, null, this)
    }

    fun checkSightMarkDisplayed(sightMark: SightMark, isLeft: Boolean = false) {
        val text = sightMark.asText(isLeft)

        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(SIGHT_MARK_TEXT)
            +CodexNodeMatcher.HasText(text)
            +CodexNodeInteraction.AssertIsDisplayed
        }
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(DIAGRAM_NOTE_ICON)
            +CodexNodeMatcher.HasAnySibling(
                    listOf(
                            CodexNodeMatcher.HasTestTag(SIGHT_MARK_TEXT),
                            CodexNodeMatcher.HasText(text),
                    )
            )
            +if (sightMark.note == null) CodexNodeInteraction.AssertDoesNotExist else CodexNodeInteraction.AssertIsDisplayed
        }
    }

    fun clickSightMark(sightMark: SightMark, isLeft: Boolean = false): SightMarkDetailRobot {
        clickElement(
                SIGHT_MARK_TEXT,
                sightMark.asText(isLeft),
                useUnmergedTree = true,
        )
        return SightMarkDetailRobot(composeTestRule, sightMark, this, true)
    }

    fun checkDiagramTickLabelRange(topTick: String, bottomTick: String) {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(DIAGRAM_TICK_LABEL))
            +CodexNodeGroupToOne.First
            +CodexNodeInteraction.AssertTextEquals(topTick)
        }
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(DIAGRAM_TICK_LABEL))
            +CodexNodeGroupToOne.Last
            +CodexNodeInteraction.AssertTextEquals(bottomTick)
        }
    }

    fun flipDiagram() {
        clickOptions()
        clickElement(FLIP_DIAGRAM_MENU_BUTTON)
    }

    fun shiftAndScale(): SightMarksShiftAndScaleRobot {
        clickOptions()
        clickElement(SHIFT_AND_SCALE_MENU_BUTTON)
        return SightMarksShiftAndScaleRobot(composeTestRule, this, true)
    }

    fun archiveAll() {
        clickOptions()
        clickElement(ARCHIVE_MENU_BUTTON)
        clickDialogOk("Archive all")
    }

    private fun clickOptions() {
        clickElement(OPTIONS_BUTTON)
    }
}
