package eywa.projectcodex.database.archer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
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

    @Query("SELECT * FROM ${DatabaseArcher.TABLE_NAME} WHERE archerId = $DEFAULT_ARCHER_ID")
    fun getDefaultArcher(): Flow<DatabaseArcher?>

    @Transaction
    @Query("SELECT * FROM ${DatabaseArcher.TABLE_NAME} WHERE archerId = $DEFAULT_ARCHER_ID")
    fun getDefaultFullArcher(): Flow<DatabaseFullArcher?>

    @Transaction
    @Query("SELECT * FROM ${DatabaseArcher.TABLE_NAME}")
    fun getAllArchers(): Flow<List<DatabaseFullArcher>>

    @Query("UPDATE ${DatabaseArcher.TABLE_NAME} SET isGent = :isGent WHERE archerId = $DEFAULT_ARCHER_ID")
    suspend fun updateDefaultArcher(isGent: Boolean)

    @Query("UPDATE ${DatabaseArcher.TABLE_NAME} SET age = :age WHERE archerId = $DEFAULT_ARCHER_ID")
    suspend fun updateDefaultArcher(age: ClassificationAge)

    @Query("UPDATE ${DatabaseArcher.TABLE_NAME} SET bow = :bow WHERE archerId = $DEFAULT_ARCHER_ID")
    suspend fun updateDefaultArcher(bow: ClassificationBow)
}
