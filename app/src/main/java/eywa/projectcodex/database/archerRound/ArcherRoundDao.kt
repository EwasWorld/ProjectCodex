package eywa.projectcodex.database.archerRound

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eywa.projectcodex.database.rounds.Round

@Dao
interface ArcherRoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archerRound: ArcherRound)

    @Query("SELECT MAX(archerRoundId) FROM $ARCHER_ROUNDS_TABLE_NAME")
    fun getMaxId(): LiveData<Int>

    @Query("SELECT * FROM $ARCHER_ROUNDS_TABLE_NAME WHERE archerRoundId = :id")
    fun getArcherRoundById(id: Int): LiveData<ArcherRound>

    @Query(
            """
                SELECT rounds.*
                FROM $ARCHER_ROUNDS_TABLE_NAME INNER JOIN rounds ON archer_rounds.roundId = rounds.roundId
                WHERE archerRoundId = :archerRoundId
            """
    )
    fun getRoundInfo(archerRoundId: Int): LiveData<Round>

    @Query(
            """
                SELECT 
                    archer_rounds.archerRoundId AS ar_archerRoundId,
                    archer_rounds.dateShot AS ar_dateShot,
                    archer_rounds.archerId AS ar_archerId,
                    archer_rounds.countsTowardsHandicap AS ar_countsTowardsHandicap,
                    archer_rounds.bowId AS ar_bowId,
                    archer_rounds.roundId AS ar_roundId,
                    archer_rounds.roundSubTypeId AS ar_roundSubTypeId,
                    archer_rounds.goalScore AS ar_goalScore,
                    archer_rounds.shootStatus AS ar_shootStatus, 
                    rounds.*, 
                    round_sub_types.name AS roundSubTypeName
                FROM $ARCHER_ROUNDS_TABLE_NAME 
                    LEFT JOIN rounds ON archer_rounds.roundId = rounds.roundId
                    LEFT JOIN round_sub_types ON archer_rounds.roundSubTypeId = round_sub_types.subTypeId
                                              AND archer_rounds.roundId = round_sub_types.roundId
            """
    )
    fun getAllArcherRoundsWithRoundInfoAndName(): LiveData<List<ArcherRoundWithRoundInfoAndName>>

    @Query(
            """
                SELECT 
                    archer_rounds.archerRoundId AS ar_archerRoundId,
                    archer_rounds.dateShot AS ar_dateShot,
                    archer_rounds.archerId AS ar_archerId,
                    archer_rounds.countsTowardsHandicap AS ar_countsTowardsHandicap,
                    archer_rounds.bowId AS ar_bowId,
                    archer_rounds.roundId AS ar_roundId,
                    archer_rounds.roundSubTypeId AS ar_roundSubTypeId,
                    archer_rounds.goalScore AS ar_goalScore,
                    archer_rounds.shootStatus AS ar_shootStatus, 
                    rounds.*, 
                    round_sub_types.name AS roundSubTypeName
                FROM $ARCHER_ROUNDS_TABLE_NAME 
                    LEFT JOIN rounds ON archer_rounds.roundId = rounds.roundId
                    LEFT JOIN round_sub_types ON archer_rounds.roundSubTypeId = round_sub_types.subTypeId
                                              AND archer_rounds.roundId = round_sub_types.roundId
                WHERE ar_archerRoundId == :archerRoundId
            """
    )
    fun getArcherRoundWithRoundInfoAndName(archerRoundId: Int): LiveData<ArcherRoundWithRoundInfoAndName>

    @Query("SELECT * FROM $ARCHER_ROUNDS_TABLE_NAME")
    fun getAllArcherRounds(): LiveData<List<ArcherRound>>

    @Query("DELETE FROM $ARCHER_ROUNDS_TABLE_NAME WHERE archerRoundId = :archerRoundId")
    suspend fun deleteRound(archerRoundId: Int)

    // TODO Remove custom type example when from-to filter has been implemented
//    @Entity
//    data class User(private val birthday: Date?)

//    @Query("SELECT * FROM user WHERE birthday BETWEEN :from AND :to")
//    fun findUsersBornBetweenDates(from: Date, to: Date): List<User>
}