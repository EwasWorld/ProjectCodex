package eywa.projectcodex.components.referenceTables.rankingPoints

import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata

class RankingPointsUseCase(
        val data: List<RankingPointsEntry>,
) {
    /**
     * Take a CSV file and create RankingPointsUseCase object
     *
     * @see RankingPointsEntry.fromString
     */
    constructor(rawString: String) :
            this(rawString.split("\n").drop(2).mapNotNull { RankingPointsEntry.fromString(it.removeSuffix("\r")) })
}

data class RankingPointsEntry(
        val rank: Int,
        val tierOneRankingRound: Double,
        val tierOneH2h: Double,
        val tierTwoPlus: Double,
        val tierTwoRankingRound: Double,
        val tierTwoH2h: Double,
        val tierThree: Double,
) : CodexGridRowMetadata {
    override fun isBoldText(): Boolean = rank % 4 == 0

    companion object {
        fun fromString(value: String): RankingPointsEntry? {
            if (value.isEmpty()) return null

            val split = value.split(",")
            return RankingPointsEntry(
                    rank = split[0].toInt(),
                    tierOneRankingRound = split[1].toDouble(),
                    tierOneH2h = split[2].toDouble(),
                    tierTwoPlus = split[3].toDouble(),
                    tierTwoRankingRound = split[4].toDouble(),
                    tierTwoH2h = split[5].toDouble(),
                    tierThree = split[6].toDouble(),
            )
        }
    }
}
