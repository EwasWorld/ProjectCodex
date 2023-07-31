package eywa.projectcodex.database.archerRound

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.archerRound.DatabaseShootRound.Companion.TABLE_NAME
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType

@Entity(
        tableName = TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = Round::class,
                    parentColumns = ["roundId"],
                    childColumns = ["roundId"],
                    onDelete = ForeignKey.SET_NULL,
            ),
            ForeignKey(
                    entity = RoundSubType::class,
                    parentColumns = ["roundId", "subTypeId"],
                    childColumns = ["roundId", "roundSubTypeId"],
                    onDelete = ForeignKey.SET_NULL,
            ),
            ForeignKey(
                    entity = ArcherRound::class,
                    parentColumns = ["archerRoundId"],
                    childColumns = ["archerRoundId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ],
)
data class DatabaseShootRound(
        @PrimaryKey val archerRoundId: Int,
        val roundId: Int,
        val roundSubTypeId: Int? = null,
        val faces: List<RoundFace>? = null,
        val sightersCount: Int = 0,
) {
    companion object {
        const val TABLE_NAME = "shoot_round"
    }
}
