package eywa.projectcodex.instrumentedTests.dsl

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.BaseRobot

/**
 * Dsl for creating Espresso matchers with actions
 *
 * @see BaseRobot.perform
 */
class TestActionDsl {
    private var info: CodexNodeInfo = CodexNodeInfo.Empty
    var useUnmergedTree = false

    /**
     * @see CodexNodeInfo.assertIsDisplayed
     */
    var waitForDisplay = false

    operator fun CodexNodeAction.unaryPlus() {
        info += this
    }

    operator fun CodexNodeMatcher.unaryPlus() {
        info += this
    }

    operator fun CodexNodeGroupToOne.unaryPlus() {
        info += this
    }

    operator fun CodexNodeGroupAction.unaryPlus() {
        info += this
    }

    fun allNodes(vararg matchers: CodexNodeMatcher) {
        check(info == CodexNodeInfo.Empty) { "This call must come before adding other matchers or actions" }
        info = CodexNodeInfo.Group(matchers.toList())
    }

    fun run(composeTestRule: ComposeTestRule<MainActivity>) {
        info.createNode(composeTestRule, useUnmergedTree)
        if (waitForDisplay) {
            CustomConditionWaiter.waitForComposeCondition {
                info.assertIsDisplayed()
            }
        }
        info.performActions()
    }
}
