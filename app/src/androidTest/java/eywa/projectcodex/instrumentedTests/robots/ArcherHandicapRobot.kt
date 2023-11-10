package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchTextBox
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.TabSwitcherRobot
import java.util.Calendar

class ArcherHandicapRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ArcherHandicapsTestTag.SCREEN), TabSwitcherRobot {
    override val group: TabSwitcherGroup
        get() = TabSwitcherGroup.ARCHER_INFO

    fun checkNoHandicapsMessageShown(isShown: Boolean = true) {
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.NO_HANDICAPS_MESSAGE)
            if (isShown) +CodexNodeInteraction.AssertIsDisplayed().waitFor()
            else +CodexNodeInteraction.AssertDoesNotExist().waitFor()
        }
    }

    /**
     * Check the "Past:" header appears above the given [rowIndex]
     */
    fun checkPastHeader(rowIndex: Int) {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW))
            +CodexNodeGroupToOne.Index(rowIndex)
            +CodexNodeInteraction.Assert(
                    CodexNodeMatcher.HasAnyChild(
                            listOf(
                                    CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.LIST_HEADER),
                                    CodexNodeMatcher.HasText("Past:"),
                            )
                    )
            )
        }
    }

    fun checkHandicap(index: Int, date: Calendar, handicap: Int) {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW_TIME))
            +CodexNodeGroupToOne.Index(index)
            +CodexNodeInteraction.AssertIsDisplayed()
            +CodexNodeInteraction.AssertTextEquals(DateTimeFormat.TIME_24_HOUR.format(date))
        }
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW_DATE))
            +CodexNodeGroupToOne.Index(index)
            +CodexNodeInteraction.AssertTextEquals(DateTimeFormat.SHORT_DATE.format(date))
        }
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW_HANDICAP))
            +CodexNodeGroupToOne.Index(index)
            +CodexNodeInteraction.AssertTextEquals(handicap.toString())
        }
    }

    fun clickAdd() {
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ADD_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            matchTextBox(ArcherHandicapsTestTag.ADD_HANDICAP_VALUE)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun setAddDate(date: Calendar) {
        setDateAndTime(date)
    }

    fun setAddHandicap(handicap: Int, expectedErrorText: String? = null) {
        perform {
            setText(ArcherHandicapsTestTag.ADD_HANDICAP_VALUE, handicap.toString())
            if (expectedErrorText != null) +CodexNodeInteraction.Assert(CodexNodeMatcher.HasError(expectedErrorText))
            else +CodexNodeInteraction.Assert(CodexNodeMatcher.HasNoError)
        }
    }

    fun clickAddSubmit() {
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ADD_HANDICAP_SUBMIT)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ADD_HANDICAP_VALUE)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun clickRow(index: Int) {
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW_LIST_ITEM))
            +CodexNodeGroupToOne.Index(index)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkDeleteShown() {
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.DELETE_BUTTON)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun clickDelete() {
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.DELETE_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
    }
}
