package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.sightMarks.SightMarksTestTag.*
import eywa.projectcodex.model.SightMark

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

    private fun SightMark.asText(isLeft: Boolean = true) =
            listOf(
                    sightMark.toString(),
                    "-",
                    "$distance" + if (isMetric) "m" else "yd",
            )
                    .let { if (isLeft) it.asReversed() else it }
                    .joinToString(" ")

    fun checkSightMarkDisplayed(sightMark: SightMark, isLeft: Boolean = false) {
        val text = sightMark.asText(isLeft)

        checkElementIsDisplayed(SIGHT_MARK_TEXT, text, true)

        composeTestRule.onNode(
                hasTestTag(DIAGRAM_NOTE_ICON.getTestTag())
                        .and(hasAnySibling(hasTestTag(SIGHT_MARK_TEXT.getTestTag()).and(hasText(text)))),
                true,
        ).let {
            if (sightMark.note == null) it.assertDoesNotExist() else it.assertIsDisplayed()
        }
    }

    fun clickSightMark(sightMark: SightMark, isLeft: Boolean = false): SightMarkDetailRobot {
        clickElement(
                SIGHT_MARK_TEXT,
                sightMark.asText(isLeft),
                true,
        )
        return SightMarkDetailRobot(composeTestRule, sightMark, this, true)
    }

    fun checkDiagramTickLabelRange(topTick: String, bottomTick: String) {
        checkElementText(DIAGRAM_TICK_LABEL, 0, topTick, useUnmergedTree = true)
        checkLastElementText(DIAGRAM_TICK_LABEL, bottomTick, useUnmergedTree = true)
    }

    fun flipDiagram() {
        clickOptions()
        clickElement(FLIP_DIAGRAM_MENU_BUTTON)
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
