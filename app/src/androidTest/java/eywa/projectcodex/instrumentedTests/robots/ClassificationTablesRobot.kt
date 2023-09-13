package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.classificationTables.ClassificationTablesTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.SelectRoundRobot


class ClassificationTablesRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ClassificationTablesTestTag.SCREEN) {
    val roundRobot = SelectRoundRobot(composeTestRule, ClassificationTablesTestTag.SCREEN)

    fun clickGender() {
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.GENDER_SELECTOR)
            +CodexNodeInteraction.PerformClick
        }
    }

    fun setAge(value: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.AGE_SELECTOR)
            +CodexNodeInteraction.PerformClick
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick
        }
    }

    fun setBowStyle(value: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.BOW_SELECTOR)
            +CodexNodeInteraction.PerformClick
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick
        }
    }

    fun checkNoClassifications() {
        perform {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_NO_DATA)
            +CodexNodeInteraction.AssertIsDisplayed
        }
    }

    fun checkClassifications(data: List<TableRow>) {
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_CLASSIFICATION))
            +CodexNodeGroupInteraction.ForEach(
                    data.map { CodexNodeInteraction.AssertTextEquals(it.classification) }
            )
        }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_SCORE))
            +CodexNodeGroupInteraction.ForEach(
                    data.map { CodexNodeInteraction.AssertTextEquals(it.score.toString()) }
            )
        }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_HANDICAP))
            +CodexNodeGroupInteraction.ForEach(
                    data.map { CodexNodeInteraction.AssertTextEquals(it.handicap.toString()) }
            )
        }
    }

    @JvmInline
    value class TableRow private constructor(val data: Triple<String, Int, Int>) {
        constructor(classification: String, score: Int, handicap: Int)
                : this(Triple(classification, score, handicap))

        val classification
            get() = data.first
        val score
            get() = data.second
        val handicap
            get() = data.third
    }
}
