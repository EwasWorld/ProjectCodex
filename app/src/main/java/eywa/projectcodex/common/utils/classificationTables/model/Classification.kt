package eywa.projectcodex.common.utils.classificationTables.model

import androidx.annotation.StringRes
import eywa.projectcodex.R

enum class Classification(val rawName: String, val rawKey: Int, @StringRes val shortStringId: Int) {
    ARCHER_3RD_CLASS("Archer 3rd Class", 1, R.string.classification_tables__archer_3_short),
    ARCHER_2ND_CLASS("Archer 2nd Class", 2, R.string.classification_tables__archer_2_short),
    ARCHER_1ST_CLASS("Archer 1st Class", 3, R.string.classification_tables__archer_1_short),
    BOWMAN_3RD_CLASS("Bowman 3rd Class", 4, R.string.classification_tables__bowman_3_short),
    BOWMAN_2ND_CLASS("Bowman 2nd Class", 5, R.string.classification_tables__bowman_2_short),
    BOWMAN_1ST_CLASS("Bowman 1st Class", 6, R.string.classification_tables__bowman_1_short),
    MASTER_BOWMAN("Master Bowman", 7, R.string.classification_tables__master_bowman_short),
    GRAND_MASTER_BOWMAN("Grand Master Bowman", 8, R.string.classification_tables__grand_master_bowman_short),
    ELITE_MASTER_BOWMAN("Elite Master Bowman", 9, R.string.classification_tables__elite_grand_master_bowman_short),
    ;

    companion object {
        val backwardsMap = values().associateBy { it.rawKey }
    }
}
