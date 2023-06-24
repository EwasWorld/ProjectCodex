package eywa.projectcodex.database

enum class RoundFace {
    /**
     * Standard single face consisting of all scoring rings appropriate for the round
     */
    FULL,

    /**
     * Three faces (usually aligned vertically) consisting of scoring rings 10 through 6 inclusive.
     * One arrow must be put in each face. 10-zone scoring only
     */
    TRIPLE,

    /**
     * Scoring rings 10 through 5 inclusive. 10-zone scoring only
     */
    FITA_FIVE,

    /**
     * Scoring rings 10 through 6 inclusive. 10-zone scoring only
     */
    FITA_SIX,

    /**
     * Five faces (in a dice pattern) consisting scoring rings 4 and 5 rings of a Worcester only.
     * One arrow must be put in each face
     */
    WORCESTER_FIVE,
    ;

    fun toDbData() = ordinal

    companion object {
        fun fromDbData(value: Int) = values()[value]
    }
}
