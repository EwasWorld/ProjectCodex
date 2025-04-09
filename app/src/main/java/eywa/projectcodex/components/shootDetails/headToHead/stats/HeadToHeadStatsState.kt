package eywa.projectcodex.components.shootDetails.headToHead.stats

import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.roundHandicap

data class HeadToHeadStatsState(
        val fullShootInfo: FullShootInfo,
        val extras: Extras = Extras(),
        val classificationTablesUseCase: ClassificationTablesUseCase,
        val archerInfo: DatabaseArcher? = null,
        val bow: DatabaseBow? = null,
        val wa1440FullRoundInfo: FullRoundInfo? = null,
        val wa18FullRoundInfo: FullRoundInfo? = null,
        val useSimpleView: Boolean = false,
        val hasPermissionToSeeAdvanced: Boolean = false,
) {
    val classification = getClassification(
            classificationTables = classificationTablesUseCase,
            use2023System = fullShootInfo.use2023HandicapSystem,
            wa1440FullRoundInfo = wa1440FullRoundInfo,
    )

    private fun getClassification(
            classificationTables: ClassificationTablesUseCase,
            use2023System: Boolean,
            wa1440FullRoundInfo: FullRoundInfo?,
    ): Classification? = classificationTables.getClassification(
            archerInfo = archerInfo,
            bow = bow,
            fullShootInfo = fullShootInfo,
            use2023System = use2023System,
            wa1440FullRoundInfo = wa1440FullRoundInfo,
            wa18FullRoundInfo = wa18FullRoundInfo,
            handicap = fullShootInfo.h2hHandicapToIsSelf?.first?.roundHandicap(),
    )?.first

    data class Extras(
            val qualifyingRoundId: Int? = null,
            val editMainInfo: Boolean = false,
            val expandHandicaps: Boolean = false,
            val expandClassifications: Boolean = false,
            val editArcherInfo: Boolean = false,
            val viewQualifyingRound: Boolean = false,
    )
}
