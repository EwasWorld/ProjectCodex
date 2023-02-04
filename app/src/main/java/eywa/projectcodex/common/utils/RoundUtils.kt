package eywa.projectcodex.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.database.rounds.FullRoundInfo

@Composable
fun FullRoundInfo.getDistanceUnit() = stringResource(
        if (round.isMetric) R.string.units_meters_short else R.string.units_yards_short
)

fun FullRoundInfo.getDistances(subTypeId: Int?) =
        when {
            roundSubTypes == null || roundSubTypes.size <= 1 -> roundDistances
            subTypeId == null -> throw IllegalArgumentException("Must specify a subtype")
            else -> roundDistances?.filter { it.subTypeId == subTypeId }
        }
