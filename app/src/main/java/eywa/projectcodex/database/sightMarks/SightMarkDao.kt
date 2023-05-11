package eywa.projectcodex.database.sightMarks

import androidx.room.*
import eywa.projectcodex.database.sightMarks.DatabaseSightMark.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface SightMarkDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sightMark: DatabaseSightMark)

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllSightMarks(): Flow<List<DatabaseSightMark>>

    @Query("SELECT * FROM $TABLE_NAME WHERE id = :id")
    fun getSightMark(id: Int): Flow<DatabaseSightMark>

    @Query("UPDATE $TABLE_NAME SET isArchived = 1")
    suspend fun archiveAll()

    @Query("DELETE FROM $TABLE_NAME WHERE id = :id")
    suspend fun deleteRound(id: Int)

    @Update
    suspend fun update(vararg sightMark: DatabaseSightMark)
}
