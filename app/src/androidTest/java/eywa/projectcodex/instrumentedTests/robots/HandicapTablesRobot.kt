package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.TabSwitcherTestTag
import eywa.projectcodex.components.handicapTables.HandicapTablesTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.SelectFaceRobot
import eywa.projectcodex.instrumentedTests.robots.common.SelectRoundRobot
import eywa.projectcodex.model.Handicap

class HandicapTablesRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, HandicapTablesTestTag.SCREEN) {
    val facesRobot = SelectFaceRobot(composeTestRule, HandicapTablesTestTag.SCREEN)
    val roundsRobot = SelectRoundRobot(composeTestRule, HandicapTablesTestTag.SCREEN)

    fun clickClassificationTables(block: ClassificationTablesRobot.() -> Unit = {}) {
        perform {
            +CodexNodeMatcher.HasTestTag(TabSwitcherTestTag.ITEM)
            +CodexNodeMatcher.HasText("Classifications")
            +CodexNodeInteraction.PerformClick
        }
        ClassificationTablesRobot(composeTestRule).apply { block() }
    }

    fun clickHandicapSystem() {
        perform {
            clickDataRow(HandicapTablesTestTag.SYSTEM_SELECTOR)
        }
    }

    fun clickInputMethod() {
        perform {
            clickDataRow(HandicapTablesTestTag.INPUT_SELECTOR)
        }
    }

    fun setInputText(text: String) {
        perform {
            setText(HandicapTablesTestTag.INPUT_TEXT, text)
        }
    }

    fun checkNoDataInTable() {
        perform {
            +CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_EMPTY_TEXT)
            +CodexNodeInteraction.AssertIsDisplayed
        }
    }

    fun checkTableData(data: List<TableRow>) {
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_HANDICAP))
            +CodexNodeGroupInteraction.ForEach(
                    data.map { CodexNodeInteraction.AssertTextEquals(it.handicap.toString()) }
            )
        }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_SCORE))
            +CodexNodeGroupInteraction.ForEach(
                    data.map { CodexNodeInteraction.AssertTextEquals(it.score.toString()) }
            )
        }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(HandicapTablesTestTag.TABLE_ALLOWANCE))
            +CodexNodeGroupInteraction.ForEach(
                    data.map { CodexNodeInteraction.AssertTextEquals(it.allowance.toString()) }
            )
        }
    }

    @JvmInline
    value class TableRow private constructor(val data: Pair<Int, Int>) {
        constructor(handicap: Int, score: Int) : this(handicap to score)

        val handicap
            get() = data.first
        val score
            get() = data.second
        val allowance
            get() = Handicap.fullRoundScoreToAllowance(score)
    }
}
