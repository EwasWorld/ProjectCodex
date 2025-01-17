package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersTestTag
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersTypes
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchDataRowValue
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.PerformFn
import eywa.projectcodex.instrumentedTests.robots.selectRound.SelectRoundBaseRobot
import java.util.Calendar

class ViewScoresFiltersRobot(
        val perform: PerformFn,
        val performDatePickerDateSelection: (Calendar) -> Unit,
) {
    val selectRoundsRobot = SelectRoundBaseRobot(perform)

    init {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(ViewScoresFiltersTestTag.SCREEN)
                +CodexNodeInteraction.AssertIsDisplayed().waitFor()
            }
        }
    }

    private fun clickElement(testTag: ViewScoresFiltersTestTag) {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(testTag)
                +CodexNodeInteraction.PerformClick().waitFor()
            }
        }
    }

    private fun clickDataRow(testTag: ViewScoresFiltersTestTag) {
        perform {
            this.clickDataRow(testTag)
        }
    }

    private fun checkElementDisplayed(testTag: ViewScoresFiltersTestTag) {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(testTag)
                +CodexNodeInteraction.AssertIsDisplayed()
            }
        }
    }

    fun setFromDate(year: Int, month: Int, day: Int) {
        clickElement(ViewScoresFiltersTestTag.DATE_FROM_FILTER)

        val calendar = Calendar.getInstance()
        // Use a different hour/minute to ensure it's not overwriting the time
        calendar.set(year, month - 1, day, 13, 15, 0)
        performDatePickerDateSelection(calendar)
    }

    fun setUntilDate(year: Int, month: Int, day: Int) {
        clickElement(ViewScoresFiltersTestTag.DATE_UNTIL_FILTER)

        val calendar = Calendar.getInstance()
        // Use a different hour/minute to ensure it's not overwriting the time
        calendar.set(year, month - 1, day, 13, 15, 0)
        performDatePickerDateSelection(calendar)
    }

    fun clearDateFilters() {
        clickElement(ViewScoresFiltersTestTag.CLEAR_DATE_FROM_FILTER_BUTTON)
        clickElement(ViewScoresFiltersTestTag.CLEAR_DATE_UNTIL_FILTER_BUTTON)
    }

    fun clearScoreFilters() {
        perform {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresFiltersTestTag.CLEAR_SCORE_MAX_FILTER_BUTTON)
                +CodexNodeInteraction.PerformClick().waitFor()
            }
        }
        perform {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresFiltersTestTag.CLEAR_SCORE_MIN_FILTER_BUTTON)
                +CodexNodeInteraction.PerformClick().waitFor()
            }
        }
    }

    fun checkUntilDateErrorShown() {
        checkElementDisplayed(ViewScoresFiltersTestTag.DATE_UNTIL_FILTER_ERROR)
    }

    fun setMinScore(score: Int) {
        perform {
            setText(ViewScoresFiltersTestTag.SCORE_MIN_FILTER, score.toString())
        }
    }

    fun setMaxScore(score: Int) {
        perform {
            setText(ViewScoresFiltersTestTag.SCORE_MAX_FILTER, score.toString())
        }
    }

    fun checkScoreErrorShown() {
        checkElementDisplayed(ViewScoresFiltersTestTag.SCORE_FILTER_ERROR)
    }

    fun clickPbsOnlyFilter() {
        clickDataRow(ViewScoresFiltersTestTag.PERSONAL_BESTS_FILTER)
    }

    fun checkPbFilterInterferenceErrorShown() {
        checkElementDisplayed(ViewScoresFiltersTestTag.PERSONAL_BESTS_FILTER_WARNING)
    }

    fun clickCompleteOnlyFilter() {
        clickDataRow(ViewScoresFiltersTestTag.COMPLETE_FILTER)
    }

    fun clickFirstOfDayFilter() {
        clickDataRow(ViewScoresFiltersTestTag.FIRST_OF_DAY_FILTER)
    }

    fun clickTypeFilter() {
        clickDataRow(ViewScoresFiltersTestTag.TYPE_FILTER)
    }

    fun checkTypeFilter(expectedType: ViewScoresFiltersTypes) {
        val text = when (expectedType) {
            ViewScoresFiltersTypes.ALL -> "All"
            ViewScoresFiltersTypes.SCORE -> "Score"
            ViewScoresFiltersTypes.COUNT -> "Count"
        }
        perform {
            singleNode {
                matchDataRowValue(ViewScoresFiltersTestTag.TYPE_FILTER)
                +CodexNodeInteraction.AssertTextEquals(text).waitFor()
            }
        }
    }

    fun clearRoundsFilter() {
        clickElement(ViewScoresFiltersTestTag.CLEAR_ROUND_FILTER_BUTTON)
    }

    fun clickClose() {
        clickElement(ViewScoresFiltersTestTag.CLOSE_BUTTON)
    }
}
