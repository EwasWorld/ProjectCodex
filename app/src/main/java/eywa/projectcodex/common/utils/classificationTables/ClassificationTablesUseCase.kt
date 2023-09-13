package eywa.projectcodex.common.utils.classificationTables

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound.DbRoundRef
import eywa.projectcodex.database.rounds.FullRoundInfo
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
                    && it.rounds.contains(DbRoundRef(defaultRoundId, roundSubTypeId))
        }.map {
            it.copy(
                    handicap = Handicap.getHandicapForRound(
                            round = fullRoundInfo,
                            subType = roundSubTypeId,
                            score = it.score,
                            innerTenArcher = bow == ClassificationBow.COMPOUND,
                            use2023Handicaps = use2023Handicaps,
                    )?.roundHandicap()
            )
        }.sortedBy { it.classification.ordinal }
    }
}
