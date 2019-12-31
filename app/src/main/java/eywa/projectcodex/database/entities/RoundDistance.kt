package eywa.projectcodex.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "round_distances")
data class RoundDistance(
        @PrimaryKey(autoGenerate = true)
        val roundDistanceId: Int,
        val roundReferenceId: Int? = null,
        val distanceInM: Double? = null,
        val faceSizeInCm: Int? = null,
        val arrowCount: Int? = null
)