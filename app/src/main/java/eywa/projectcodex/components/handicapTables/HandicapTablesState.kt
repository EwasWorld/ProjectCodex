package eywa.projectcodex.components.handicapTables

import androidx.annotation.StringRes
import eywa.projectcodex.R
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
        val highlightedHandicap: HandicapScore? = null,
        val selectRoundDialogState: SelectRoundDialogState = SelectRoundDialogState(),
        val selectFaceDialogState: SelectRoundFaceDialogState = SelectRoundFaceDialogState(
                round = selectRoundDialogState.selectedRound?.round,
                distances = selectRoundDialogState.roundSubTypeDistances?.map { it.distance },
        ),
        val updateDefaultRoundsState: UpdateDefaultRoundsState = UpdateDefaultRoundsState.NotStarted,
) {
    val inputFull
        get() = input.asNumberFieldState(
                NumberValidatorGroup(TypeValidator.IntValidator, *inputType.validators(use2023System).toTypedArray()),
        )
}

@JvmInline
value class HandicapScore private constructor(val data: Pair<Int, Int>) {
    constructor(handicap: Int, score: Int) : this(handicap to score)

    val handicap
        get() = data.first
    val score
        get() = data.second
    val allowance
        get() = Handicap.fullRoundScoreToAllowance(score)
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
