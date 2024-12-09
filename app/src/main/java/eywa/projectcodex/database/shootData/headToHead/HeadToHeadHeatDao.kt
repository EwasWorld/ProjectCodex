package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat.Companion.TABLE_NAME

@Dao
interface HeadToHeadHeatDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shootDetail: DatabaseHeadToHeadHeat)

    @Update
    suspend fun update(vararg heats: DatabaseHeadToHeadHeat)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId AND matchNumber = :matchNumber")
    suspend fun delete(shootId: Int, matchNumber: Int)
}
