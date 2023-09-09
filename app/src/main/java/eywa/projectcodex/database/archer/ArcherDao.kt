package eywa.projectcodex.database.archer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcherDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archer: DatabaseArcher)

    @Query(
            """
                INSERT OR IGNORE 
                INTO ${DatabaseArcher.TABLE_NAME} (archerId, name) 
                VALUES ($DEFAULT_ARCHER_ID, "Default")
            """
    )
    suspend fun insertDefaultArcherIfNotExist()

    @Transaction
    @Query("SELECT * FROM ${DatabaseArcher.TABLE_NAME} WHERE archerId = $DEFAULT_ARCHER_ID")
    fun getDefaultArcher(): Flow<DatabaseFullArcher?>

    @Transaction
    @Query("SELECT * FROM ${DatabaseArcher.TABLE_NAME}")
    fun getAllArchers(): Flow<List<DatabaseFullArcher>>
}
