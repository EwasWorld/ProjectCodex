package eywa.projectcodex.instrumentedTests.utils

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick

sealed class CodexNodeAction {
    object PerformClick : CodexNodeAction() {
        override fun perform(node: SemanticsNodeInteraction) {
            node.performClick()
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

    abstract fun perform(node: SemanticsNodeInteraction)
}
