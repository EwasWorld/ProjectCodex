package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import eywa.projectcodex.common.CustomConditionWaiter


fun waitForWrapper(waitFor: Boolean, block: () -> Unit) =
        if (!waitFor) block()
        else CustomConditionWaiter.waitForComposeCondition { block() }

/**
 * Actions which can be performed on a [SemanticsNodeInteraction]
 *
 * WARNING: Don't use objects here as they are like enum constants, they won't keep the [waitFor] property properly
 */
sealed class CodexNodeInteraction {
    private var waitFor: Boolean = false

    class PerformClick : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.performClick()
        }
    }

    /**
     * Note: LazyRow/Column cannot use this.
     * Instead, should use [TestActionDsl.scrollToParentIndex]
     */
    class PerformScrollTo : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.performScrollTo()
        }
    }

    class AssertIsDisplayed : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertIsDisplayed()
        }
    }

    data class Assert(val matcher: CodexNodeMatcher) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assert(matcher.getMatcher())
        }
    }

    class AssertDoesNotExist : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertDoesNotExist()
        }
    }

    data class AssertTextEquals(val text: String) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertTextEquals(text)
        }
    }

    data class AssertContentDescriptionEquals(val text: String) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertContentDescriptionEquals(text)
        }
    }

    data class AssertIsSelected(val value: Boolean) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            if (value) node.assertIsSelected()
            else node.assertIsNotSelected()
        }
    }

    data class SetText(val text: String, val append: Boolean = false) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            if (!append) node.performTextClearance()
            node.performTextInput(text)
        }
    }

    protected abstract fun performInternal(node: SemanticsNodeInteraction)
    fun perform(node: SemanticsNodeInteraction) {
        waitForWrapper(waitFor) { performInternal(node) }
    }

    fun waitFor(): CodexNodeInteraction {
        waitFor = true
        return this
    }
}
