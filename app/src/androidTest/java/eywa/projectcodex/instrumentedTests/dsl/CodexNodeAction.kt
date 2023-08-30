package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.*
import eywa.projectcodex.common.CustomConditionWaiter


fun waitForWrapper(waitFor: Boolean, block: () -> Unit) =
        if (!waitFor) block()
        else CustomConditionWaiter.waitForComposeCondition { block() }

/**
 * Actions which can be performed on a [SemanticsNodeInteraction]
 */
sealed class CodexNodeAction {
    private var waitFor: Boolean = false

    object PerformClick : CodexNodeAction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.performClick()
        }
    }

    /**
     * Note: LazyRow/Column cannot use this.
     * Instead, should use [TestActionDsl.scrollToParentIndex]
     */
    object PerformScrollTo : CodexNodeAction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.performScrollTo()
        }
    }

    object AssertIsDisplayed : CodexNodeAction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertIsDisplayed()
        }
    }

    object AssertDoesNotExist : CodexNodeAction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertDoesNotExist()
        }
    }

    data class AssertTextEquals(val text: String) : CodexNodeAction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertTextEquals(text)
        }
    }

    object AssertIsSelected : CodexNodeAction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertIsSelected()
        }
    }

    object AssertIsNotSelected : CodexNodeAction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertIsNotSelected()
        }
    }

    data class SetText(val text: String, val append: Boolean = false) : CodexNodeAction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            if (!append) node.performTextClearance()
            node.performTextInput(text)
        }
    }

    protected abstract fun performInternal(node: SemanticsNodeInteraction)
    fun perform(node: SemanticsNodeInteraction) {
        waitForWrapper(waitFor) { performInternal(node) }
    }

    fun waitFor(): CodexNodeAction {
        waitFor = true
        return this
    }
}
