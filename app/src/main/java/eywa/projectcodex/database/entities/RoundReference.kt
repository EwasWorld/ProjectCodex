package eywa.projectcodex.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds_references")
data class RoundReference(
        @PrimaryKey(autoGenerate = true)
        val roundReferenceId: Int,
        val outdoor: Boolean,
        val scoringType: String,
        val innerTenScoring: Boolean,
        val type: String, // Warwick, National, etc.
        val length: String? = null // Standard, Short, Long, etc.
)