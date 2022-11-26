package eywa.projectcodex.components.newScore

import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType

data class DbRoundsData(
        val rounds: List<Round>? = null,
        val subTypes: List<RoundSubType>? = null,
        val arrowCounts: List<RoundArrowCount>? = null,
        val distances: List<RoundDistance>? = null,
)