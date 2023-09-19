package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import eywa.projectcodex.common.utils.CodexTestTag

fun List<CodexNodeMatcher>.getMatcher() =
        map { it.getMatcher() }.reduce { acc, m -> acc.and(m) }


sealed class CodexNodeMatcher {
    data class HasText(val text: String) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasText(text)
    }

    data class HasContentDescription(val text: String) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasContentDescription(text)
    }

    data class HasTestTag(val testTag: CodexTestTag) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasTestTag(testTag.getTestTag())
    }

    data class HasAnyAncestor(val matchers: List<CodexNodeMatcher>) : CodexNodeMatcher() {
        constructor(matcher: CodexNodeMatcher) : this(listOf(matcher))

        override fun getMatcher(): SemanticsMatcher = hasAnyAncestor(matchers.getMatcher())
    }

    data class HasAnySibling(val matchers: List<CodexNodeMatcher>) : CodexNodeMatcher() {
        constructor(matcher: CodexNodeMatcher) : this(listOf(matcher))

        override fun getMatcher(): SemanticsMatcher = hasAnySibling(matchers.getMatcher())
    }

    data class HasAnyDescendant(val matchers: List<CodexNodeMatcher>) : CodexNodeMatcher() {
        constructor(matcher: CodexNodeMatcher) : this(listOf(matcher))

        override fun getMatcher(): SemanticsMatcher = hasAnyDescendant(matchers.getMatcher())
    }

    data class HasAnyChild(val matchers: List<CodexNodeMatcher>) : CodexNodeMatcher() {
        constructor(matcher: CodexNodeMatcher) : this(listOf(matcher))

        override fun getMatcher(): SemanticsMatcher = hasAnyChild(matchers.getMatcher())
    }

    object HasClickAction : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasClickAction()
    }

    object HasScrollToIndexAction : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasScrollToIndexAction()
    }

    object HasScrollAction : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasScrollAction()
    }

    abstract fun getMatcher(): SemanticsMatcher
}
