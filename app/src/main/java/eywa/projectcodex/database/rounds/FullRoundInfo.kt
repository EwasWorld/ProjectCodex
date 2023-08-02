package eywa.projectcodex.database.rounds

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Embedded
import androidx.room.Relation


data class FullRoundInfo(
        @Embedded val round: Round,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundSubTypes: List<RoundSubType>? = null,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundArrowCounts: List<RoundArrowCount>? = null,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundDistances: List<RoundDistance>? = null,
) {
    fun getDistanceUnitRes() = round.getDistanceUnitRes()

    @Composable
    fun getDistanceUnit() = getDistanceUnitRes()?.let { stringResource(it) }

    fun getDistances(subTypeId: Int?) =
            when {
                roundSubTypes == null || roundSubTypes.size <= 1 -> roundDistances
                else -> roundDistances?.filter { it.subTypeId == (subTypeId ?: 1) }
            }
}
