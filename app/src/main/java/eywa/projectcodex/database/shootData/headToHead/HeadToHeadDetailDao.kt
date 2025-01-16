package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail.Companion.TABLE_NAME

@Dao
interface HeadToHeadDetailDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg shootDetail: DatabaseHeadToHeadDetail)

    @Update
    suspend fun update(vararg heats: DatabaseHeadToHeadDetail)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId AND matchNumber = :matchNumber")
    suspend fun delete(shootId: Int, matchNumber: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId AND matchNumber = :matchNumber AND setNumber = :setNumber")
    suspend fun delete(shootId: Int, matchNumber: Int, setNumber: Int)

    @Query(
            """
                UPDATE $TABLE_NAME
                SET matchNumber = :newMatchNumber
                WHERE shootId = :shootId AND matchNumber = :oldMatchNumber
            """
    )
    suspend fun updateHeat(shootId: Int, newMatchNumber: Int, oldMatchNumber: Int)
}
