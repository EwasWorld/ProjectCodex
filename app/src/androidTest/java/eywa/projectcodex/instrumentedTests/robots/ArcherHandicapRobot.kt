package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchTextBox
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
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.NO_HANDICAPS_MESSAGE)
            if (isShown) +CodexNodeInteraction.AssertIsDisplayed().waitFor()
            else +CodexNodeInteraction.AssertDoesNotExist().waitFor()
        }
    }

    /**
     * Check the "Past:" header appears above the given [rowIndex]
     */
    fun checkPastHeader(rowIndex: Int) {
        performV2Group {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW)
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
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
    }

    fun checkHandicap(index: Int, date: Calendar, handicap: Int) {
        performV2Group {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW_TIME)
            toSingle(CodexNodeGroupToOne.Index(index)) {
                +CodexNodeInteraction.AssertIsDisplayed()
                +CodexNodeInteraction.AssertTextEquals(DateTimeFormat.TIME_24_HOUR.format(date))
            }
        }
        performV2Group {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW_DATE)
            toSingle(CodexNodeGroupToOne.Index(index)) {
                +CodexNodeInteraction.AssertTextEquals(DateTimeFormat.SHORT_DATE.format(date))
            }
        }
        performV2Group {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW_HANDICAP)
            toSingle(CodexNodeGroupToOne.Index(index)) {
                +CodexNodeInteraction.AssertTextEquals(handicap.toString())
            }
        }
    }

    fun clickAdd() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ADD_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
        performV2Single {
            matchTextBox(ArcherHandicapsTestTag.ADD_HANDICAP_VALUE)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun setAddDate(date: Calendar) {
        setDateAndTime(date)
    }

    fun setAddHandicap(handicap: Int, expectedErrorText: String? = null) {
        performV2Single {
            matchTextBox(ArcherHandicapsTestTag.ADD_HANDICAP_VALUE)
            +CodexNodeInteraction.SetText(handicap.toString())
            +CodexNodeInteraction.AssertHasError(expectedErrorText)
        }
    }

    fun clickAddSubmit() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ADD_HANDICAP_SUBMIT)
            +CodexNodeInteraction.PerformClick()
        }
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ADD_HANDICAP_VALUE)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun clickRow(index: Int) {
        performV2Group {
            CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.ROW_LIST_ITEM)
            toSingle(CodexNodeGroupToOne.Index(index)) {
                +CodexNodeInteraction.PerformClick()
            }
        }
    }

    fun checkDeleteShown() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.DELETE_BUTTON)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun clickDelete() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArcherHandicapsTestTag.DELETE_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
    }
}
