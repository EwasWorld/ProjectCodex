package eywa.projectcodex.database.rounds

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Embedded
import androidx.room.Relation


fun FullRoundInfo.getDistanceUnitRes() = round.getDistanceUnitRes()

@Composable
fun FullRoundInfo.getDistanceUnit() = getDistanceUnitRes()?.let { stringResource(it) }

fun FullRoundInfo.getDistances(subTypeId: Int?) =
        when {
            roundSubTypes == null || roundSubTypes.size <= 1 -> roundDistances
            else -> roundDistances?.filter { it.subTypeId == (subTypeId ?: 1) }
        }


data class FullRoundInfo(
        @Embedded val round: Round,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundSubTypes: List<RoundSubType>? = null,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundArrowCounts: List<RoundArrowCount>? = null,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundDistances: List<RoundDistance>? = null,
)
