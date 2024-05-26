package eywa.projectcodex.common.utils.classificationTables.model

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.ResOrActual.StringResource

enum class Classification(
        val rawKey: Int,
        val shortStringId: ResOrActual<String>,
        val fullStringId: ResOrActual<String>,
        val fullStringMultiline: ResOrActual<String>,
) {
    ARCHER_3RD_CLASS(
            rawKey = 1,
            shortStringId = StringResource(R.string.classification_tables__archer_3_short),
            fullStringId = StringResource(R.string.classification_tables__archer_3_full),
            fullStringMultiline = StringResource(R.string.classification_tables__archer_3_full_multiline),
    ),
    ARCHER_2ND_CLASS(
            rawKey = 2,
            shortStringId = StringResource(R.string.classification_tables__archer_2_short),
            fullStringId = StringResource(R.string.classification_tables__archer_2_full),
            fullStringMultiline = StringResource(R.string.classification_tables__archer_2_full_multiline),
    ),
    ARCHER_1ST_CLASS(
            rawKey = 3,
            shortStringId = StringResource(R.string.classification_tables__archer_1_short),
            fullStringId = StringResource(R.string.classification_tables__archer_1_full),
            fullStringMultiline = StringResource(R.string.classification_tables__archer_1_full_multiline),
    ),
    BOWMAN_3RD_CLASS(
            rawKey = 4,
            shortStringId = StringResource(R.string.classification_tables__bowman_3_short),
            fullStringId = StringResource(R.string.classification_tables__bowman_3_full),
            fullStringMultiline = StringResource(R.string.classification_tables__bowman_3_full_multiline),
    ),
    BOWMAN_2ND_CLASS(
            rawKey = 5,
            shortStringId = StringResource(R.string.classification_tables__bowman_2_short),
            fullStringId = StringResource(R.string.classification_tables__bowman_2_full),
            fullStringMultiline = StringResource(R.string.classification_tables__bowman_2_full_multiline),
    ),
    BOWMAN_1ST_CLASS(
            rawKey = 6,
            shortStringId = StringResource(R.string.classification_tables__bowman_1_short),
            fullStringId = StringResource(R.string.classification_tables__bowman_1_full),
            fullStringMultiline = StringResource(R.string.classification_tables__bowman_1_full_multiline),
    ),
    MASTER_BOWMAN(
            rawKey = 7,
            shortStringId = StringResource(R.string.classification_tables__master_bowman_short),
            fullStringId = StringResource(R.string.classification_tables__master_bowman_full),
            fullStringMultiline = StringResource(R.string.classification_tables__master_bowman_full_multiline),
    ),
    GRAND_MASTER_BOWMAN(
            rawKey = 8,
            shortStringId = StringResource(R.string.classification_tables__grand_master_bowman_short),
            fullStringId = StringResource(R.string.classification_tables__grand_master_bowman_full),
            fullStringMultiline = StringResource(R.string.classification_tables__grand_master_bowman_full_multiline),
    ),
    ELITE_MASTER_BOWMAN(
            rawKey = 9,
            shortStringId = StringResource(R.string.classification_tables__elite_grand_master_bowman_short),
            fullStringId = StringResource(R.string.classification_tables__elite_grand_master_bowman_full),
            fullStringMultiline = StringResource(
                    R.string.classification_tables__elite_grand_master_bowman_full_multiline
            ),
    ),
    ;

    val isArcher
        get() = ordinal < 3

    companion object {
        val rawKeyBackwardsMap = values().associateBy { it.rawKey }
    }
}
