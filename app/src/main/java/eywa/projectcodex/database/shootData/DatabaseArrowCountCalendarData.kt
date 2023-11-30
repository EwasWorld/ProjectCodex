package eywa.projectcodex.database.shootData

data class DatabaseArrowCountCalendarData(
        val dateString: String,
        val count: Int,
) {
    val day
        get() = dateString.split("-")[0].toInt()

    val month
        get() = dateString.split("-")[1].toInt()
}
