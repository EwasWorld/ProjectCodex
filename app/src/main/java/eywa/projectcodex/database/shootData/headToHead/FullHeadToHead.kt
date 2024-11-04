package eywa.projectcodex.database.shootData.headToHead

data class FullHeadToHead(
        val headToHead: DatabaseHeadToHead,
        val heats: List<DatabaseHeadToHeadHeat>,
) {
    val hasStarted: Boolean
        get() = heats.isNotEmpty()

    val arrowsShot: Int
        get() = 0

    val isComplete: Boolean
        get() = false
}
