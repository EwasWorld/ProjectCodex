package eywa.projectcodex.instrumentedTests.robots.referenceTables

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadReferenceTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.common.TabSwitcherRobot

class HeadToHeadReferenceRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
) : BaseRobot(composeTestRule, HeadToHeadReferenceTestTag.SCREEN), TabSwitcherRobot {
    override val group: TabSwitcherGroup
        get() = TabSwitcherGroup.REFERENCES

    fun setArcherARank(rank: Int) {
        performV2 {
            setText(HeadToHeadReferenceTestTag.ARCHER_RANK, rank.toString())
        }
    }

    fun setArcherBRank(rank: Int) {
        performV2 {
            setText(HeadToHeadReferenceTestTag.OPPONENT_RANK, rank.toString())
        }
    }

    fun setTotalArchers(rank: Int) {
        performV2 {
            setText(HeadToHeadReferenceTestTag.TOTAL_ARCHERS, rank.toString())
        }
    }

    fun checkMeetInString(rankA: Int, rankB: Int, round: String) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(HeadToHeadReferenceTestTag.MEET_IN)
            +CodexNodeInteraction.AssertTextEquals("Rank $rankA will face rank $rankB in the $round round")
        }
    }

    fun checkTable(items: Map<Int, List<Int?>>) {
        performV2Group {
            +CodexNodeMatcher.HasTestTag(HeadToHeadReferenceTestTag.TABLE_RANK)
            +CodexNodeGroupInteraction.ForEach(
                    items.keys.map { listOf(CodexNodeInteraction.AssertTextEquals(it.toString())) }
            )
        }
        performV2Group {
            +CodexNodeMatcher.HasTestTag(HeadToHeadReferenceTestTag.TABLE_ROUND)
            val values = items.values.flatten()
            +CodexNodeGroupInteraction.ForEach(
                    values.map { listOf(CodexNodeInteraction.AssertTextEquals(it?.toString() ?: "Bye")) },
            )
        }
    }
}
