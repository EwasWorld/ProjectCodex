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
        val countsTowardsHandicap: Boolean,
        val bowId: Int? = null,
        val roundReferenceId: Int? = null,
        val roundDistanceId: Int? = null,
        val goalScore: Int? = null,
        var shootStatus: String? = null
)