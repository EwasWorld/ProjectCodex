package eywa.projectcodex.database.bow

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eywa.projectcodex.database.bow.DatabaseBow.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface BowDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(bow: DatabaseBow)

    @Query(
            """
                INSERT OR IGNORE 
                INTO $TABLE_NAME (id, name, description, type, isSightMarkDiagramHighestAtTop) 
                VALUES ($DEFAULT_BOW_ID, "Default", NULL, "RECURVE", 0)
            """
    )
    suspend fun insertDefaultBowIfNotExist()

    @Query("SELECT * FROM $TABLE_NAME WHERE id = $DEFAULT_BOW_ID")
    fun getDefaultBow(): Flow<DatabaseBow>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllBows(): Flow<List<DatabaseBow>>

    @Query("UPDATE $TABLE_NAME SET isSightMarkDiagramHighestAtTop = :isHighestAtTop WHERE id = :id")
    suspend fun setHighestAtTop(id: Int, isHighestAtTop: Boolean)
}
