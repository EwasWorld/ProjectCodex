package eywa.projectcodex.database.archer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(archer: DatabaseArcher)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archer: DatabaseArcher)

    @Query("SELECT * FROM ${DatabaseArcher.TABLE_NAME} WHERE archerId = $DEFAULT_ARCHER_ID")
    fun getDefaultArcher(): Flow<DatabaseArcher?>

    @Query("UPDATE ${DatabaseArcher.TABLE_NAME} SET isGent = :isGent WHERE archerId = $DEFAULT_ARCHER_ID")
    suspend fun updateDefaultArcher(isGent: Boolean)

    @Query("UPDATE ${DatabaseArcher.TABLE_NAME} SET age = :age WHERE archerId = $DEFAULT_ARCHER_ID")
    suspend fun updateDefaultArcher(age: ClassificationAge)
}
