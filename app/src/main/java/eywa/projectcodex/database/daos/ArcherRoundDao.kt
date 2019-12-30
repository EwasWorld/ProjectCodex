package eywa.projectcodex.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import eywa.projectcodex.database.entities.ArcherRound

@Dao
interface ArcherRoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archerRound: ArcherRound)

    // TODO Remove custom type example when from-to filter has been implemented
//    @Entity
//    data class User(private val birthday: Date?)

//    @Query("SELECT * FROM user WHERE birthday BETWEEN :from AND :to")
//    fun findUsersBornBetweenDates(from: Date, to: Date): List<User>
}