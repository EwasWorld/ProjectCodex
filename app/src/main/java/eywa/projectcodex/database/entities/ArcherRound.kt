package eywa.projectcodex.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "archer_rounds")
data class ArcherRound(
        @PrimaryKey(autoGenerate = true)
        val archerRoundId: Int,
        var dateShot: Date,
        val archerId: Int,
        val bowId: Int?,
        val roundReferenceId: Int?,
        val roundDistanceId: Int?,
        val goalScore: Int?,
        var shootStatus: String?,
        val countsTowardsHandicap: Boolean
)