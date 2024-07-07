package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.components.handicapTables.HandicapTablesTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkInputtedText
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.TabSwitcherRobot
import eywa.projectcodex.instrumentedTests.robots.selectFace.SelectFaceBaseRobot
import eywa.projectcodex.instrumentedTests.robots.selectRound.SelectRoundBaseRobot
import eywa.projectcodex.model.Handicap

class HandicapTablesRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, HandicapTablesTestTag.SCREEN), TabSwitcherRobot {
    override val group: TabSwitcherGroup
        get() = TabSwitcherGroup.REFERENCES

    val selectFaceBaseRobot = SelectFaceBaseRobot(::performV2)
    val selectRoundsRobot = SelectRoundBaseRobot(::performV2)

    fun clickHandicapSystem() {
        perform {
            clickDataRow(HandicapTablesTestTag.SYSTEM_SELECTOR)
        }
    }

    fun clickInputMethod() {
        perform {
            +CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.INPUT_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkInputMethod(isHandicap: Boolean) {
        perform {
            +CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.INPUT_SELECTOR)
            +CodexNodeInteraction.AssertTextEquals(if (isHandicap) "Handicap:" else "Score:")
        }
    }

    fun setInputText(text: String) {
        perform {
            setText(HandicapTablesTestTag.INPUT_TEXT, text)
        }
    }

    fun checkInputText(text: String) {
        perform {
            checkInputtedText(HandicapTablesTestTag.INPUT_TEXT, text)
        }
    }

    fun checkNoDataInTable() {
        perform {
            +CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_EMPTY_TEXT)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun checkTableData(data: List<TableRow>) {
        fun getChecks(value: Int, suffix: String) =
                listOf(
                        CodexNodeInteraction.AssertContentDescriptionEquals("$value $suffix"),
                        CodexNodeInteraction.AssertTextEquals(value.toString()),
                )

        perform {
            allNodes(CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_HANDICAP))
            +CodexNodeGroupInteraction.ForEach(data.map { getChecks(it.handicap, "Handicap") })
        }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_SCORE))
            +CodexNodeGroupInteraction.ForEach(data.map { getChecks(it.score, "Score") })
        }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_ALLOWANCE))
            +CodexNodeGroupInteraction.ForEach(data.map { getChecks(it.allowance, "Allowance") })
        }
    }

    fun clickToggleSimple() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.SIMPLE_TOGGLE)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkIsSimpleView() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_AVERAGE_END)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun checkIsAdvancedView() {
        performV2Group {
            +CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_AVERAGE_END)
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.AssertIsDisplayed()
            }
        }
    }

    data class TableRow(
            val handicap: Int,
            val score: Int,
    ) {
        val allowance
            get() = Handicap.fullRoundScoreToAllowance(score)
    }
}
