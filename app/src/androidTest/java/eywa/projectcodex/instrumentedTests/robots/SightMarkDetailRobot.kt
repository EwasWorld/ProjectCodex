package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.ARCHIVED
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.DATE
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.DELETE_BUTTON
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.DISTANCE
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.DISTANCE_ERROR_TEXT
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.DISTANCE_UNIT
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.MARKED
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.NOTE
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.RESET_BUTTON
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.SAVE_BUTTON
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.SCREEN
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.SIGHT
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.SIGHT_ERROR_TEXT
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkInputtedText
import eywa.projectcodex.model.SightMark

class SightMarkDetailRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        sightMark: SightMark? = null,
        previousScreen: BaseRobot,
        addScreenToStack: Boolean = true,
) : BaseRobot(composeTestRule, SCREEN, previousScreen, addScreenToStack) {
    init {
        val isNew = sightMark == null
        if (!isNew) {
            checkInfo(sightMark!!, isNew)
        }
        checkButtons(isNew)
    }

    private fun checkButtons(isNew: Boolean) {
        checkElementIsDisplayed(SAVE_BUTTON)

        listOf(
                RESET_BUTTON,
                DELETE_BUTTON,
        ).forEach {
            if (isNew) checkElementDoesNotExist(it) else checkElementIsDisplayed(it)
        }
    }

    private fun setDistanceUnit(isMetric: Boolean) {
        var actualIsMetric = false
        try {
            checkElementText(DISTANCE_UNIT, "yd")
        }
        catch (e: AssertionError) {
            actualIsMetric = true
        }
        if (actualIsMetric != isMetric) {
            clickElement(DISTANCE_UNIT)
        }
    }

    fun setInfo(
            sightMark: SightMark,
            isArchivedCurrently: Boolean = false,
            isMarkedCurrently: Boolean = false,
    ) {
        setText(SIGHT, sightMark.sightMark.toString())
        setText(DISTANCE, sightMark.distance.toString())
        setDistanceUnit(sightMark.isMetric)

        setChip(MARKED, sightMark.isMarked, isMarkedCurrently)
        setChip(ARCHIVED, sightMark.isArchived, isArchivedCurrently)
        setText(NOTE, sightMark.note ?: "")
    }

    fun checkInfo(
            sightMark: SightMark,
            isNew: Boolean,
            hasSightMarkError: Boolean = false,
            hasDistanceError: Boolean = false,
    ) {
        perform {
            checkInputtedText(SIGHT, sightMark.sightMark.toString())
        }
        SIGHT_ERROR_TEXT.let {
            if (hasSightMarkError) checkElementIsDisplayed(it) else checkElementDoesNotExist(it)
        }

        perform {
            checkInputtedText(DISTANCE, sightMark.distance.toString())
        }
        DISTANCE_ERROR_TEXT.let {
            if (hasDistanceError) checkElementIsDisplayed(it) else checkElementDoesNotExist(it)
        }
        checkElementText(DISTANCE_UNIT, if (sightMark.isMetric) "m" else "yd")

        if (!isNew) {
            checkElementText(DATE, DateTimeFormat.SHORT_DATE.format(sightMark.dateSet), useUnmergedTree = true)
        }

        CustomConditionWaiter.waitForComposeCondition {
            checkCheckboxState(MARKED, sightMark.isMarked, useUnmergedTree = true)
        }
        checkCheckboxState(ARCHIVED, sightMark.isArchived, useUnmergedTree = true)

        perform {
            checkInputtedText(NOTE, sightMark.note ?: "")
        }

        checkButtons(isNew)
    }

    fun clickSave(): SightMarksRobot {
        clickElement(SAVE_BUTTON)
        return popRobot() as SightMarksRobot
    }

    fun clickReset() {
        clickElement(RESET_BUTTON)
    }

    fun clickDelete(): SightMarksRobot {
        clickElement(DELETE_BUTTON)
        clickDialogOk("Delete sight mark")
        return popRobot() as SightMarksRobot
    }
}
