package eywa.projectcodex.common.utils.classificationTables.model

enum class ClassificationBow(val rawName: String) {
    RECURVE("Recurve"),
    COMPOUND("Compound"),
    BAREBOW("Barebow"),
    LONGBOW("Longbow"),
    ;

    companion object {
        val backwardsMap = entries.associateBy { it.rawName }
    }
}
