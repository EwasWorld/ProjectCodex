package eywa.projectcodex.database.entities

import androidx.room.Entity

@Entity(tableName = "arrow_value_table", primaryKeys = ["archerRoundsID", "arrowNumber"])
data class ArrowValue(
        //@PrimaryKey(autoGenerate = true)
        val archerRoundsID: Int,
        val arrowNumber: Int,
        val score: Int,
        val isX: Boolean
)