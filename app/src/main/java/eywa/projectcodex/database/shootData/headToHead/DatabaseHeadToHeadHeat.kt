package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Entity
import androidx.room.ForeignKey
import eywa.projectcodex.database.shootData.DatabaseShoot

@Entity(
        tableName = DatabaseHeadToHeadHeat.TABLE_NAME,
        primaryKeys = ["shootId", "heat"],
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ],
)
data class DatabaseHeadToHeadHeat(
        val shootId: Int,
        val heat: Int,
        val opponent: String?,
        val opponentQualificationRank: Int?,
        val isShootOffWin: Boolean,
        val sightersCount: Int,
        val isBye: Boolean,
) {
    companion object {
        const val TABLE_NAME = "head_to_head_heat"
    }
}
