package eywa.projectcodex.common.utils.classificationTables.model

enum class ClassificationAge(val rawName: String) {
    OVER_50("50+"),
    SENIOR("Senior"),
    U21("U21"),
    U18("U18"),
    U16("U16"),
    U15("U15"),
    U14("U14"),
    U12("U12"),
    ;

    companion object {
        val backwardsMap = values().associateBy { it.rawName }
    }
}
