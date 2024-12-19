package eywa.projectcodex.instrumentedTests.robots.referenceTables

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.components.referenceTables.classificationTables.ClassificationTablesTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchDataRowValue
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.common.TabSwitcherRobot
import eywa.projectcodex.instrumentedTests.robots.selectRound.SelectRoundBaseRobot


class ClassificationTablesRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ClassificationTablesTestTag.SCREEN), TabSwitcherRobot {
    override val group: TabSwitcherGroup
        get() = TabSwitcherGroup.REFERENCES

    val selectRoundsRobot = SelectRoundBaseRobot(::performV2)

    fun clickGender(expectedNewGenderIsGent: Boolean = true) {
        val expectedNewGender = if (expectedNewGenderIsGent) "Gents" else "Ladies"
        performV2 {
            clickDataRow(ClassificationTablesTestTag.GENDER_SELECTOR, expectedNewGender)
        }
    }

    fun checkGender(isGent: Boolean = true) {
        val expectedGender = if (isGent) "Gents" else "Ladies"
        performV2Single {
            matchDataRowValue(ClassificationTablesTestTag.GENDER_SELECTOR)
            +CodexNodeInteraction.AssertTextEquals(expectedGender).waitFor()
        }
    }

    fun setAge(value: String) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.AGE_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
        checkAge(value)
    }

    fun checkAge(value: String) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.AGE_SELECTOR)
            +CodexNodeInteraction.AssertContentDescriptionEquals("$value Age:")
        }
    }

    fun setBowStyle(value: String) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.BOW_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
        checkBowStyle(value)
    }

    fun checkBowStyle(value: String) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.BOW_SELECTOR)
            +CodexNodeInteraction.AssertContentDescriptionEquals("$value Bow:")
        }
    }

    fun checkNoClassifications() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_NO_DATA)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun checkClassifications(data: List<TableRow>) {
        performV2Group {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_CLASSIFICATION)
            +CodexNodeGroupInteraction.ForEach(
                    data.map { listOf(CodexNodeInteraction.AssertTextEquals(it.classification).waitFor()) }
            )
        }
        performV2Group {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_SCORE)
            +CodexNodeGroupInteraction.ForEach(
                    data.map {
                        listOf(
                                CodexNodeInteraction.AssertTextEquals(it.getScoreString()),
                                CodexNodeInteraction.AssertContentDescriptionEquals(it.getScoreContentDescription()),
                        )
                    }
            )
        }
        performV2Group {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.TABLE_HANDICAP)
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
