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
        /**
         * Set points (usually recurve matches) vs total score (usually compound matches)
         */
        val isSetPoints: Boolean,
        val teamSize: Int,
        val qualificationRank: Int?,
        /**
         * If null, the matches are standard format, else free format
         */
        val endSize: Int?,
        val totalArchers: Int?,
) {
    val isStandardFormat: Boolean
        get() = endSize == null

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
