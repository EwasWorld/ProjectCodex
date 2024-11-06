package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.model.Arrow

sealed class HeadToHeadGridRowData : CodexGridRowMetadata {
    abstract val type: HeadToHeadArcherType
    abstract val expectedArrowCount: Int
    abstract val totalScore: Int
    abstract val isComplete: Boolean
    abstract val arrowsShot: Int
    open val isTotalRow: Boolean = true

    data class Arrows(
            override val type: HeadToHeadArcherType,
            override val expectedArrowCount: Int,
            val arrows: List<Arrow> = emptyList(),
    ) : HeadToHeadGridRowData() {
        override val totalScore: Int
            get() = arrows.sumOf { it.score }
        override val isComplete: Boolean
            get() = arrows.size == expectedArrowCount
        override val arrowsShot: Int
            get() = arrows.size
        override val isTotalRow: Boolean
            get() = false
    }

    data class Total(
            override val type: HeadToHeadArcherType,
            override val expectedArrowCount: Int,
            val total: Int? = null,
    ) : HeadToHeadGridRowData() {
        override val totalScore: Int
            get() = total ?: 0
        override val isComplete: Boolean
            get() = total != null
        override val arrowsShot: Int
            get() = if (total == null) 0 else expectedArrowCount
    }

    data class EditableTotal(
            override val type: HeadToHeadArcherType,
            override val expectedArrowCount: Int,
            val text: NumberFieldState<Int> = NumberFieldState(
                    typeValidator = TypeValidator.IntValidator,
                    NumberValidator.InRange(0..expectedArrowCount * 10),
            ),
    ) : HeadToHeadGridRowData() {
        val total: Int? = text.parsed

        override val totalScore: Int
            get() = total ?: 0
        override val isComplete: Boolean
            get() = total != null
        override val arrowsShot: Int
            get() = if (total == null) 0 else expectedArrowCount
    }
}
