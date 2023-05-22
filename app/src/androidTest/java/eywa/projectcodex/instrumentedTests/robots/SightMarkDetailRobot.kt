package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.*
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

    fun setInfo(sightMark: SightMark) {
        setText(SIGHT, sightMark.sightMark.toString())
        setText(DISTANCE, sightMark.distance.toString())
        setDistanceUnit(sightMark.isMetric)

        setChip(MARKED, sightMark.isMarked)
        setChip(ARCHIVED, sightMark.isArchived)
        setText(NOTE, sightMark.note ?: "")
    }

    fun checkInfo(
            sightMark: SightMark,
            isNew: Boolean,
            hasSightMarkError: Boolean = false,
            hasDistanceError: Boolean = false,
    ) {
        checkElementText(SIGHT, sightMark.sightMark.toString())
        SIGHT_ERROR_TEXT.let {
            if (hasSightMarkError) checkElementIsDisplayed(it) else checkElementDoesNotExist(it)
        }

        checkElementText(DISTANCE, sightMark.distance.toString())
        DISTANCE_ERROR_TEXT.let {
            if (hasDistanceError) checkElementIsDisplayed(it) else checkElementDoesNotExist(it)
        }
        checkElementText(DISTANCE_UNIT, if (sightMark.isMetric) "m" else "yd")

        if (!isNew) {
            checkElementText(DATE, DateTimeFormat.SHORT_DATE.format(sightMark.dateSet))
        }

        checkCheckboxState(MARKED, sightMark.isMarked)
        checkCheckboxState(ARCHIVED, sightMark.isArchived)

        checkElementText(NOTE, sightMark.note ?: "", true)


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
        return popRobot() as SightMarksRobot
    }
}
