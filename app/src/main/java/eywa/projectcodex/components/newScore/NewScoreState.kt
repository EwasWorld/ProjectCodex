package eywa.projectcodex.components.newScore

import eywa.projectcodex.R
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import java.util.*

data class NewScoreState(
        val isEditing: Boolean,
        val arrowsShot: Int?,
        /**
         * Pair<progress, totalItems>. Null if the database update is not in progress
         */
        val databaseUpdatingProgress: Pair<Int, Int>?,
        val date: Calendar,
        val isSelectRoundOpen: Boolean,
        val isSelectSubTypeOpen: Boolean,
        val selectedRound: Round? = null,
        val selectedSubtype: RoundSubType? = null,
        val allRounds: List<Round>,
        val allSubTypes: List<RoundSubType>,
        val allArrowCounts: List<RoundArrowCount>,
        val allDistances: List<RoundDistance>,
        val enabledSelectRoundDialogFilters: NewScoreRoundEnabledFilters,
) {
    val roundArrowCounts
        get() = selectedRound?.roundId?.let { roundId ->
            allArrowCounts.filter { it.roundId == roundId }.sortedBy { it.distanceNumber }
        } ?: listOf()

    val roundSubTypes
        get() = selectedRound?.roundId?.let { roundId ->
            allSubTypes.filter { it.roundId == roundId }.sortedBy { it.subTypeId }
        } ?: listOf()

    val roundDistances
        get() = selectedRound?.roundId?.let { roundId ->
            allDistances.filter { it.roundId == roundId }.sortedBy { it.distanceNumber }
        } ?: listOf()

    val distanceUnitStringRes
        get() = when {
            selectedRound == null -> null
            selectedRound.isMetric -> R.string.units_meters_short
            else -> R.string.units_yards_short
        }

    val displayedSubtype = when {
        selectedRound == null -> null
        roundSubTypes.isEmpty() -> null
        roundSubTypes.size == 1 -> roundSubTypes[0]
        else -> selectedSubtype
    }

    val roundsOnSelectDialog
        get() = enabledSelectRoundDialogFilters.filter(allRounds)

    val totalArrowsInSelectedRound
        get() = selectedRound?.let { roundArrowCounts.sumOf { it.arrowCount } }

    val tooManyArrowsWarningShown
        get() = selectedRound != null && (arrowsShot ?: 0) > totalArrowsInSelectedRound!!

    fun getFurthestDistance(subType: RoundSubType) = roundDistances
            .find { it.roundId == subType.roundId && it.subTypeId == subType.subTypeId }!!
}