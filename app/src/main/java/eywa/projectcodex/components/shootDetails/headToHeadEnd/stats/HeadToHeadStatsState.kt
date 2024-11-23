package eywa.projectcodex.components.shootDetails.headToHeadEnd.stats

import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.model.roundHandicap

data class HeadToHeadStatsState(
        val fullShootInfo: FullShootInfo,
        val extras: Extras = Extras(),
        val classificationTablesUseCase: ClassificationTablesUseCase,
        val archerInfo: DatabaseArcher? = null,
        val bow: DatabaseBow? = null,
        val wa1440FullRoundInfo: FullRoundInfo? = null,
) {
    val classification = getClassification(
            classificationTables = classificationTablesUseCase,
            use2023System = fullShootInfo.use2023HandicapSystem,
            wa1440FullRoundInfo = wa1440FullRoundInfo,
    )

    /**
     * @return classification of the current score TO isOfficialClassification
     */
    private fun getClassification(
            classificationTables: ClassificationTablesUseCase,
            use2023System: Boolean,
            wa1440FullRoundInfo: FullRoundInfo?,
    ): Classification? {
        if (
            archerInfo == null
            || bow == null
            || fullShootInfo.fullRoundInfo == null
            || fullShootInfo.arrowCounter != null
            || fullShootInfo.arrowsShot == 0
        ) return null

        val handicap = fullShootInfo.h2hHandicapToIsSelf?.first?.roundHandicap()

        val trueClassification = classificationTables.get(
                isGent = archerInfo.isGent,
                age = archerInfo.age,
                bow = bow.type,
                fullRoundInfo = fullShootInfo.fullRoundInfo!!,
                roundSubTypeId = fullShootInfo.roundSubType?.subTypeId,
                isTripleFace = fullShootInfo.faces == listOf(RoundFace.TRIPLE),
                use2023Handicaps = use2023System,
        )
                ?.takeIf { it.isNotEmpty() }
                ?.filter { (it.handicap ?: 0) >= (handicap ?: Handicap.maxHandicap(use2023System)) }
                ?.maxByOrNull { it.score!! }
                ?.classification
        val roughClassification = wa1440FullRoundInfo?.let {
            classificationTables.getRoughHandicaps(
                    isGent = archerInfo.isGent,
                    age = archerInfo.age,
                    bow = bow.type,
                    wa1440RoundInfo = wa1440FullRoundInfo,
                    use2023Handicaps = use2023System,
            )
        }
                ?.takeIf { it.isNotEmpty() }
                ?.filter { (it.handicap ?: 0) >= (handicap ?: Handicap.maxHandicap(use2023System)) }
                ?.maxByOrNull { it.score!! }
                ?.classification

        return when {
            trueClassification == null && roughClassification == null -> null
            trueClassification == null || roughClassification == null -> trueClassification ?: roughClassification
            trueClassification.ordinal >= roughClassification.ordinal -> trueClassification
            else -> roughClassification
        }
    }

    data class Extras(
            val qualifyingRoundId: Int? = null,
            val editMainInfo: Boolean = false,
            val expandHandicaps: Boolean = false,
            val expandClassifications: Boolean = false,
            val editArcherInfo: Boolean = false,
            val viewQualifyingRound: Boolean = false,
    )
}
