package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.*

/**
 * Actions which can be performed on a [SemanticsNodeInteraction]
 */
sealed class CodexNodeAction {
    object PerformClick : CodexNodeAction() {
        override fun perform(node: SemanticsNodeInteraction) {
            node.performClick()
        }
    }

    object PerformScrollTo : CodexNodeAction() {
        override fun perform(node: SemanticsNodeInteraction) {
            node.performScrollTo()
        }
    }

    object AssertIsDisplayed : CodexNodeAction() {
        override fun perform(node: SemanticsNodeInteraction) {
            node.assertIsDisplayed()
        }
    }

    object AssertDoesNotExist : CodexNodeAction() {
        override fun perform(node: SemanticsNodeInteraction) {
            node.assertDoesNotExist()
        }
    }

    data class AssertTextEquals(val text: String) : CodexNodeAction() {
        override fun perform(node: SemanticsNodeInteraction) {
            node.assertTextEquals(text)
        }
    }

    data class SetText(val text: String, val append: Boolean = false) : CodexNodeAction() {
        override fun perform(node: SemanticsNodeInteraction) {
            if (!append) node.performTextClearance()
            node.performTextInput(text)
        }
    }

    abstract fun perform(node: SemanticsNodeInteraction)
}
