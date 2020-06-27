package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.Round

@Dao
interface ArcherRoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archerRound: ArcherRound)

    @Query("SELECT MAX(archerRoundId) from archer_rounds")
    fun getMaxId(): LiveData<Int>

    @Query(
            """
                SELECT rounds.*
                FROM archer_rounds INNER JOIN rounds ON archer_rounds.roundId = rounds.roundId
                WHERE archerRoundId = :archerRoundId
            """
    )
    fun getRoundInfo(archerRoundId: Int): LiveData<Round>

    @Query("SELECT * from archer_rounds")
    fun getAllArcherRounds(): LiveData<List<ArcherRound>>

    @Query("DELETE FROM archer_rounds WHERE archerRoundId = :archerRoundId")
    suspend fun deleteRound(archerRoundId: Int)

    // TODO Remove custom type example when from-to filter has been implemented
//    @Entity
//    data class User(private val birthday: Date?)

//    @Query("SELECT * FROM user WHERE birthday BETWEEN :from AND :to")
//    fun findUsersBornBetweenDates(from: Date, to: Date): List<User>
}