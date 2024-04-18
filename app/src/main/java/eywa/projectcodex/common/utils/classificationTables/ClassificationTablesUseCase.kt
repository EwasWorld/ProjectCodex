package eywa.projectcodex.common.utils.classificationTables

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound.DbRoundRef
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.model.roundHandicap

data class ClassificationTablesUseCase(
        private val data: List<ClassificationTableEntry>,
) {
    /**
     * Take a CSV file and create ClassificationTables object
     *
     * @see ClassificationTableEntry.fromString
     */
    constructor(rawString: String) :
            this(rawString.split("\n").mapNotNull { ClassificationTableEntry.fromString(it.removeSuffix("\r")) })

    fun get(
            isGent: Boolean,
            age: ClassificationAge,
            bow: ClassificationBow,
            fullRoundInfo: FullRoundInfo,
            roundSubTypeId: Int?,
            use2023Handicaps: Boolean = true,
    ) = fullRoundInfo.round.defaultRoundId?.let { defaultRoundId ->
        data.filter {
            it.isGent == isGent
                    && it.age == age
                    && bow == it.bowStyle
                    && it.round == DbRoundRef(defaultRoundId, roundSubTypeId)
        }.map {
            it.copy(
                    handicap = Handicap.getHandicapForRound(
                            round = fullRoundInfo,
                            subType = roundSubTypeId,
                            score = it.score!!,
                            innerTenArcher = bow == ClassificationBow.COMPOUND,
                            use2023Handicaps = use2023Handicaps,
                    )?.roundHandicap()
            )
        }.sortedBy { it.classification.ordinal }
    }

    fun getRoughHandicaps(
            isGent: Boolean,
            age: ClassificationAge,
            bow: ClassificationBow,
            wa1440RoundInfo: FullRoundInfo,
            use2023Handicaps: Boolean = true,
    ): List<ClassificationTableEntry>? {
        check(
                wa1440RoundInfo.round.defaultRoundId == RoundRepo.WA_1440_DEFAULT_ROUND_ID
        ) { "Incorrect round provided" }

        val gents1440 = get(isGent, age, bow, wa1440RoundInfo, 1, use2023Handicaps)?.toMutableList()
                ?: return null
        val metricV = get(isGent, age, bow, wa1440RoundInfo, 6, use2023Handicaps)

        if (bow == ClassificationBow.LONGBOW) {
            gents1440.filter { it.classification.isArcher && it.handicap!! > 100 }.forEach { entry ->
                val new = metricV?.find { it.classification == entry.classification } ?: return@forEach
                gents1440.remove(entry)
                gents1440.add(new)
            }
        }

        return gents1440
    }
}
