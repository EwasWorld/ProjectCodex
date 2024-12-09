package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.database.shootData.DatabaseShoot

@Entity(
        tableName = DatabaseHeadToHead.TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class DatabaseHeadToHead(
        @PrimaryKey val shootId: Int,
        val isRecurveStyle: Boolean,
        val teamSize: Int,
        val qualificationRank: Int?,
        val isStandardFormat: Boolean,
        val totalArchers: Int?,
) {
    fun getOpponentRank(matchNumber: Int): Opponent? {
        if (qualificationRank == null || totalArchers == null) return null
        return HeadToHeadUseCase.getOpponents(rank = qualificationRank, totalArchers = totalArchers)
                .reversed()
                .getOrNull(matchNumber - 1)
                ?.let { Opponent.Rank(it) }
                ?: Opponent.Bye
    }

    companion object {
        const val TABLE_NAME = "head_to_head"
    }
}

sealed class Opponent {
    data object Bye : Opponent()
    data class Rank(val rank: Int) : Opponent()
}
