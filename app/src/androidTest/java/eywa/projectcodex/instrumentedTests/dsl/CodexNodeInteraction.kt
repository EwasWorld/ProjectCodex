package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import eywa.projectcodex.common.CustomConditionWaiter


fun waitForWrapper(waitFor: Boolean, description: String?, block: () -> Unit) =
        if (!waitFor) block()
        else CustomConditionWaiter.waitForComposeCondition(description) { block() }

fun assertTextEqualsOrDoesntExist(text: String?) =
        if (text != null) CodexNodeInteraction.AssertTextEquals(text)
        else CodexNodeInteraction.AssertDoesNotExist()

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

    class PerformLongClick : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.performTouchInput { longClick() }
        }
    }

    /**
     * Note: LazyRow/Column cannot use this.
     */
    class PerformScrollTo : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.performScrollTo()
        }
    }

    /**
     * Note: LazyRow/Column cannot use this (probably lol).
     */
    class PerformScrollToNode(private val matchers: List<CodexNodeMatcher>) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.performScrollToNode(matchers.getMatcher())
        }
    }

    data class PerformScrollToIndex(val index: Int) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.performScrollToIndex(index)
        }
    }

    data class Swipe(var direction: Direction) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            with(direction) {
                node.performTouchInput {
                    directionalSwipe()
                }
            }
        }

        enum class Direction {
            UP {
                override fun TouchInjectionScope.directionalSwipe() = swipeUp()
            },
            DOWN {
                override fun TouchInjectionScope.directionalSwipe() = swipeDown()
            },
            LEFT {
                override fun TouchInjectionScope.directionalSwipe() = swipeLeft()
            },
            RIGHT {
                override fun TouchInjectionScope.directionalSwipe() = swipeRight()
            },
            ;

            abstract fun TouchInjectionScope.directionalSwipe()
        }
    }

    class AssertIsDisplayed : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertIsDisplayed()
        }
    }

    data class AssertSiblingCount(val count: Int) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.onSiblings().filter(CodexNodeMatcher.IsNotCached.getMatcher()).assertCountEquals(count)
        }
    }

    data class Assert(val matcher: CodexNodeMatcher) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assert(matcher.getMatcher())
        }
    }

    data class AssertFalse(val matcher: CodexNodeMatcher) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assert(matcher.getMatcher().not())
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

    data class AssertTextMatchesRegex(val pattern: Regex) : CodexNodeInteraction() {
        constructor(pattern: String) : this(Regex(pattern))

        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assert(CodexNodeMatcher.HasTextMatchingRegex(pattern).getMatcher())
        }
    }

    data class AssertTextContains(val text: String) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertTextContains(text, substring = true)
        }
    }

    /**
     * @param texts if the node has merge descendants set, each descendant will be an item in this list
     */
    data class AssertContentDescriptionEquals(val texts: List<String>) : CodexNodeInteraction() {
        constructor(text: String) : this(listOf(text))

        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertContentDescriptionEquals(*texts.toTypedArray())
        }
    }

    data class AssertIsSelected(val value: Boolean) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            if (value) node.assertIsSelected()
            else node.assertIsNotSelected()
        }
    }

    class AssertIsSelectable : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            node.assertIsSelectable()
        }
    }

    data class SetText(val text: String, val append: Boolean = false) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            if (!append) node.performTextClearance()
            node.performTextInput(text)
        }
    }

    data class AssertHasError(val errorText: String?) : CodexNodeInteraction() {
        override fun performInternal(node: SemanticsNodeInteraction) {
            val expected =
                    if (errorText != null) CodexNodeMatcher.HasError(errorText)
                    else CodexNodeMatcher.HasNoError
            node.assert(expected.getMatcher())
        }
    }

    protected abstract fun performInternal(node: SemanticsNodeInteraction)
    fun perform(node: SemanticsNodeInteraction, description: String?) {
        waitForWrapper(waitFor, description) { performInternal(node) }
    }

    fun waitFor(): CodexNodeInteraction {
        waitFor = true
        return this
    }
}
