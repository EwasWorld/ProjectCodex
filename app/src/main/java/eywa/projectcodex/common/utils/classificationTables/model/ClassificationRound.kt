package eywa.projectcodex.common.utils.classificationTables.model

enum class ClassificationRound(val rawName: String, val rounds: List<DbRoundRef>) {
    YORK("York", 1, 1),
    HEREFORD_BRISTOL_I("Hereford / Bristol I", 1, 2),
    BRISTOL_II("Bristol II", 1, 3),
    BRISTOL_III("Bristol III", 1, 4),
    BRISTOL_IV("Bristol IV", 1, 5),
    BRISTOL_V("Bristol V", 1, 6),
    ST_GEORGE("St. George", 2, 1),
    ALBION("Albion", 2, 2),
    WINDSOR("Windsor", 2, 3),
    WINDSOR_50("Windsor 50", 2, 4),
    WINDSOR_40("Windsor 40", 2, 5),
    WINDSOR_30("Windsor 30", 2, 6),
    NEW_WESTERN("New Western", 3, 1),
    LONG_WESTERN("Long Western", 3, 2),
    WESTERN("Western", 3, 3),
    WESTERN_50("Western 50", 3, 4),
    WESTERN_40("Western 40", 3, 5),
    WESTERN_30("Western 30", 3, 6),
    AMERICAN("American", 4),
    ST_NICHOLAS("St. Nicholas", 5),
    NEW_NATIONAL("New National", 6, 1),
    LONG_NATIONAL("Long National", 6, 2),
    NATIONAL("National", 6, 3),
    NATIONAL_50("National 50", 6, 4),
    NATIONAL_40("National 40", 6, 5),
    NATIONAL_30("National 30", 6, 6),
    NEW_WARWICK("New Warwick", 7, 1),
    LONG_WARWICK("Long Warwick", 7, 2),
    WARWICK("Warwick", 7, 3),
    WARWICK_50("Warwick 50", 7, 4),
    WARWICK_40("Warwick 40", 7, 5),
    WARWICK_30("Warwick 30", 7, 6),
    WA_1440_90M("WA 1440 (90m)", 8, 1),
    WA_1440_70M_METRIC_I("WA 1440 (70m) / Metric I", 8, 2),
    WA_1440_60M_METRIC_II("WA 1440 (60m) / Metric II", 8, 3),
    METRIC_III("Metric III", 8, 4),
    METRIC_IV("Metric IV", 8, 5),
    METRIC_V("Metric V", 8, 6),
    LONG_METRIC_MEN("Long Metric (Men)", 9, 1),
    LONG_METRIC_WOMEN_I("Long Metric (Women) / I", 9, 2),
    LONG_METRIC_II("Long Metric II", 9, 3),
    LONG_METRIC_III("Long Metric III", 9, 4),
    LONG_METRIC_IV("Long Metric IV", 9, 5),
    LONG_METRIC_V("Long Metric V", 9, 6),
    SHORT_METRIC_I("Short Metric I", 10, 1),
    SHORT_METRIC_II("Short Metric II", 10, 2),
    SHORT_METRIC_III("Short Metric III", 10, 3),
    SHORT_METRIC_IV("Short Metric IV", 10, 4),
    SHORT_METRIC_V("Short Metric V", 10, 5),
    WA_STANDARD_BOW("WA Standard Bow", 11),
    WA_900("WA 900", 12),
    WA_70M("WA 70m", 13),
    WA_60M("WA 60m", 14),
    WA_50M_BAREBOW_METRIC_122_50("WA 50m (Barebow) / Metric 122-50", listOf(DbRoundRef(15), DbRoundRef(16, 1))),
    METRIC_122_40("Metric 122-40", 16, 2),
    METRIC_122_30("Metric 122-30", 16, 3),
    WA_50M_COMPOUND("WA 50m (Compound)", 15),
    METRIC_80_40("Metric 80-40", 17, 1),
    METRIC_80_30("Metric 80-30", 17, 2),
    ;

    constructor(rawName: String, defaultRoundId: Int, defaultRoundSubtypeId: Int? = null) :
            this(rawName, listOf(DbRoundRef(defaultRoundId, defaultRoundSubtypeId)))

    data class DbRoundRef(
            val defaultRoundId: Int,
            val defaultRoundSubtypeId: Int? = null,
    ) {
        override fun equals(other: Any?): Boolean {
            if (other !is DbRoundRef) return false

            if (defaultRoundId != other.defaultRoundId) return false

            if (defaultRoundSubtypeId == other.defaultRoundSubtypeId) return true
            if (defaultRoundSubtypeId == null && other.defaultRoundSubtypeId == 1) return true
            if (defaultRoundSubtypeId == 1 && other.defaultRoundSubtypeId == null) return true
            return false
        }

        override fun hashCode(): Int {
            var result = defaultRoundId
            result = 31 * result + (defaultRoundSubtypeId ?: 1)
            return result
        }
    }

    companion object {
        val backwardsMap = values().associateBy { it.rawName }
    }
}
