package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.components.classificationTables.ClassificationTablesTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.SelectRoundRobot
import eywa.projectcodex.instrumentedTests.robots.common.TabSwitcherRobot


class ClassificationTablesRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ClassificationTablesTestTag.SCREEN), TabSwitcherRobot {
    override val group: TabSwitcherGroup
        get() = TabSwitcherGroup.REFERENCES

    val roundRobot = SelectRoundRobot(composeTestRule, ClassificationTablesTestTag.SCREEN)

    fun clickGender(expectedNewGenderIsGent: Boolean = true) {
        val expectedNewGender = if (expectedNewGenderIsGent) "Gents" else "Ladies"
        perform {
            clickDataRow(ClassificationTablesTestTag.GENDER_SELECTOR)
            +CodexNodeInteraction.AssertTextEquals(expectedNewGender).waitFor()
        }
    }

    fun setAge(value: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.AGE_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.AGE_SELECTOR)
            +CodexNodeInteraction.AssertContentDescriptionEquals("$value Age:")
        }
    }

    fun setBowStyle(value: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.BOW_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.BOW_SELECTOR)
            +CodexNodeInteraction.AssertContentDescriptionEquals("$value Bow:")
        }
    }

    fun checkNoClassifications() {
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_NO_DATA)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun checkClassifications(data: List<TableRow>) {
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_CLASSIFICATION))
            +CodexNodeGroupInteraction.ForEach(
                    data.map { listOf(CodexNodeInteraction.AssertTextEquals(it.classification).waitFor()) }
            )
        }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_SCORE))
            +CodexNodeGroupInteraction.ForEach(
                    data.map {
                        listOf(
                                CodexNodeInteraction.AssertTextEquals(it.getScoreString()),
                                CodexNodeInteraction.AssertContentDescriptionEquals(it.getScoreContentDescription()),
                        )
                    }
            )
        }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_HANDICAP))
            +CodexNodeGroupInteraction.ForEach(
                    data.map {
                        listOf(
                                CodexNodeInteraction.AssertTextEquals(it.getHandicapString()),
                                CodexNodeInteraction.AssertContentDescriptionEquals(it.getHandicapContentDescription()),
                        )
                    }
            )
        }
    }

    data class TableRow constructor(
            val classification: String,
            val score: Int?,
            val handicap: Int,
            val isOfficial: Boolean = true,
    ) {
        fun getScoreString(): String =
                score?.toString() ?: "-"

        fun getScoreContentDescription(): String =
                if (score == null) "no score data"
                else score.toString() + " score" + getSuffix()

        fun getHandicapString(): String = handicap.toString()
        fun getHandicapContentDescription(): String = handicap.toString() + " handicap" + getSuffix()

        private fun getSuffix() = if (isOfficial) " " else " unofficial"
    }
}
