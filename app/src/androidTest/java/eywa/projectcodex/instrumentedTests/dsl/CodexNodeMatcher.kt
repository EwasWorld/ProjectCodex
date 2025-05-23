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
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import eywa.projectcodex.common.utils.CodexTestTag

fun List<CodexNodeMatcher>.getMatcher() =
        map { it.getMatcher() }.reduce { acc, m -> acc.and(m) }


sealed class CodexNodeMatcher {
    data class HasText(val text: String, val substring: Boolean = false) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasText(text, substring)
    }

    data object HasSetTextAction : CodexNodeMatcher() {
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

    data class HasTextMatchingRegex(val pattern: Regex) : CodexNodeMatcher() {
        constructor(pattern: String) : this(Regex(pattern))

        override fun getMatcher(): SemanticsMatcher {
            val propertyName = "${SemanticsProperties.Text.name} + ${SemanticsProperties.EditableText.name}"
            return SemanticsMatcher(
                    "$propertyName matches regex '$pattern'"
            ) {
                it.config.getOrNull(SemanticsProperties.Text)
                        .orEmpty()
                        .plus(listOfNotNull(it.config.getOrNull(SemanticsProperties.EditableText)))
                        .any { item -> pattern.matches(item.text) }
            }
        }
    }

    data object HasNoError : CodexNodeMatcher() {
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
            val text = "${testTag.screenName}_${testTag.getElement()}"
            return SemanticsMatcher(
                    "${SemanticsProperties.TestTag.name} = '$text' (substring: $substring)"
            ) { node ->
                node.config
                        .getOrNull(SemanticsProperties.TestTag)
                        ?.let { if (substring) it.contains(text) else it == text }
                        ?: false
            }
        }

        override fun toString(): String {
            return "HasTestTag(${testTag.screenName}_${testTag.getElement()}, substring: $substring)"
        }
    }

    /**
     * LazyColumn will cache nodes that have been removed, leaving them in the hierarchy.
     * An update to compose could resolve this issue as outlined in https://issuetracker.google.com/issues/187188981
     */
    data object IsNotCached : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher {
            return SemanticsMatcher("isNotCached") { node -> node.layoutInfo.isPlaced }
        }
    }

    data class HasAnyAncestor(val matchers: List<CodexNodeMatcher>) : CodexNodeMatcher() {
        constructor(matcher: CodexNodeMatcher) : this(listOf(matcher))

        override fun getMatcher(): SemanticsMatcher = hasAnyAncestor(matchers.getMatcher())
    }

    data class HasParent(val matchers: List<CodexNodeMatcher>) : CodexNodeMatcher() {
        constructor(matcher: CodexNodeMatcher) : this(listOf(matcher))

        override fun getMatcher(): SemanticsMatcher = hasParent(matchers.getMatcher())
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

    data object HasClickAction : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasClickAction()
    }

    data object HasScrollToIndexAction : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasScrollToIndexAction()
    }

    data object HasScrollAction : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = hasScrollAction()
    }

    data object IsNotSelectable : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = isSelectable().not()
    }

    data class Custom(val customMatcher: SemanticsMatcher) : CodexNodeMatcher() {
        override fun getMatcher(): SemanticsMatcher = customMatcher
    }

    abstract fun getMatcher(): SemanticsMatcher
}
