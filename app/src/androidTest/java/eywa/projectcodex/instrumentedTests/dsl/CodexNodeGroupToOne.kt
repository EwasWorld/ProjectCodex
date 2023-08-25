package eywa.projectcodex.instrumentedTests.dsl

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.onFirst

sealed class CodexNodeGroupToOne {
    object First : CodexNodeGroupToOne() {
        override fun toOne(group: SemanticsNodeInteractionCollection) = group.onFirst()
    }

    data class Index(val index: Int) : CodexNodeGroupToOne() {
        override fun toOne(group: SemanticsNodeInteractionCollection) = group[index]
    }

    abstract fun toOne(group: SemanticsNodeInteractionCollection): SemanticsNodeInteraction
}
