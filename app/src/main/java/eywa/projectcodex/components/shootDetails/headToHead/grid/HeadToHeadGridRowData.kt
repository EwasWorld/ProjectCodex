package eywa.projectcodex.components.shootDetails.headToHead.grid

import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.model.Arrow

sealed class HeadToHeadGridRowData : CodexGridRowMetadata {
    abstract val type: HeadToHeadArcherType
    abstract val expectedArrowCount: Int
    abstract val totalScore: Int
    abstract val isComplete: Boolean
    abstract val arrowsShot: Int
    open val isTotalRow: Boolean = true
    open val isFullWidth: Boolean = false

    data class Arrows(
            override val type: HeadToHeadArcherType,
            override val expectedArrowCount: Int,
            val arrows: List<Arrow> = emptyList(),
            val dbIds: List<Int>? = null,
    ) : HeadToHeadGridRowData() {
        override val totalScore: Int
            get() = arrows.sumOf { it.score }
        override val isComplete: Boolean
            get() = arrows.size == expectedArrowCount
        override val arrowsShot: Int
            get() = arrows.size
        override val isTotalRow: Boolean
            get() = false

        init {
            standardRowChecks()
        }
    }

    data class Total(
            override val type: HeadToHeadArcherType,
            override val expectedArrowCount: Int,
            val total: Int? = null,
            val dbId: Int? = null,
    ) : HeadToHeadGridRowData() {
        override val totalScore: Int
            get() = total ?: 0
        override val isComplete: Boolean
            get() = total != null
        override val arrowsShot: Int
            get() = if (total == null) 0 else expectedArrowCount

        init {
            standardRowChecks()
        }

        fun asEditableTotal() =
                EditableTotal(type, expectedArrowCount, dbId = dbId).let { field ->
                    field.copy(text = field.text.copy(text = total?.toString() ?: ""))
                }
    }

    data class EditableTotal(
            override val type: HeadToHeadArcherType,
            override val expectedArrowCount: Int,
            val text: NumberFieldState<Int> = NumberFieldState(
                    typeValidator = TypeValidator.IntValidator,
                    NumberValidator.InRange(0..expectedArrowCount * 10),
            ),
            val dbId: Int? = null,
    ) : HeadToHeadGridRowData() {
        val total: Int? = text.parsed

        override val totalScore: Int
            get() = total ?: 0
        override val isComplete: Boolean
            get() = total != null
        override val arrowsShot: Int
            get() = if (total == null) 0 else expectedArrowCount

        init {
            standardRowChecks()
        }
    }

    protected fun standardRowChecks() {
        require(expectedArrowCount > 0)
        require(type != HeadToHeadArcherType.RESULT)
        require(type != HeadToHeadArcherType.SHOOT_OFF)
    }

    data class Result(
            val result: HeadToHeadResult = HeadToHeadResult.LOSS,
            val dbId: Int? = null,
    ) : HeadToHeadGridRowData() {
        init {
            require(orderedMap.find { it.first == result } != null)
        }

        val dbScoreValue: Int
            get() = orderedMap.find { it.first == result }!!.second

        override val type: HeadToHeadArcherType
            get() = HeadToHeadArcherType.RESULT
        override val expectedArrowCount: Int
            get() = 0
        override val totalScore: Int
            get() = 0
        override val isComplete: Boolean
            get() = true
        override val arrowsShot: Int
            get() = 0
        override val isTotalRow: Boolean
            get() = false
        override val isFullWidth: Boolean
            get() = true

        /**
         * Used when the user clicks the result, this is how it will change
         */
        fun next(): Result {
            val index = orderedMap.indexOfFirst { it.first == result }.takeIf { it != -1 }!!
            return Result(
                    result = orderedMap[(index + 1) % orderedMap.size].first,
                    dbId = dbId,
            )
        }

        companion object {
            private val orderedMap = listOf(
                    HeadToHeadResult.WIN to 2,
                    HeadToHeadResult.TIE to 1,
                    HeadToHeadResult.LOSS to 0,
            )

            fun fromDbValue(score: Int, dbId: Int? = null) =
                    Result(
                            result = orderedMap.find { it.second == score }!!.first,
                            dbId = dbId,
                    )
        }
    }

    data class ShootOff(
            /**
             * When this is null, it means the scores are different therefore can be used to determine whether
             * the set is a win/loss. If this is a tie, for standard format there should be another shoot off
             * that uses closest to centre rules. Else win/loss represent closest to centre result
             */
            val result: HeadToHeadResult? = HeadToHeadResult.LOSS,
            val dbId: Int? = null,
    ) : HeadToHeadGridRowData() {
        init {
            require(orderedMap.find { it.first == result } != null)
        }

        val dbScoreValue: Int
            get() = orderedMap.find { it.first == result }!!.second

        override val type: HeadToHeadArcherType
            get() = HeadToHeadArcherType.SHOOT_OFF
        override val expectedArrowCount: Int
            get() = 0
        override val totalScore: Int
            get() = 0
        override val isComplete: Boolean
            get() = true
        override val arrowsShot: Int
            get() = 0
        override val isTotalRow: Boolean
            get() = false
        override val isFullWidth: Boolean
            get() = true

        /**
         * Used when the user clicks the shoot off row, this is how it will change
         */
        fun next(): ShootOff {
            val index = orderedMap.indexOfFirst { it.first == result }.takeIf { it != -1 }!!
            // Don't allow null result to be selected
            val newIndex = ((index + 1) % orderedMap.size).coerceAtLeast(1)
            return ShootOff(
                    result = orderedMap[newIndex].first,
                    dbId = dbId,
            )
        }

        companion object {
            private val orderedMap = listOf(
                    null to 3,
                    HeadToHeadResult.WIN to 2,
                    HeadToHeadResult.TIE to 1,
                    HeadToHeadResult.LOSS to 0,
            )

            fun fromDbValue(score: Int, dbId: Int? = null) =
                    ShootOff(
                            result = orderedMap.find { it.second == score }!!.first,
                            dbId = dbId,
                    )
        }
    }
}
