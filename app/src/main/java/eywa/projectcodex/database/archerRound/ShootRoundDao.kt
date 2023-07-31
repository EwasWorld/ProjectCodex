package eywa.projectcodex.database.archerRound

import androidx.room.*
import eywa.projectcodex.database.archerRound.DatabaseShootRound.Companion.TABLE_NAME

@Dao
interface ShootRoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shootRound: DatabaseShootRound)

    @Update
    suspend fun update(vararg shootRounds: DatabaseShootRound)

    @Query("DELETE FROM $TABLE_NAME WHERE archerRoundId = :archerRoundId")
    suspend fun delete(archerRoundId: Int)
}
