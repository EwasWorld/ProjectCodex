package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.*
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
        val updateDefaultRoundsState: UpdateDefaultRoundsState = UpdateDefaultRoundsState.NotStarted,

        /*
         * User-set info
         */
        val dateShot: Calendar = getDefaultDate(),

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
        val navigateToInputEnd: Int? = null,
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
        selectRoundDialogState.selectedRound != null && (roundBeingEditedArrowsShot ?: 0) > totalArrowsInSelectedRound!!
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
            roundId = selectRoundDialogState.selectedRoundId,
            roundSubTypeId = selectRoundDialogState.selectedSubTypeId,
            faces = selectFaceDialogState.selectedFaces,
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
