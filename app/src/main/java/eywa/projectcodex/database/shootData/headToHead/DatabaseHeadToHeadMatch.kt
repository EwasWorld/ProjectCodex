package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Entity
import androidx.room.ForeignKey
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.shootData.DatabaseShoot

@Entity(
        tableName = DatabaseHeadToHeadMatch.TABLE_NAME,
        primaryKeys = ["shootId", "matchNumber"],
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ],
)
data class DatabaseHeadToHeadMatch(
        val shootId: Int,
        /**
         * First is match 1
         */
        val matchNumber: Int,
        val heat: Int?,
        /**
         * The final rank if the archer wins this and all later matches
         */
        val maxPossibleRank: Int?,
        val opponent: String?,
        val opponentQualificationRank: Int?,
        val sightersCount: Int,
        val isBye: Boolean,
) {
    fun opponentString(multiline: Boolean = false) =
            when {
                opponentQualificationRank != null && !opponent.isNullOrBlank() -> {
                    ResOrActual.StringResource(
                            if (multiline) R.string.head_to_head_add_end__opponent_rank_and_name_multiline
                            else R.string.head_to_head_add_end__opponent_rank_and_name,
                            listOf(opponentQualificationRank, opponent),
                    )
                }

                opponentQualificationRank != null -> {
                    ResOrActual.StringResource(
                            if (multiline) R.string.head_to_head_add_end__opponent_rank_multiline
                            else R.string.head_to_head_add_end__opponent_rank,
                            listOf(opponentQualificationRank),
                    )
                }

                !opponent.isNullOrBlank() -> {
                    ResOrActual.StringResource(
                            if (multiline) R.string.head_to_head_add_end__opponent_name_multiline
                            else R.string.head_to_head_add_end__opponent_name,
                            listOf(opponent),
                    )
                }

                else -> null
            }

    companion object {
        const val TABLE_NAME = "head_to_head_match"
    }
}
