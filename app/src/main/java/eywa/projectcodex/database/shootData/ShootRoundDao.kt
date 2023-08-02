package eywa.projectcodex.database.shootData

import androidx.room.*
import eywa.projectcodex.database.shootData.DatabaseShootRound.Companion.TABLE_NAME

@Dao
interface ShootRoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shootRound: DatabaseShootRound)

    @Update
    suspend fun update(vararg shootRounds: DatabaseShootRound)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)
}
