package eywa.projectcodex.components.newScore

import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.rounds.distanceUnitStringRes
import java.util.*

data class NewScoreState(
        /**
         * Non-null if the fragment is being used to edit an existing round
         */
        val roundBeingEdited: ArcherRound? = null,
        /**
         * Only used if a round is being edited
         */
        val roundBeingEditedArrowsShot: Int? = null,
        /**
         * Should block round selection while this is true
         */
        val updateDefaultRoundsState: UpdateDefaultRoundsState? = null,

        val roundsData: List<FullRoundInfo>? = null,

        /*
         * Dialogs
         */
        val isSelectRoundDialogOpen: Boolean = false,
        val isSelectSubTypeDialogOpen: Boolean = false,
        val enabledRoundFilters: SelectRoundEnabledFilters = SelectRoundEnabledFilters(),

        /*
         * User-set info
         */
        val dateShot: Calendar = getDefaultDate(),
        val selectedRound: Round? = null,
        val selectedSubtype: RoundSubType? = null,

        /*
         * Effects
         */
        val navigateToInputEnd: Int? = null,
        val popBackstack: Boolean = false,
) {
    val isEditing by lazy { roundBeingEdited != null }

    val isUpdateDefaultRoundsInProgress by lazy {
        when (updateDefaultRoundsState) {
            UpdateDefaultRoundsState.Initialising,
            is UpdateDefaultRoundsState.StartProcessingNew,
            is UpdateDefaultRoundsState.DeletingOld -> true

            null,
            is UpdateDefaultRoundsState.TemporaryError,
            is UpdateDefaultRoundsState.InternalError,
            is UpdateDefaultRoundsState.Complete -> false
        }
    }

    val selectedRoundInfo by lazy {
        selectedRound?.roundId?.let { roundId ->
            roundsData?.find { it.round.roundId == roundId }
        }
    }

    /**
     * All distances for [selectedSubtype]
     */
    val roundSubtypeDistances by lazy {
        selectedSubtype?.subTypeId?.let { subtypeId ->
            selectedRoundInfo?.roundDistances?.filter { it.subTypeId == subtypeId }
        } ?: selectedRoundInfo?.roundDistances
    }

    /**
     * Resource id of the unit for [selectedRound]'s distances (e.g. yd/m)
     */
    val distanceUnitStringRes by lazy { selectedRound?.distanceUnitStringRes() }

    /**
     * The subtype to display on the screen. No subtype is shown if no round selected or if there's only one subtype
     */
    val displayedSubtype by lazy {
        when {
            selectedRound != null && (selectedRoundInfo?.roundSubTypes?.size ?: 0) > 1 -> selectedSubtype!!
            else -> null
        }
    }

    /**
     * The round to display on the screen. Round name when one is selected, 'No Round' if no round is selected,
     * 'No rounds in database' if nothing in the database
     * TODO Turn no rounds into a different field and show a warning message instead
     */
    val displayedRound by lazy {
        when {
            selectedRound != null -> ResOrActual.fromActual(selectedRound.displayName)
            roundsData.isNullOrEmpty() -> ResOrActual.fromRes(R.string.create_round__no_rounds_found)
            else -> ResOrActual.fromRes(R.string.create_round__no_round)
        }
    }

    /**
     * Rounds to be displayed on the round select dialog. Filtered by [enabledRoundFilters]
     */
    val roundsOnSelectDialog by lazy {
        enabledRoundFilters.filter(roundsData?.map { it.round } ?: listOf())
    }

    /**
     * Number of arrows to be shot for [selectedRound]
     */
    val totalArrowsInSelectedRound by lazy { selectedRoundInfo?.roundArrowCounts?.sumOf { it.arrowCount } }

    /**
     * True if there are more [roundBeingEditedArrowsShot] than there are [totalArrowsInSelectedRound]
     */
    val tooManyArrowsWarningShown by lazy {
        selectedRound != null && (roundBeingEditedArrowsShot ?: 0) > totalArrowsInSelectedRound!!
    }

    /**
     * Convert the information on the screen to an [ArcherRound].
     * Edited rounds copy their old data, overwriting fields selected on the screen
     * New rounds defaults: [ArcherRound.archerRoundId] is 0, [ArcherRound.archerId] is 1
     */
    fun asArcherRound() = ArcherRound(
            archerRoundId = roundBeingEdited?.archerRoundId ?: 0,
            // TODO Check date locales (I want to store in UTC)
            dateShot = dateShot,
            archerId = roundBeingEdited?.archerId ?: 1,
            roundId = selectedRound?.roundId,
            roundSubTypeId = selectedSubtype?.subTypeId,
    )

    fun getFurthestDistance(subType: RoundSubType) = roundsData
            ?.find { it.round.roundId == subType.roundId }
            ?.roundDistances
            ?.filter { it.subTypeId == subType.subTypeId }
            ?.maxByOrNull { it.distance }!!

    companion object {
        private fun getDefaultDate() = Calendar
                .getInstance(Locale.getDefault())
                .apply {
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
    }
}
