package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.performScrollToIndex
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.core.mainActivity.MainActivity

@DslMarker
annotation class TestActionDslMarker

/**
 * Represents a single action or check
 */
@TestActionDslMarker
open class TestActionDslRoot internal constructor() {
    private var nodes = mutableListOf<TestActionDslNode>()

    /**
     * Start a check or action by matching a single node
     */
    fun singleNode(config: TestActionDslSingleNode.First.() -> Unit) {
        if (nodes.isNotEmpty()) {
            throw IllegalStateException("Action already used, start a new perform block to run a new check")
        }

        TestActionDslSingleNode.First(this).apply {
            this@TestActionDslRoot.nodes.add(this)
            config()
        }
    }

    /**
     * Start a check or action by matching a group of nodes
     */
    fun allNodes(config: TestActionDslGroupNode.First.() -> Unit) {
        if (nodes.isNotEmpty()) {
            throw IllegalStateException("Action already used, start a new perform block to run a new check")
        }

        TestActionDslGroupNode.First(this).apply {
            this@TestActionDslRoot.nodes.add(this)
            config()
        }
    }

    internal fun addNode(node: TestActionDslNode) {
        nodes.add(node)
    }

    internal fun perform(composeTestRule: ComposeTestRule<MainActivity>) {
        nodes.fold<TestActionDslNode, TestActionDslPreviousNode?>(null) { prev, node ->
            node.perform(composeTestRule, prev)
        }
    }
}


abstract class TestActionDslNode {
    abstract val root: TestActionDslRoot
    var isComplete = false

    abstract val description: String

    fun checkIsValidSetupCall() {
        if (isComplete) throw IllegalStateException("Scope no longer valid")
    }

    /**
     * Create the node and perform any actions associated with it, then return the node.
     *
     * Passing [TestActionDslPreviousNode] around allows for chaining
     * e.g. onNode(withTag("Screen")).assertDisplayed().onChildren().onFirst().assertTextEquals("Title")
     * In this example, onChildren moves from [TestActionDslSingleNode] to [TestActionDslGroupNode]
     */
    internal abstract fun perform(
            composeTestRule: ComposeTestRule<MainActivity>,
            previous: TestActionDslPreviousNode?,
    ): TestActionDslPreviousNode
}

@TestActionDslMarker
open class TestActionDslSingleNode internal constructor(
        /**
         * Null if this is the first node
         */
        private val creation: Creation?,
        override val root: TestActionDslRoot,
) : TestActionDslNode() {
    protected val actions = mutableListOf<CodexNodeInteraction>()

    override val description: String
        get() = "Actions: " + actions.joinToString { it.toString() } + "\nCreation: " + creation.toString()

    operator fun CodexNodeInteraction.unaryPlus() {
        checkIsValidSetupCall()
        actions += this
    }

    override fun perform(
            composeTestRule: ComposeTestRule<MainActivity>,
            previous: TestActionDslPreviousNode?
    ): TestActionDslPreviousNode {
        val node = when (creation) {
            is Creation.FromGroup -> {
                if (previous !is TestActionDslPreviousNode.Group) throw IllegalStateException()
                creation.groupToOne.toOne(previous.node)
            }

            else -> throw UnsupportedOperationException()
        }

        actions.forEach { it.perform(node, "$description (${previous.description})") }
        return TestActionDslPreviousNode.Single(node, description)
    }

    @TestActionDslMarker
    class First internal constructor(props: TestActionDslRoot) : TestActionDslSingleNode(null, props) {
        private val matchers = mutableListOf<CodexNodeMatcher>()
        private var useUnmergedTree = false
        private var scrollToParentIndex: Int? = null

        override val description: String
            get() = "Matchers" + matchers.joinToString { it.toString() } + "\n" + super.description

        fun useUnmergedTree(value: Boolean = true) {
            useUnmergedTree = value
        }

        /**
         * Performs a [performScrollToIndex] on any parent of the matched node with the required action
         */
        fun scrollToParentIndex(value: Int) {
            scrollToParentIndex = value
        }

        operator fun CodexNodeMatcher.unaryPlus() {
            checkIsValidSetupCall()
            matchers += this
        }

        override fun perform(
                composeTestRule: ComposeTestRule<MainActivity>,
                previous: TestActionDslPreviousNode?
        ): TestActionDslPreviousNode {
            if (previous != null) throw IllegalStateException()

            return composeTestRule
                    .onNode(matchers.getMatcher(), useUnmergedTree)
                    .apply {
                        scrollToParentIndex?.let { index ->
                            val parentMatchers = listOf(
                                    CodexNodeMatcher.HasAnyDescendant(
                                            matchers.filter { it !is CodexNodeMatcher.IsNotCached },
                                    ),
                                    CodexNodeMatcher.HasScrollToIndexAction,
                            )
                            composeTestRule
                                    .onNode(parentMatchers.getMatcher(), useUnmergedTree)
                                    .performScrollToIndex(index)
                        }
                        actions.forEach { it.perform(this, description) }
                    }
                    .let { TestActionDslPreviousNode.Single(it, description) }
        }
    }

    /**
     * How a [TestActionDslSingleNode] was created from the previous [TestActionDslNode]
     */
    internal sealed class Creation {
        data class FromGroup(val groupToOne: CodexNodeGroupToOne) : Creation()
    }
}

@TestActionDslMarker
open class TestActionDslGroupNode internal constructor(
        override val root: TestActionDslRoot,
) : TestActionDslNode() {
    protected val actions = mutableListOf<CodexNodeGroupInteraction>()

    override val description: String
        get() = "Actions: " + actions.joinToString { it.toString() }

    operator fun CodexNodeGroupInteraction.unaryPlus() {
        checkIsValidSetupCall()
        actions += this
    }

    fun toSingle(groupToOne: CodexNodeGroupToOne, config: TestActionDslSingleNode.() -> Unit) {
        checkIsValidSetupCall()
        isComplete = true

        root.addNode(
                TestActionDslSingleNode(TestActionDslSingleNode.Creation.FromGroup(groupToOne), root).apply(config)
        )
    }

    override fun perform(
            composeTestRule: ComposeTestRule<MainActivity>,
            previous: TestActionDslPreviousNode?
    ): TestActionDslPreviousNode {
        throw UnsupportedOperationException()
    }

    @TestActionDslMarker
    class First internal constructor(props: TestActionDslRoot) : TestActionDslGroupNode(props) {
        private val matchers = mutableListOf<CodexNodeMatcher>()
        private var useUnmergedTree = false

        override val description: String
            get() = "Matchers" + matchers.joinToString { it.toString() } + "\n" + super.description

        fun useUnmergedTree(value: Boolean = true) {
            useUnmergedTree = value
        }

        operator fun CodexNodeMatcher.unaryPlus() {
            checkIsValidSetupCall()
            matchers += this
        }

        override fun perform(
                composeTestRule: ComposeTestRule<MainActivity>,
                previous: TestActionDslPreviousNode?
        ): TestActionDslPreviousNode {
            if (previous != null) throw IllegalStateException()
            val node = composeTestRule.onAllNodes(matchers.getMatcher(), useUnmergedTree)
            actions.forEach { it.perform(node, description) }
            return TestActionDslPreviousNode.Group(node, description)
        }
    }
}

internal sealed class TestActionDslPreviousNode {
    abstract val description: String

    data class Single(
            val node: SemanticsNodeInteraction,
            override val description: String,
    ) : TestActionDslPreviousNode()

    data class Group(
            val node: SemanticsNodeInteractionCollection,
            override val description: String,
    ) : TestActionDslPreviousNode()
}
