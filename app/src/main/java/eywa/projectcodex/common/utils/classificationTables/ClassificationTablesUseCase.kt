package eywa.projectcodex.common.utils.classificationTables

import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound.DbRoundRef
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.model.FullShootInfo
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
            isTripleFace: Boolean,
            use2023Handicaps: Boolean = true,
    ) = fullRoundInfo.round.defaultRoundId?.let { defaultRoundId ->
        data.filter {
            it.isGent == isGent
                    && it.age == age
                    && bow == it.bowStyle
                    && it.round == DbRoundRef(defaultRoundId, roundSubTypeId)
                    && (it.round.defaultRoundId != RoundRepo.VEGAS_DEFAULT_ROUND_ID || isTripleFace)
        }.map {
            it.copy(
                    handicap = Handicap.getHandicapForRound(
                            round = fullRoundInfo,
                            subType = roundSubTypeId,
                            score = it.score!!,
                            innerTenArcher = bow == ClassificationBow.COMPOUND,
                            use2023Handicaps = use2023Handicaps,
                    )?.roundHandicap(),
            )
        }.sortedBy { it.classification.ordinal }
    }

    fun getRoughHandicaps(
            isGent: Boolean,
            age: ClassificationAge,
            bow: ClassificationBow,
            wa1440RoundInfo: FullRoundInfo?,
            wa18RoundInfo: FullRoundInfo?,
            isOutdoor: Boolean,
            use2023Handicaps: Boolean = true,
    ): List<ClassificationTableEntry>? {
        check(
                wa1440RoundInfo == null || wa1440RoundInfo.round.defaultRoundId == RoundRepo.WA_1440_DEFAULT_ROUND_ID,
        ) { "Incorrect round provided" }
        check(
                wa18RoundInfo == null || wa18RoundInfo.round.defaultRoundId == RoundRepo.WA_18_DEFAULT_ROUND_ID,
        ) { "Incorrect round provided" }

        val classifications = (
                if (isOutdoor) get(isGent, age, bow, wa1440RoundInfo!!, 1, use2023Handicaps)
                else get(isGent, age, bow, wa18RoundInfo!!, 1, use2023Handicaps)
                )?.toMutableList() ?: return null

        if (isOutdoor && bow == ClassificationBow.LONGBOW) {
            val metricV = get(isGent, age, bow, wa1440RoundInfo!!, 6, use2023Handicaps)
            classifications
                    .filter { it.classification.isArcher && it.handicap!! > 100 }
                    .forEach { entry ->
                        val new = metricV?.find { it.classification == entry.classification } ?: return@forEach
                        classifications.remove(entry)
                        classifications.add(new)
                    }
        }

        // EMB is not awarded for indoor rounds
        return classifications.filter { isOutdoor || it.classification != Classification.ELITE_MASTER_BOWMAN }
    }

    /**
     * @return classification of the current score to isOfficialClassification
     */
    fun getClassification(
            archerInfo: DatabaseArcher?,
            bow: DatabaseBow?,
            fullShootInfo: FullShootInfo,
            use2023System: Boolean,
            wa1440FullRoundInfo: FullRoundInfo?,
            wa18FullRoundInfo: FullRoundInfo?,
            handicap: Int? = null,
    ): Pair<Classification, Boolean>? {
        if (
            archerInfo == null
            || bow == null
            || fullShootInfo.fullRoundInfo == null
            || fullShootInfo.arrowCounter != null
            || fullShootInfo.arrowsShot == 0
        ) return null

        val isH2h = fullShootInfo.h2h != null
        val roundHandicap = handicap ?: fullShootInfo.handicap ?: return null

        val trueClassification = get(
                isGent = archerInfo.isGent,
                age = archerInfo.age,
                bow = bow.type,
                fullRoundInfo = fullShootInfo.fullRoundInfo!!,
                roundSubTypeId = fullShootInfo.roundSubType?.subTypeId,
                isTripleFace = fullShootInfo.faces == listOf(RoundFace.TRIPLE),
                use2023Handicaps = use2023System,
        )
                ?.takeIf { it.isNotEmpty() }
                ?.filter { (it.handicap ?: 0) >= roundHandicap }
                ?.maxByOrNull { it.score!! }
                ?.classification
                // H2h will always be unofficial
                ?.to(!isH2h)
        val roughClassification = wa1440FullRoundInfo?.let {
            getRoughHandicaps(
                    isGent = archerInfo.isGent,
                    age = archerInfo.age,
                    bow = bow.type,
                    wa1440RoundInfo = wa1440FullRoundInfo,
                    wa18RoundInfo = wa18FullRoundInfo,
                    use2023Handicaps = use2023System,
                    isOutdoor = fullShootInfo.fullRoundInfo!!.round.isOutdoor,
            )
        }
                ?.takeIf { it.isNotEmpty() }
                ?.filter { (it.handicap ?: 0) >= roundHandicap }
                ?.maxByOrNull { it.score!! }
                ?.classification
                ?.to(false)

        return when {
            trueClassification == null && roughClassification == null -> null
            trueClassification == null || roughClassification == null -> trueClassification ?: roughClassification
            trueClassification.first.ordinal >= roughClassification.first.ordinal -> trueClassification
            else -> roughClassification
        }
    }
}
