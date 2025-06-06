package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface HeadToHeadDetailDao {
    @Query(
            """
                SELECT *
                FROM $TABLE_NAME
                WHERE shootId = :shootId 
                    AND matchNumber = :matchNumber 
                    AND setNumber >= :setNumbersAboveAndIncluding
            """
    )
    fun getSetNumberGreaterThanOrEqualTo(
            shootId: Int,
            matchNumber: Int,
            setNumbersAboveAndIncluding: Int,
    ): Flow<List<DatabaseHeadToHeadDetail>>

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
    ): Flow<List<DatabaseHeadToHeadDetail>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg shootDetail: DatabaseHeadToHeadDetail)

    @Update
    suspend fun update(vararg details: DatabaseHeadToHeadDetail)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun deleteAll(shootId: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId AND matchNumber = :matchNumber")
    suspend fun delete(shootId: Int, matchNumber: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId AND matchNumber = :matchNumber AND setNumber = :setNumber")
    suspend fun delete(shootId: Int, matchNumber: Int, setNumber: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE headToHeadArrowScoreId = :headToHeadArrowScoreId")
    suspend fun delete(headToHeadArrowScoreId: Int)
}
