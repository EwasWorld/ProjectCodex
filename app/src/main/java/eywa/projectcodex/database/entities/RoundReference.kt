package eywa.projectcodex.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds_references")
data class RoundReference(
        @PrimaryKey(autoGenerate = true)
        val roundReferenceId: Int,
        val type: String,
        val length: String?,
        val scoringType: String,
        val outdoor: Boolean,
        val innerTenScoring: Boolean
)