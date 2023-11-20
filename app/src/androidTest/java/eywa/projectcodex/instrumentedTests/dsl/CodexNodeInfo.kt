package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performScrollToIndex
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.core.mainActivity.MainActivity

/**
 * Helper class for [TestActionDsl].
 * Handles which matchers and actions can be added when.
 * Runs actions with [createNode], [assertIsDisplayed], and [performActions].
 */
@Deprecated("This is used by TestActionDsl which has been superseded")
internal sealed class CodexNodeInfo {
    object Empty : CodexNodeInfo() {
        override fun plus(other: CodexNodeInteraction): CodexNodeInfo = Single() + other
        override fun plus(other: CodexNodeMatcher): CodexNodeInfo = Single() + other
    }

    data class Single(
            val matchers: List<CodexNodeMatcher> = emptyList(),
            val actions: List<CodexNodeInteraction> = emptyList(),
    ) : CodexNodeInfo() {
        private var node: SemanticsNodeInteraction? = null
        private var parentNode: SemanticsNodeInteraction? = null

        override fun plus(other: CodexNodeInteraction): CodexNodeInfo = copy(actions = actions + other)
        override fun plus(other: CodexNodeMatcher): CodexNodeInfo = copy(matchers = matchers + other)

        override fun createNode(
                composeTestRule: ComposeTestRule<MainActivity>,
                useUnmergedTree: Boolean,
        ) {
            check(matchers.isNotEmpty()) { "No matchers" }
            node = composeTestRule.onNode(matcher = matchers.getMatcher(), useUnmergedTree = useUnmergedTree)
        }

        override fun performActions() {
            check(actions.isNotEmpty()) { "No actions" }
            actions.forEach { it.perform(node!!) }
        }

        override fun createScrollableParentNode(
                composeTestRule: ComposeTestRule<MainActivity>,
                useUnmergedTree: Boolean
        ) {
            check(matchers.isNotEmpty()) { "No matchers" }

            val parentMatchers = listOf(
                    CodexNodeMatcher.HasAnyDescendant(matchers),
                    CodexNodeMatcher.HasScrollToIndexAction,
            )
            parentNode =
                    composeTestRule.onNode(matcher = parentMatchers.getMatcher(), useUnmergedTree = useUnmergedTree)
        }

        override fun scrollToIndexInParent(index: Int) {
            parentNode!!.performScrollToIndex(index)
        }
    }

    data class GroupToSingle(
            val matchers: List<CodexNodeMatcher>,
            val groupToOne: CodexNodeGroupToOne,
            val actions: List<CodexNodeInteraction> = emptyList(),
    ) : CodexNodeInfo() {
        private var node: SemanticsNodeInteraction? = null
        private var parentNode: SemanticsNodeInteraction? = null

        override fun plus(other: CodexNodeInteraction): CodexNodeInfo = copy(actions = actions + other)

        override fun createNode(
                composeTestRule: ComposeTestRule<MainActivity>,
                useUnmergedTree: Boolean,
        ) {
            check(matchers.isNotEmpty()) { "No matchers" }
            node = groupToOne.toOne(
                    composeTestRule.onAllNodes(matcher = matchers.getMatcher(), useUnmergedTree = useUnmergedTree)
            )
        }

        override fun performActions() {
            check(actions.isNotEmpty()) { "No actions" }
            actions.forEach { it.perform(node!!) }
        }

        override fun createScrollableParentNode(
                composeTestRule: ComposeTestRule<MainActivity>,
                useUnmergedTree: Boolean
        ) {
            check(matchers.isNotEmpty()) { "No matchers" }

            val parentMatchers = listOf(
                    CodexNodeMatcher.HasAnyDescendant(matchers),
                    CodexNodeMatcher.HasScrollToIndexAction,
            )
            parentNode =
                    composeTestRule.onNode(matcher = parentMatchers.getMatcher(), useUnmergedTree = useUnmergedTree)
        }

        override fun scrollToIndexInParent(index: Int) {
            parentNode!!.performScrollToIndex(index)
        }
    }

    data class Group(
            val matchers: List<CodexNodeMatcher>,
            val actions: List<CodexNodeGroupInteraction> = emptyList(),
    ) : CodexNodeInfo() {
        private var node: SemanticsNodeInteractionCollection? = null
        private var parentNode: SemanticsNodeInteraction? = null

        override fun plus(other: CodexNodeGroupInteraction): CodexNodeInfo = copy(actions = actions + other)

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

        override fun performActions() {
            check(actions.isNotEmpty()) { "No actions" }
            actions.forEach { it.perform(node!!) }
        }

        override fun createScrollableParentNode(
                composeTestRule: ComposeTestRule<MainActivity>,
                useUnmergedTree: Boolean
        ) {
            check(matchers.isNotEmpty()) { "No matchers" }

            val parentMatchers = listOf(
                    CodexNodeMatcher.HasAnyDescendant(matchers),
                    CodexNodeMatcher.HasScrollToIndexAction,
            )
            parentNode =
                    composeTestRule.onNode(matcher = parentMatchers.getMatcher(), useUnmergedTree = useUnmergedTree)
        }

        override fun scrollToIndexInParent(index: Int) {
            parentNode!!.performScrollToIndex(index)
        }
    }

    open operator fun plus(other: CodexNodeInteraction): CodexNodeInfo = throw NotImplementedError()
    open operator fun plus(other: CodexNodeMatcher): CodexNodeInfo = throw NotImplementedError()
    open operator fun plus(other: CodexNodeGroupInteraction): CodexNodeInfo = throw NotImplementedError()
    open operator fun plus(other: CodexNodeGroupToOne): CodexNodeInfo = throw NotImplementedError()

    open fun createNode(composeTestRule: ComposeTestRule<MainActivity>, useUnmergedTree: Boolean): Unit =
            throw NotImplementedError()

    open fun createScrollableParentNode(
            composeTestRule: ComposeTestRule<MainActivity>,
            useUnmergedTree: Boolean,
    ): Unit = throw NotImplementedError()

    open fun scrollToIndexInParent(index: Int): Unit = throw NotImplementedError()
    open fun performActions(): Unit = throw NotImplementedError()
}
