package eywa.projectcodex.instrumentedTests.robots

import androidx.test.espresso.Espresso
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.*
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkInputtedText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.model.SightMark

class SightMarkDetailRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
) : BaseRobot(composeTestRule, SCREEN) {
    fun checkButtons(isNew: Boolean) {
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
            perform {
                singleNode {
                    +CodexNodeMatcher.HasTestTag(DISTANCE_UNIT)
                    +CodexNodeInteraction.AssertTextEquals("yd")
                }
            }
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
        Espresso.closeSoftKeyboard()
        setDistanceUnit(sightMark.isMetric)

        setChip(MARKED, sightMark.isMarked, isMarkedCurrently)
        setChip(ARCHIVED, sightMark.isArchived, isArchivedCurrently)
        setText(NOTE, sightMark.note ?: "")
        Espresso.closeSoftKeyboard()
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

        checkDistance(sightMark.distance, sightMark.isMetric, hasDistanceError)

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

    fun checkDistance(
            distance: Int?,
            isMetric: Boolean,
            hasDistanceError: Boolean = false,
    ) {
        perform {
            checkInputtedText(DISTANCE, distance.toString())
        }
        DISTANCE_ERROR_TEXT.let {
            if (hasDistanceError) checkElementIsDisplayed(it) else checkElementDoesNotExist(it)
        }
        checkElementText(DISTANCE_UNIT, if (isMetric) "m" else "yd")

    }

    fun clickSave() {
        clickElement(SAVE_BUTTON)
    }

    fun clickReset() {
        clickElement(RESET_BUTTON)
    }

    fun clickDelete() {
        clickElement(DELETE_BUTTON)
        clickDialogOk("Delete sight mark")
    }
}
