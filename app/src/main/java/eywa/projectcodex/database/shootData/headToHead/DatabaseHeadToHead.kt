package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
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

    fun getExpectedOpponentRank(matchNumber: Int): Opponent? {
        if (qualificationRank == null || totalArchers == null) return null
        return HeadToHeadUseCase.getOpponents(rank = qualificationRank, totalArchers = totalArchers)
                .reversed()
                .getOrNull(matchNumber - 1)
                ?.let { Opponent.Rank(it) }
                ?: Opponent.Bye
    }

    val description: ResOrActual<String>
        get() {
            val team =
                    if (teamSize > 1) ResOrActual.StringResource(R.string.head_to_head__info_teams, listOf(teamSize))
                    else ResOrActual.StringResource(R.string.head_to_head__info_individual)
            val style = ResOrActual.StringResource(
                    if (isSetPoints) R.string.create_round__h2h_style_recurve
                    else R.string.create_round__h2h_style_compound,
            )
            val rank =
                    if (qualificationRank == null) {
                        ResOrActual.Blank
                    }
                    else if (totalArchers != null) {
                        ResOrActual.StringResource(
                                R.string.head_to_head__info_quali_rank_total_archers,
                                listOf(qualificationRank, totalArchers),
                        )
                    }
                    else {
                        ResOrActual.Blank
                    }
            val format =
                    if (isStandardFormat) ResOrActual.Blank
                    else ResOrActual.StringResource(R.string.head_to_head__info_non_standard)
            val totalArchers =
                    if (totalArchers == null || qualificationRank != null) ResOrActual.Blank
                    else ResOrActual.StringResource(R.string.head_to_head__info_total_archers, listOf(totalArchers))

            return ResOrActual.JoinToStringResource(
                    listOf(
                            team,
                            ResOrActual.StringResource(R.string.head_to_head__info_separator),
                            style,
                            rank,
                            totalArchers,
                            format,
                    ),
                    ResOrActual.Blank,
            )
        }

    companion object {
        const val TABLE_NAME = "head_to_head"
    }
}

sealed class Opponent {
    data object Bye : Opponent()
    data class Rank(val rank: Int) : Opponent()
}
