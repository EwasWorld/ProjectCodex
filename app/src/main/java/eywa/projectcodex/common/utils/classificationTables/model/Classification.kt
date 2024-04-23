package eywa.projectcodex.common.utils.classificationTables.model

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual

enum class Classification(
        val rawKey: Int,
        val shortStringId: ResOrActual<String>,
        val fullStringId: ResOrActual<String>,
) {
    ARCHER_3RD_CLASS(
            rawKey = 1,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__archer_3_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__archer_3_full),
    ),
    ARCHER_2ND_CLASS(
            rawKey = 2,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__archer_2_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__archer_2_full),
    ),
    ARCHER_1ST_CLASS(
            rawKey = 3,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__archer_1_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__archer_1_full),
    ),
    BOWMAN_3RD_CLASS(
            rawKey = 4,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__bowman_3_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__bowman_3_full),
    ),
    BOWMAN_2ND_CLASS(
            rawKey = 5,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__bowman_2_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__bowman_2_full),
    ),
    BOWMAN_1ST_CLASS(
            rawKey = 6,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__bowman_1_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__bowman_1_full),
    ),
    MASTER_BOWMAN(
            rawKey = 7,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__master_bowman_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__master_bowman_full),
    ),
    GRAND_MASTER_BOWMAN(
            rawKey = 8,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__grand_master_bowman_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__grand_master_bowman_full),
    ),
    ELITE_MASTER_BOWMAN(
            rawKey = 9,
            shortStringId = ResOrActual.StringResource(R.string.classification_tables__elite_grand_master_bowman_short),
            fullStringId = ResOrActual.StringResource(R.string.classification_tables__elite_grand_master_bowman_full),
    ),
    ;

    val isArcher
        get() = ordinal < 3

    companion object {
        val rawKeyBackwardsMap = values().associateBy { it.rawKey }
    }
}
