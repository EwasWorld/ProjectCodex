package eywa.projectcodex.database.shootData

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.shootData.DatabaseShootRound.Companion.TABLE_NAME

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
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ],
)
data class DatabaseShootRound(
        @PrimaryKey val shootId: Int,
        @ColumnInfo(index = true) val roundId: Int,
        @ColumnInfo(index = true) val roundSubTypeId: Int? = null,
        val faces: List<RoundFace>? = null,
        val sightersCount: Int = 0,
) {
    companion object {
        const val TABLE_NAME = "shoot_rounds"
    }
}
