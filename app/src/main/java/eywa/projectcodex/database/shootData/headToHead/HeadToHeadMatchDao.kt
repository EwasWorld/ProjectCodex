package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface HeadToHeadMatchDao {
    @Query(
            """
                SELECT *
                FROM $TABLE_NAME
                WHERE shootId = :shootId 
                    AND matchNumber >= :matchNumbersAboveAndIncluding 
            """
    )
    fun getMatchNumberGreaterThanOrEqualTo(
            shootId: Int,
            matchNumbersAboveAndIncluding: Int,
    ): Flow<List<DatabaseHeadToHeadMatch>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg shootDetail: DatabaseHeadToHeadMatch)

    @Update
    suspend fun update(vararg matches: DatabaseHeadToHeadMatch)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId AND matchNumber = :matchNumber")
    suspend fun delete(shootId: Int, matchNumber: Int)

    @Delete
    suspend fun delete(vararg match: DatabaseHeadToHeadMatch)
}
