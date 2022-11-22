package eywa.projectcodex.components.newScore

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType
import java.util.*

private fun getDefaultDate() = Calendar
        .getInstance(Locale.getDefault())
        .apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

data class NewScoreState(
        /**
         * Non-null if the fragment is being used to edit an existing round
         */
        val roundBeingEdited: ArcherRound? = null,
        /**
         * Only used if a round is being edited
         */
        val arrowsShot: Int? = null,
        val databaseUpdatingProgress: Boolean = false,
        val databaseUpdatingMessage: ResOrActual<String>? = null,
        val date: Calendar = getDefaultDate(),
        val isSelectRoundDialogOpen: Boolean = false,
        val isSelectSubTypeDialogOpen: Boolean = false,
        val selectedRound: Round? = null,
        val selectedSubtype: RoundSubType? = null,
        val roundsData: NewScoreDbData = NewScoreDbData(),
        val enabledRoundFilters: NewScoreRoundEnabledFilters = NewScoreRoundEnabledFilters(),
) {
    val hasRounds = !roundsData.rounds.isNullOrEmpty()
    val isEditing
        get() = roundBeingEdited != null

    val roundArrowCounts
        get() = selectedRound?.roundId?.let { roundId ->
            roundsData.arrowCounts?.filter { it.roundId == roundId }
        } ?: listOf()

    val roundSubTypes
        get() = selectedRound?.roundId?.let { roundId ->
            roundsData.subTypes?.filter { it.roundId == roundId }
        } ?: listOf()

    val roundDistances
        get() = selectedRound?.roundId?.let { roundId ->
            roundsData.distances?.filter { it.roundId == roundId }
        } ?: listOf()

    val roundSubtypeDistances
        get() = selectedSubtype?.subTypeId?.let { subtypeId ->
            roundDistances.filter { it.subTypeId == subtypeId }
        } ?: roundDistances

    val distanceUnitStringRes
        get() = when {
            selectedRound == null -> null
            selectedRound.isMetric -> R.string.units_meters_short
            else -> R.string.units_yards_short
        }

    val displayedSubtype = selectedSubtype?.takeIf { selectedRound != null && roundSubTypes.size > 1 }

    val roundsOnSelectDialog
        get() = enabledRoundFilters.filter(roundsData.rounds ?: listOf())

    val totalArrowsInSelectedRound
        get() = selectedRound?.let { roundArrowCounts.sumOf { it.arrowCount } }

    val tooManyArrowsWarningShown
        get() = selectedRound != null && (arrowsShot ?: 0) > totalArrowsInSelectedRound!!

    fun asArcherRound() = ArcherRound(
            archerRoundId = roundBeingEdited?.archerRoundId ?: 0,
            // TODO Check date locales (I want to store in UTC)
            dateShot = date.time,
            archerId = roundBeingEdited?.archerId ?: 1,
            roundId = selectedRound?.roundId,
            roundSubTypeId = selectedSubtype?.subTypeId,
    )

    fun getFurthestDistance(subType: RoundSubType) = roundsData.distances
            ?.filter { it.roundId == subType.roundId && it.subTypeId == subType.subTypeId }
            ?.maxByOrNull { it.distance }!!
}