package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface HeadToHeadDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE shootId = :shootId")
    fun getFullHeadToHead(shootId: Int): Flow<DatabaseFullHeadToHead?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shootDetail: DatabaseHeadToHead)

    @Update
    suspend fun update(vararg headToHeads: DatabaseHeadToHead)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)
}
