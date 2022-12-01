package eywa.projectcodex.database.archerRound

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.archerRound.ArcherRound.Companion.TABLE_NAME
import eywa.projectcodex.database.rounds.Round
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcherRoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archerRound: ArcherRound): Long

    @Update
    suspend fun update(vararg archerRounds: ArcherRound)

    @Query("SELECT MAX(archerRoundId) FROM $TABLE_NAME")
    fun getMaxId(): LiveData<Int>

    @Query("SELECT * FROM $TABLE_NAME WHERE archerRoundId = :id")
    fun getArcherRoundById(id: Int): LiveData<ArcherRound>

    @Query(
            """
                SELECT rounds.*
                FROM $TABLE_NAME INNER JOIN ${Round.TABLE_NAME} ON archer_rounds.roundId = rounds.roundId
                WHERE archerRoundId = :archerRoundId
            """
    )
    fun getRoundInfo(archerRoundId: Int): LiveData<Round>

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllArcherRoundsWithRoundInfoAndName(): LiveData<List<ArcherRoundWithRoundInfoAndName>>

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME WHERE archerRoundId == :archerRoundId")
    fun getArcherRoundWithRoundInfoAndName(archerRoundId: Int): LiveData<ArcherRoundWithRoundInfoAndName>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllArcherRounds(): LiveData<List<ArcherRound>>

    @Query("DELETE FROM $TABLE_NAME WHERE archerRoundId = :archerRoundId")
    suspend fun deleteRound(archerRoundId: Int)

    @Query("SELECT * FROM $TABLE_NAME WHERE archerRoundId == :archerRoundId")
    fun getFullArcherRoundInfo(archerRoundId: Int): Flow<DatabaseFullArcherRoundInfo>

    // TODO Remove custom type example when from-to filter has been implemented
//    @Entity
//    data class User(private val birthday: Date?)

//    @Query("SELECT * FROM user WHERE birthday BETWEEN :from AND :to")
//    fun findUsersBornBetweenDates(from: Date, to: Date): List<User>
}