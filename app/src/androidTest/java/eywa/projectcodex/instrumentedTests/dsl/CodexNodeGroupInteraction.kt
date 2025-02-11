package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals

/**
 * Actions which can be performed on a [SemanticsNodeInteractionCollection].
 *
 * WARNING: Don't use objects here as they are like enum constants, they won't keep the [waitFor] property properly
 */
sealed class CodexNodeGroupInteraction {
    private var waitFor: Boolean = false

    /**
     * One List<CodexNodeInteraction> per expected node. Each node can have multiple [CodexNodeInteraction].
     */
    data class ForEach(val actions: List<List<CodexNodeInteraction>>) : CodexNodeGroupInteraction() {
        override fun performInternal(nodes: SemanticsNodeInteractionCollection, description: String?) {
            actions.forEachIndexed { index, nodeActions ->
                nodeActions.forEach {
                    it.perform(nodes[index], description)
                }
            }
        }
    }

    data class AssertCount(val count: Int) : CodexNodeGroupInteraction() {
        override fun performInternal(nodes: SemanticsNodeInteractionCollection, description: String?) {
            nodes.assertCountEquals(count)
        }
    }

    data class AssertAll(val matcher: CodexNodeMatcher) : CodexNodeGroupInteraction() {
        override fun performInternal(nodes: SemanticsNodeInteractionCollection, description: String?) {
            nodes.assertAll(matcher.getMatcher())
        }
    }

    abstract fun performInternal(nodes: SemanticsNodeInteractionCollection, description: String?)

    fun perform(nodes: SemanticsNodeInteractionCollection, description: String) {
        waitForWrapper(waitFor, description) { performInternal(nodes, description) }
    }

    fun waitFor(): CodexNodeGroupInteraction {
        waitFor = true
        return this
    }
}
