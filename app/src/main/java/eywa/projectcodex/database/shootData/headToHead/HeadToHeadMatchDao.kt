package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Dao
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
                SELECT COUNT(rowId)
                FROM $TABLE_NAME
                WHERE shootId = :shootId AND matchNumber = :matchNumber 
            """
    )
    fun getMatchCount(shootId: Int, matchNumber: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shootDetail: DatabaseHeadToHeadMatch)

    @Update
    suspend fun update(vararg matches: DatabaseHeadToHeadMatch)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId AND matchNumber = :matchNumber")
    suspend fun delete(shootId: Int, matchNumber: Int)

    @Query(
            """
                UPDATE $TABLE_NAME
                SET matchNumber = matchNumber + :increment
                WHERE shootId = :shootId AND matchNumber >= :matchNumbersAboveAndIncluding
            """
    )
    suspend fun incrementMatchNumber(shootId: Int, matchNumbersAboveAndIncluding: Int, increment: Int)
}
