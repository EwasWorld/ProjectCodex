package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertCountEquals

sealed class CodexNodeGroupAction {
    data class ForEach(val actions: List<CodexNodeAction>): CodexNodeGroupAction() {
        override fun perform(nodes: SemanticsNodeInteractionCollection) {
            actions.forEachIndexed { index, action ->
                action.perform(nodes[index])
            }
        }
    }

    data class AssertCount(val count: Int): CodexNodeGroupAction() {
        override fun perform(nodes: SemanticsNodeInteractionCollection) {
            nodes.assertCountEquals(count)
        }
    }

    abstract fun perform(nodes: SemanticsNodeInteractionCollection)
}
