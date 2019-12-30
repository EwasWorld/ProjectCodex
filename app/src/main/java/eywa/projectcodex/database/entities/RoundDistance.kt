package eywa.projectcodex.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "round_distances")
data class RoundDistance(
        @PrimaryKey(autoGenerate = true)
        val roundDistanceId: Int,
        val roundReferenceId: Int?,
        val distanceInM: Double?,
        val faceSizeInCm: Int?,
        val arrowCount: Int?
)