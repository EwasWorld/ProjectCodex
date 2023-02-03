package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.*
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.utils.transpose
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadScreen
import eywa.projectcodex.components.mainActivity.MainActivity
import org.junit.Assert

class ScorePadRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArcherRoundRobot(composeTestRule) {
    fun waitForLoad() {
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule
                    .onAllNodesWithTag(ScorePadScreen.TestTag.CELL)
                    .onFirst()
                    .assertIsDisplayed()
        }
    }

    fun checkScorePadData(list: List<ExpectedRowData>) {
        val allCells = list.map { it.asList() }.transpose().flatten()
        val nodes = composeTestRule.onAllNodesWithTag(ScorePadScreen.TestTag.CELL)

        allCells.forEachIndexed { index, text ->
            nodes[index].assertTextEquals(text)
        }
        nodes.assertCountEquals(allCells.size)
    }

    fun clickOkOnNoDataDialog() {
        TODO()

        Assert.assertFalse("Still on score frag after navigating away", isShown())
    }

    fun clickRow(rowIndex: Int) {
        TODO()
    }

    fun clickEditDropdownMenuItem(block: EditEndRobot.() -> Unit) {
        TODO()
    }

    fun clickInsertDropdownMenuItem(block: InsertEndRobot.() -> Unit) {
        TODO()
    }

    fun clickDeleteDropdownMenuItem(dialogAction: Boolean) {
        TODO()
    }

    data class ExpectedRowData(
            val header: String?,
            val main: String,
            val hits: String,
            val score: String,
            val golds: String,
            val runningTotal: String?,
    ) {
        constructor(
                header: String?,
                main: String,
                hits: Int,
                score: Int,
                golds: Int,
                runningTotal: Int?,
        ) : this(
                header,
                main,
                hits.toString(),
                score.toString(),
                golds.toString(),
                runningTotal?.toString(),
        )

        fun asList() = listOfNotNull(header ?: "", main, hits, score, golds, runningTotal ?: "-")
    }
}
