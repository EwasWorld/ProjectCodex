package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.core.mainActivity.MainActivity

/**
 * Represents a single action or check
 */
open class TestActionDslV2 internal constructor() {
    private var nodes = mutableListOf<TestActionDslNode>()

    /**
     * Start a check or action by matching a single node
     */
    fun singleNode(config: TestActionDslSingleNode.First.() -> Unit) {
        if (nodes.isNotEmpty())
            throw IllegalStateException("Action already used, start a new perform block to run a new check")

        TestActionDslSingleNode.First(this).apply {
            nodes.add(this)
            config()
        }
    }

    /**
     * Start a check or action by matching a group of nodes
     */
    fun allNodes(config: TestActionDslGroupNode.First.() -> Unit) {
        if (nodes.isNotEmpty())
            throw IllegalStateException("Action already used, start a new perform block to run a new check")

        TestActionDslGroupNode.First(this).apply {
            nodes.add(this)
            config()
        }
    }

    internal fun addNode(node: TestActionDslNode) {
        nodes.add(node)
    }

    internal fun perform(composeTestRule: ComposeTestRule<MainActivity>) {
        nodes.fold<TestActionDslNode, TestActionDslPreviousNode?>(null) { prev, node ->
            val next = node.perform(composeTestRule, prev)
            next
        }
    }
}


abstract class TestActionDslNode {
    abstract val properties: TestActionDslV2
    var isComplete = false

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
        override val properties: TestActionDslV2,
) : TestActionDslNode() {
    protected val actions = mutableListOf<CodexNodeInteraction>()

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

        actions.forEach { it.perform(node) }
        return TestActionDslPreviousNode.Single(node)
    }

    @TestActionDslMarker
    class First internal constructor(props: TestActionDslV2) : TestActionDslSingleNode(null, props) {
        private val matchers = mutableListOf<CodexNodeMatcher>()
        private var useUnmergedTree = false

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

            return composeTestRule
                    .onNode(matchers.getMatcher(), useUnmergedTree)
                    .apply { actions.forEach { it.perform(this) } }
                    .let { TestActionDslPreviousNode.Single(it) }
        }
    }

    /**
     * How a [TestActionDslSingleNode] was created from the previous [TestActionDslNode]
     */
    internal sealed class Creation {
        class FromGroup(val groupToOne: CodexNodeGroupToOne) : Creation()
    }
}

@TestActionDslMarker
open class TestActionDslGroupNode internal constructor(
        override val properties: TestActionDslV2,
) : TestActionDslNode() {
    protected val actions = mutableListOf<CodexNodeGroupInteraction>()

    operator fun CodexNodeGroupInteraction.unaryPlus() {
        checkIsValidSetupCall()
        actions += this
    }

    fun toSingle(groupToOne: CodexNodeGroupToOne, config: TestActionDslSingleNode.() -> Unit) {
        checkIsValidSetupCall()
        isComplete = true

        properties.addNode(
                TestActionDslSingleNode(TestActionDslSingleNode.Creation.FromGroup(groupToOne), properties)
                        .apply(config)
        )
    }

    override fun perform(
            composeTestRule: ComposeTestRule<MainActivity>,
            previous: TestActionDslPreviousNode?
    ): TestActionDslPreviousNode {
        throw UnsupportedOperationException()
    }

    @TestActionDslMarker
    class First internal constructor(props: TestActionDslV2) : TestActionDslGroupNode(props) {
        private val matchers = mutableListOf<CodexNodeMatcher>()
        private var useUnmergedTree = false

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
            actions.forEach { it.perform(node) }
            return TestActionDslPreviousNode.Group(node)
        }
    }
}

internal sealed class TestActionDslPreviousNode {
    data class Single(val node: SemanticsNodeInteraction) : TestActionDslPreviousNode()
    data class Group(val node: SemanticsNodeInteractionCollection) : TestActionDslPreviousNode()
}
