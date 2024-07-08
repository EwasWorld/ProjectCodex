package eywa.projectcodex.components.handicapTables

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.Handicap

data class HandicapTablesState(
        val input: PartialNumberFieldState = PartialNumberFieldState(),
        val inputType: InputType = InputType.HANDICAP,
        val use2023System: Boolean = DatastoreKey.Use2023HandicapSystem.defaultValue,
        val handicaps: List<HandicapScore> = emptyList(),
        val selectRoundDialogState: SelectRoundDialogState = SelectRoundDialogState(),
        val selectFaceDialogState: SelectRoundFaceDialogState = SelectRoundFaceDialogState(
                round = selectRoundDialogState.selectedRound?.round,
                distances = selectRoundDialogState.roundSubTypeDistances?.map { it.distance },
        ),
        val updateDefaultRoundsState: UpdateDefaultRoundsState = UpdateDefaultRoundsState.NotStarted,
        val useSimpleHandicapView: Boolean = true,
) {
    val inputFull
        get() = input.asNumberFieldState(
                NumberValidatorGroup(TypeValidator.IntValidator, *inputType.validators(use2023System).toTypedArray()),
        )

    val highlightedHandicap = handicaps.find { it.isHighlightedRow }

    val detailedHandicaps: List<DetailedHandicapBreakdown>?
        get() {
            val round = selectRoundDialogState.selectedRound ?: return null

            val arrowCounts = round.roundArrowCounts ?: return null
            if (arrowCounts.size <= 1) return null

            val distances = round.getDistances(selectRoundDialogState.selectedSubTypeId) ?: return null
            if (arrowCounts.size != distances.size) return null

            val faces = selectFaceDialogState.finalFaces
            if (faces != null && !(faces.size == arrowCounts.size || faces.size == 1)) return null

            val handicap = highlightedHandicap?.handicap ?: return null
            val endSize = if (round.round.isOutdoor) 6 else 3

            return List(arrowCounts.size) {
                val arrowCount = arrowCounts[it]
                val distance = distances[it].distance
                val face = when {
                    faces == null -> null
                    faces.size == 1 -> faces[0]
                    else -> faces[it]
                }

                val arrowScore = Handicap.getExactScoreForHandicap(
                        handicap = handicap,
                        arrowCount = 1,
                        distance = distance,
                        isMetric = round.round.isMetric,
                        faceSizeInCm = arrowCount.faceSizeInCm,
                        scoringType = Handicap.ScoringType.getScoringType(round.round, face),
                        innerTenScoring = false,
                        isOutdoor = round.round.isOutdoor,
                        use2023Handicaps = use2023System,
                )
                DetailedHandicapBreakdown(
                        distance = distance,
                        score = arrowCount.arrowCount * arrowScore,
                        averageEnd = endSize * arrowScore,
                        averageArrow = arrowScore,
                )
            }
        }

    val distanceUnitRes = selectRoundDialogState.selectedRound?.getDistanceUnitRes()
}

data class DetailedHandicapBreakdown(
        val distance: Int,
        val score: Double,
        val averageEnd: Double,
        val averageArrow: Double,
) : CodexGridRowMetadata {
    override val isTotalRow: Boolean
        get() = false
}

class HandicapScore(
        val handicap: Int,
        val score: Int,
        arrowsInRound: Int,
        arrowsPerEnd: Int,
        val isHighlightedRow: Boolean = false,
) : CodexGridRowMetadata {
    val allowance
        get() = Handicap.fullRoundScoreToAllowance(score)

    val averageArrow = score.toFloat() / arrowsInRound
    val averageEnd = score.toFloat() / (arrowsInRound.toFloat() / arrowsPerEnd)

    override val isTotalRow: Boolean
        get() = isHighlightedRow

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HandicapScore) return false

        if (handicap != other.handicap) return false
        if (score != other.score) return false

        return true
    }

    override fun hashCode(): Int {
        var result = handicap
        result = 31 * result + score
        return result
    }
}

enum class InputType(
        @StringRes val labelId: Int,
        @StringRes val typeHelpId: Int,
        @StringRes val inputHelpId: Int,
        val validators: (use2023System: Boolean) -> List<NumberValidator<in Int>>,
) {
    HANDICAP(
            R.string.handicap_tables__handicap_input_header,
            R.string.help_handicap_tables__input_type_body_handicap,
            R.string.help_handicap_tables__input_body_handicap,
            { listOf(NumberValidator.InRange(Handicap.MIN_HANDICAP..Handicap.maxHandicap(it))) },
    ),
    SCORE(
            R.string.handicap_tables__score_input_header,
            R.string.help_handicap_tables__input_type_body_score,
            R.string.help_handicap_tables__input_body_score,
            { listOf(NumberValidator.AtLeast(1)) },
    ),
}
