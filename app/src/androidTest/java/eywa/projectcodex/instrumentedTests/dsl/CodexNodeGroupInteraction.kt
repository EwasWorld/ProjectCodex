package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertCountEquals

/**
 * Actions which can be performed on a [SemanticsNodeInteractionCollection].
 * One List<CodexNodeInteraction> per expected node. Each node can have multiple [CodexNodeInteraction]
 */
sealed class CodexNodeGroupInteraction {
    data class ForEach(val actions: List<List<CodexNodeInteraction>>) : CodexNodeGroupInteraction() {
        override fun perform(nodes: SemanticsNodeInteractionCollection) {
            actions.forEachIndexed { index, nodeActions ->
                nodeActions.forEach {
                    it.perform(nodes[index])
                }
            }
        }
    }

    data class AssertCount(val count: Int) : CodexNodeGroupInteraction() {
        override fun perform(nodes: SemanticsNodeInteractionCollection) {
            nodes.assertCountEquals(count)
        }
    }

    abstract fun perform(nodes: SemanticsNodeInteractionCollection)
}
