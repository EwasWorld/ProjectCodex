package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertCountEquals
import eywa.projectcodex.common.CustomConditionWaiter

/**
 * Actions which can be performed on a [SemanticsNodeInteractionCollection]
 */
sealed class CodexNodeGroupAction {
    data class ForEach(
            val actions: List<CodexNodeAction>,
            val waitFor: Boolean = true,
    ) : CodexNodeGroupAction() {
        override fun perform(nodes: SemanticsNodeInteractionCollection) {
            val wrapper = if (!waitFor) {
                { it: () -> Unit -> it() }
            }
            else {
                { it: () -> Unit -> CustomConditionWaiter.waitForComposeCondition { it() } }
            }

            actions.forEachIndexed { index, action ->
                wrapper {
                    action.perform(nodes[index])
                }
            }
        }
    }

    data class AssertCount(val count: Int) : CodexNodeGroupAction() {
        override fun perform(nodes: SemanticsNodeInteractionCollection) {
            nodes.assertCountEquals(count)
        }
    }

    abstract fun perform(nodes: SemanticsNodeInteractionCollection)
}
