package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onFirst
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.core.mainActivity.MainActivity

/**
 * Helper class for [TestActionDsl].
 * Handles which matchers and actions can be added when.
 * Runs actions with [createNode], [assertIsDisplayed], and [performActions].
 */
internal sealed class CodexNodeInfo {
    object Empty : CodexNodeInfo() {
        override fun plus(other: CodexNodeAction): CodexNodeInfo = Single() + other
        override fun plus(other: CodexNodeMatcher): CodexNodeInfo = Single() + other
    }

    data class Single(
            val matchers: List<CodexNodeMatcher> = emptyList(),
            val actions: List<CodexNodeAction> = emptyList(),
    ) : CodexNodeInfo() {
        private var node: SemanticsNodeInteraction? = null

        override fun plus(other: CodexNodeAction): CodexNodeInfo = copy(actions = actions + other)
        override fun plus(other: CodexNodeMatcher): CodexNodeInfo = copy(matchers = matchers + other)

        override fun createNode(
                composeTestRule: ComposeTestRule<MainActivity>,
                useUnmergedTree: Boolean,
        ) {
            check(matchers.isNotEmpty()) { "No matchers" }
            node = composeTestRule.onNode(matcher = matchers.getMatcher(), useUnmergedTree = useUnmergedTree)
        }

        override fun assertIsDisplayed() {
            node!!.assertIsDisplayed()
        }

        override fun performActions() {
            check(actions.isNotEmpty()) { "No actions" }
            actions.forEach { it.perform(node!!) }
        }
    }

    data class GroupToSingle(
            val matchers: List<CodexNodeMatcher>,
            val groupToOne: CodexNodeGroupToOne,
            val actions: List<CodexNodeAction> = emptyList(),
    ) : CodexNodeInfo() {
        private var node: SemanticsNodeInteraction? = null

        override fun plus(other: CodexNodeAction): CodexNodeInfo = copy(actions = actions + other)

        override fun createNode(
                composeTestRule: ComposeTestRule<MainActivity>,
                useUnmergedTree: Boolean,
        ) {
            check(matchers.isNotEmpty()) { "No matchers" }
            node = groupToOne.toOne(
                    composeTestRule.onAllNodes(matcher = matchers.getMatcher(), useUnmergedTree = useUnmergedTree)
            )
        }

        override fun assertIsDisplayed() {
            node!!.assertIsDisplayed()
        }

        override fun performActions() {
            check(actions.isNotEmpty()) { "No actions" }
            actions.forEach { it.perform(node!!) }
        }
    }

    data class Group(
            val matchers: List<CodexNodeMatcher>,
            val actions: List<CodexNodeGroupAction> = emptyList(),
    ) : CodexNodeInfo() {
        private var node: SemanticsNodeInteractionCollection? = null

        override fun plus(other: CodexNodeGroupAction): CodexNodeInfo = copy(actions = actions + other)

        override fun plus(other: CodexNodeGroupToOne): CodexNodeInfo {
            check(actions.isEmpty()) { "Group actions have already been added, can't transform to single node" }
            return GroupToSingle(matchers, other, emptyList())
        }

        override fun createNode(
                composeTestRule: ComposeTestRule<MainActivity>,
                useUnmergedTree: Boolean,
        ) {
            check(matchers.isNotEmpty()) { "No matchers" }
            node = composeTestRule.onAllNodes(matcher = matchers.getMatcher(), useUnmergedTree = useUnmergedTree)
        }

        override fun assertIsDisplayed() {
            node!!.onFirst().assertIsDisplayed()
        }

        override fun performActions() {
            check(actions.isNotEmpty()) { "No actions" }
            actions.forEach { it.perform(node!!) }
        }
    }

    open operator fun plus(other: CodexNodeAction): CodexNodeInfo = throw NotImplementedError()
    open operator fun plus(other: CodexNodeMatcher): CodexNodeInfo = throw NotImplementedError()
    open operator fun plus(other: CodexNodeGroupAction): CodexNodeInfo = throw NotImplementedError()
    open operator fun plus(other: CodexNodeGroupToOne): CodexNodeInfo = throw NotImplementedError()

    open fun createNode(composeTestRule: ComposeTestRule<MainActivity>, useUnmergedTree: Boolean): Unit =
            throw NotImplementedError()

    /**
     * Checks that at least one node (or the only node) is displayed
     */
    open fun assertIsDisplayed(): Unit = throw NotImplementedError()
    open fun performActions(): Unit = throw NotImplementedError()
}
