package eywa.projectcodex.instrumentedTests.dsl

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.BaseRobot

@DslMarker
annotation class TestActionDslMarker

/**
 * Dsl for creating Espresso matchers with actions
 *
 * @see BaseRobot.perform
 */
@Deprecated(
        "There's a shiny new version",
        ReplaceWith("TestActionDslV2", " eywa.projectcodex.instrumentedTests.dsl.TestActionDslV2"),
)
@TestActionDslMarker
class TestActionDsl {
    private var info: CodexNodeInfo = CodexNodeInfo.Empty
    var useUnmergedTree = false

    operator fun CodexNodeInteraction.unaryPlus() {
        info += this
    }

    operator fun CodexNodeMatcher.unaryPlus() {
        info += this
    }

    operator fun CodexNodeGroupToOne.unaryPlus() {
        info += this
    }

    operator fun CodexNodeGroupInteraction.unaryPlus() {
        info += this
    }

    fun allNodes(vararg matchers: CodexNodeMatcher) {
        check(info == CodexNodeInfo.Empty) { "This call must come before adding other matchers or actions" }
        info = CodexNodeInfo.Group(matchers.toList())
    }

    fun run(composeTestRule: ComposeTestRule<MainActivity>) {
        info.createNode(composeTestRule, useUnmergedTree)
        info.performActions()
    }
}
