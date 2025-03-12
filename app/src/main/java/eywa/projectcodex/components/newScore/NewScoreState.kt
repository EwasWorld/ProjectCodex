package eywa.projectcodex.components.newScore

import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogState
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootDetail
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.model.FullShootInfo
import java.util.*

data class NewScoreState(
        /**
         * Non-null if the fragment is being used to edit an existing round
         */
        val roundBeingEdited: FullShootInfo? = null,

        /**
         * Should block round selection while this is true
         */
        val updateDefaultRoundsState: UpdateDefaultRoundsState = UpdateDefaultRoundsState.NotStarted,

        /*
         * User-set info
         */
        val dateShot: Calendar = getDefaultDate(),
        val type: NewScoreType = NewScoreType.SCORING,
        val h2hIsSetPoints: Boolean = true,
        val h2hFormatIsStandard: Boolean = true,
        val h2hTeamSize: NumberFieldState<Int> = NumberFieldState(
                text = "1",
                validators = NumberValidatorGroup(
                        TypeValidator.IntValidator,
                        NumberValidator.InRange(1..5),
                        NumberValidator.NotRequired,
                ),
        ),
        val h2hEndSize: NumberFieldState<Int> = NumberFieldState(
                TypeValidator.IntValidator,
                NumberValidator.InRange(1..12),
                NumberValidator.NotRequired,
        ),
        val h2hQualificationRank: NumberFieldState<Int> = NumberFieldState(
                TypeValidator.IntValidator,
                NumberValidator.InRange(1..HeadToHeadUseCase.MAX_QUALI_RANK),
                NumberValidator.NotRequired,
        ),
        val h2hTotalArchers: NumberFieldState<Int> = NumberFieldState(
                TypeValidator.IntValidator,
                NumberValidator.InRange(1..HeadToHeadUseCase.MAX_QUALI_RANK),
                NumberValidator.NotRequired,
        ),

        /*
         * Dialogs
         */
        val isSelectRoundDialogOpen: Boolean = false,
        val isSelectSubTypeDialogOpen: Boolean = false,
        val enabledRoundFilters: SelectRoundEnabledFilters = SelectRoundEnabledFilters(),

        val selectRoundDialogState: SelectRoundDialogState = SelectRoundDialogState(),
        val selectFaceDialogState: SelectRoundFaceDialogState = SelectRoundFaceDialogState(
                round = selectRoundDialogState.selectedRound?.round,
                distances = selectRoundDialogState.roundSubTypeDistances?.map { it.distance }
        ),

        /*
         * Effects
         */
        val navigateToAddEnd: Int? = null,
        val popBackstack: Boolean = false,
        val roundNotFoundError: Boolean = false,
) {
    val isEditing by lazy { roundBeingEdited != null }

    /**
     * Number of arrows to be shot for [selectedRound]
     */
    val totalArrowsInSelectedRound by lazy {
        selectRoundDialogState.selectedRound?.roundArrowCounts?.sumOf { it.arrowCount }
    }

    /**
     * True if there are more [roundBeingEditedArrowsShot] than there are [totalArrowsInSelectedRound]
     */
    val tooManyArrowsWarningShown by lazy {
        selectRoundDialogState.selectedRound != null && (roundBeingEdited?.arrowsShot
                ?: 0) > totalArrowsInSelectedRound!!
    }

    /**
     * Convert the information on the screen to an [DatabaseShoot].
     * Edited rounds copy their old data, overwriting fields selected on the screen
     * New rounds defaults: [DatabaseShoot.shootId] is 0, [DatabaseShoot.archerId] is 1
     */
    fun asShoot() = DatabaseShoot(
            shootId = roundBeingEdited?.shoot?.shootId ?: 0,
            // TODO Check date locales (I want to store in UTC)
            dateShot = dateShot,
            archerId = roundBeingEdited?.shoot?.archerId,
    )

    fun asShootRound() =
            if (selectRoundDialogState.selectedRoundId == null) null
            else DatabaseShootRound(
                    shootId = roundBeingEdited?.shoot?.shootId ?: 0,
                    roundId = selectRoundDialogState.selectedRoundId,
                    roundSubTypeId = selectRoundDialogState.selectedSubTypeId,
                    faces = selectFaceDialogState.selectedFaces,
                    sightersCount = roundBeingEdited?.shootRound?.sightersCount ?: 0,
            )

    fun asShootDetail() =
            if (
                selectRoundDialogState.selectedRoundId != null || selectFaceDialogState.selectedFaces.isNullOrEmpty()
            ) null
            else DatabaseShootDetail(
                    shootId = roundBeingEdited?.shoot?.shootId ?: 0,
                    face = selectFaceDialogState.selectedFaces.firstOrNull(),
            )

    fun asHeadToHead() =
            if (type != NewScoreType.HEAD_TO_HEAD) null
            else if (h2hTeamSize.parsed == null) null
            else DatabaseHeadToHead(
                    shootId = roundBeingEdited?.shoot?.shootId ?: 0,
                    isSetPoints = h2hIsSetPoints,
                    teamSize = h2hTeamSize.parsed,
                    qualificationRank = h2hQualificationRank.parsed,
                    totalArchers = h2hTotalArchers.parsed,
                    endSize = (h2hEndSize.parsed ?: HeadToHeadUseCase.endSize(h2hTeamSize.parsed, false))
                            .takeIf { !h2hFormatIsStandard },
            )

    companion object {
        private fun getDefaultDate() = Calendar
                .getInstance(Locale.getDefault())
                .apply {
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
    }
}

enum class NewScoreType(
        val title: ResOrActual<String>,
) {
    SCORING(ResOrActual.StringResource(R.string.create_round__score_type_score)),
    COUNTING(ResOrActual.StringResource(R.string.create_round__score_type_count)),
    HEAD_TO_HEAD(ResOrActual.StringResource(R.string.create_round__score_type_head_to_head)),
    ;

    fun next(hasHeadToHeadCapability: Boolean): NewScoreType {
        val new = entries[(ordinal + 1) % entries.size]

        if (!hasHeadToHeadCapability && new == HEAD_TO_HEAD) {
            return new.next(false)
        }

        return new
    }
}
