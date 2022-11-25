package eywa.projectcodex.components.newScore

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.newScore.helpers.NewScoreRoundEnabledFilters
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType
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
         * True if the database currently being updated. Should block round selection while this is true
         */
        val databaseUpdatingProgress: Boolean = false,
        val databaseUpdatingMessage: ResOrActual<String>? = null,

        val roundsData: NewScoreDbData = NewScoreDbData(),

        /*
         * Dialogs
         */
        val isSelectRoundDialogOpen: Boolean = false,
        val isSelectSubTypeDialogOpen: Boolean = false,
        val enabledRoundFilters: NewScoreRoundEnabledFilters = NewScoreRoundEnabledFilters(),

        /*
         * User-set info
         */
        val dateShot: Calendar = getDefaultDate(),
        val selectedRound: Round? = null,
        val selectedSubtype: RoundSubType? = null,
) {
    val isEditing
        get() = roundBeingEdited != null

    /**
     * All arrow counts for [selectedRound]
     */
    val roundArrowCounts
        get() = selectedRound?.roundId?.let { roundId ->
            roundsData.arrowCounts?.filter { it.roundId == roundId }
        } ?: listOf()

    /**
     * All subtypes for [selectedRound]
     */
    val roundSubTypes
        get() = selectedRound?.roundId?.let { roundId ->
            roundsData.subTypes?.filter { it.roundId == roundId }
        } ?: listOf()

    /**
     * All distances for [selectedRound]
     */
    val roundDistances
        get() = selectedRound?.roundId?.let { roundId ->
            roundsData.distances?.filter { it.roundId == roundId }
        } ?: listOf()

    /**
     * All distances for [selectedSubtype]
     */
    val roundSubtypeDistances
        get() = selectedSubtype?.subTypeId?.let { subtypeId ->
            roundDistances.filter { it.subTypeId == subtypeId }
        } ?: roundDistances

    /**
     * Resource id of the unit for [selectedRound]'s distances (e.g. yd/m)
     */
    val distanceUnitStringRes
        get() = when {
            selectedRound == null -> null
            selectedRound.isMetric -> R.string.units_meters_short
            else -> R.string.units_yards_short
        }

    /**
     * The subtype to display on the screen. No subtype is shown if no round selected or if there's only one subtype
     */
    val displayedSubtype
        get() = when {
            selectedRound != null && roundSubTypes.size > 1 -> selectedSubtype!!
            else -> null
        }

    /**
     * The round to display on the screen. Round name when one is selected, 'No Round' if no round is selected,
     * 'No rounds in database' if nothing in the database
     * TODO Turn no rounds into a different field and show a warning message instead
     */
    val displayedRound
        get() = when {
            selectedRound != null -> ResOrActual.fromActual(selectedRound.displayName)
            roundsData.rounds.isNullOrEmpty() -> ResOrActual.fromRes(R.string.create_round__no_rounds_found)
            else -> ResOrActual.fromRes(R.string.create_round__no_round)
        }

    /**
     * Rounds to be displayed on the round select dialog. Filtered by [enabledRoundFilters]
     */
    val roundsOnSelectDialog
        get() = enabledRoundFilters.filter(roundsData.rounds ?: listOf())

    /**
     * Number of arrows to be shot for [selectedRound]
     */
    val totalArrowsInSelectedRound
        get() = selectedRound?.let { roundArrowCounts.sumOf { it.arrowCount } }

    /**
     * True if there are more [roundBeingEditedArrowsShot] than there are [totalArrowsInSelectedRound]
     */
    val tooManyArrowsWarningShown
        get() = selectedRound != null && (roundBeingEditedArrowsShot ?: 0) > totalArrowsInSelectedRound!!

    /**
     * Convert the information on the screen to an [ArcherRound].
     * Edited rounds copy their old data, overwriting fields selected on the screen
     * New rounds defaults: [ArcherRound.archerRoundId] is 0, [ArcherRound.archerId] is 1
     */
    fun asArcherRound() = ArcherRound(
            archerRoundId = roundBeingEdited?.archerRoundId ?: 0,
            // TODO Check date locales (I want to store in UTC)
            dateShot = dateShot.time,
            archerId = roundBeingEdited?.archerId ?: 1,
            roundId = selectedRound?.roundId,
            roundSubTypeId = selectedSubtype?.subTypeId,
    )

    fun getFurthestDistance(subType: RoundSubType) = roundsData.distances
            ?.filter { it.roundId == subType.roundId && it.subTypeId == subType.subTypeId }
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
