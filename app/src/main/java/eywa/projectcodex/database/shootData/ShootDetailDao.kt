package eywa.projectcodex.database.shootData

import androidx.room.*
import eywa.projectcodex.database.shootData.DatabaseShootDetail.Companion.TABLE_NAME

@Dao
interface ShootDetailDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shootDetail: DatabaseShootDetail)

    @Update
    suspend fun update(vararg shootDetails: DatabaseShootDetail)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)
}
