package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch.Companion.TABLE_NAME

@Dao
interface HeadToHeadMatchDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shootDetail: DatabaseHeadToHeadMatch)

    @Update
    suspend fun update(vararg matches: DatabaseHeadToHeadMatch)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId AND matchNumber = :matchNumber")
    suspend fun delete(shootId: Int, matchNumber: Int)
}
