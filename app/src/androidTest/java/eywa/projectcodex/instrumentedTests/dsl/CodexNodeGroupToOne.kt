package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast

/**
 * Ways to turn a [SemanticsNodeInteractionCollection] into a single [SemanticsNodeInteraction]
 */
sealed class CodexNodeGroupToOne {
    object First : CodexNodeGroupToOne() {
        override fun toOne(group: SemanticsNodeInteractionCollection) = group.onFirst()
    }

    object Last : CodexNodeGroupToOne() {
        override fun toOne(group: SemanticsNodeInteractionCollection) = group.onLast()
    }

    data class Index(val index: Int) : CodexNodeGroupToOne() {
        override fun toOne(group: SemanticsNodeInteractionCollection) = group[index]
    }

    data class HasContentDescription(val text: String) : CodexNodeGroupToOne() {
        override fun toOne(group: SemanticsNodeInteractionCollection) =
                group.filterToOne(CodexNodeMatcher.HasContentDescription(text).getMatcher())
    }

    abstract fun toOne(group: SemanticsNodeInteractionCollection): SemanticsNodeInteraction
}
