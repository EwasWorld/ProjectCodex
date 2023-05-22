package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.sightMarks.SightMarksTestTag
import eywa.projectcodex.model.SightMark

class SightMarksRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        previousScreen: BaseRobot,
        addScreenToStack: Boolean = true,
) : BaseRobot(composeTestRule, SightMarksTestTag.SCREEN, previousScreen, addScreenToStack) {
    fun checkEmptyMessage() {
        checkElementIsDisplayed(SightMarksTestTag.NO_SIGHT_MARKS_TEXT)
        checkElementDoesNotExist(SightMarksTestTag.DIAGRAM_TICK_LABEL)
    }

    fun clickAdd(): SightMarkDetailRobot {
        clickElement(SightMarksTestTag.ADD_BUTTON)
        return SightMarkDetailRobot(composeTestRule, null, this)
    }

    private fun SightMark.asText(isLeft: Boolean = true) =
            listOf(
                    sightMark.toString(),
                    "-",
                    "$distance" + if (isMetric) "m" else "yd",
            )
                    .let { if (!isMetric) it.asReversed() else it }
                    .joinToString(" ")

    fun checkSightMarkDisplayed(sightMark: SightMark, isLeft: Boolean = false) {
        checkElementIsDisplayed(
                SightMarksTestTag.SIGHT_MARK_TEXT,
                sightMark.asText(isLeft),
                true,
        )
    }

    fun clickSightMark(sightMark: SightMark, isLeft: Boolean = true): SightMarkDetailRobot {
        clickElement(
                SightMarksTestTag.SIGHT_MARK_TEXT,
                sightMark.asText(isLeft),
                true,
        )
        return SightMarkDetailRobot(composeTestRule, sightMark, this, true)
    }

    fun checkDiagramTickLabelRange(topTick: String, bottomTick: String) {
        checkElementText(SightMarksTestTag.DIAGRAM_TICK_LABEL, 0, topTick)
        checkLastElementText(SightMarksTestTag.DIAGRAM_TICK_LABEL, bottomTick)
    }

    fun flipDiagram() {
        clickOptions()
        clickElement(SightMarksTestTag.FLIP_DIAGRAM_MENU_BUTTON)
    }

    fun archiveAll() {
        clickOptions()
        clickElement(SightMarksTestTag.ARCHIVE_MENU_BUTTON)
    }

    private fun clickOptions() {
        clickElement(SightMarksTestTag.OPTIONS_BUTTON)
    }
}
