package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import eywa.projectcodex.common.utils.CodexTestTag

fun List<CodexNodeMatcher>.getMatcher() =
        map { it.getMatcher() }.reduce { acc, m -> acc.and(m) }


sealed class CodexNodeMatcher {
    data class HasText(val text: String, val substring: Boolean = false) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasText(text, substring)
    }

    object HasSetTextAction : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasSetTextAction()
    }

    data class HasError(val text: String, val ignoreCase: Boolean = true) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher =
                SemanticsMatcher(
                        "${SemanticsProperties.Error.name} = '$text' (ignoreCase: $ignoreCase)"
                ) {
                    it.config.getOrNull(SemanticsProperties.Error)
                            ?.equals(text, ignoreCase) ?: false
                }
    }

    object HasNoError : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher =
                SemanticsMatcher(
                        "Has no ${SemanticsProperties.Error.name}"
                ) {
                    !it.config.contains(SemanticsProperties.Error)
                }
    }

    data class HasContentDescription(val text: String) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasContentDescription(text)
    }

    data class HasTestTag(val testTag: CodexTestTag, val substring: Boolean = false) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher {
            val text = testTag.getTestTag()
            return SemanticsMatcher(
                    "${SemanticsProperties.TestTag.name} = '$text' (substring: $substring)"
            ) { node ->
                node.config
                        .getOrNull(SemanticsProperties.TestTag)
                        ?.let { if (substring) it.contains(text) else it == text }
                        ?: false
            }
        }
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

    data class Custom(val customMatcher: SemanticsMatcher) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = customMatcher
    }

    abstract fun getMatcher(): SemanticsMatcher
}
