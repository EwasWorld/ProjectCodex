package eywa.projectcodex.database.bow

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.bow.DatabaseBow.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface BowDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(bow: DatabaseBow)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(bow: DatabaseBow)

    @Query("SELECT * FROM $TABLE_NAME WHERE id = $DEFAULT_BOW_ID")
    fun getDefaultBow(): Flow<DatabaseBow?>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllBows(): Flow<List<DatabaseBow>>

    @Query("UPDATE $TABLE_NAME SET isSightMarkDiagramHighestAtTop = :isHighestAtTop WHERE id = :id")
    suspend fun setHighestAtTop(id: Int, isHighestAtTop: Boolean)

    @Query("UPDATE $TABLE_NAME SET type = :bowType WHERE id = $DEFAULT_BOW_ID")
    suspend fun updateDefaultBow(bowType: ClassificationBow)
}
