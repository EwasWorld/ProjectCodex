package eywa.projectcodex.database.rounds

import androidx.room.*
import eywa.projectcodex.database.rounds.Round.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao : RoundTypeDao<Round> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(insertItem: Round)

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllRoundsFullInfo(): Flow<List<FullRoundInfo>>

    @Transaction
    @Query(
            """
                SELECT *
                FROM $TABLE_NAME
                WHERE 
                    (:allIndoorOutdoor OR isOutdoor = :isOutdoor)
                    AND (:allMetricImperial OR isMetric = :isMetric)
            """
    )
    fun getAllRoundsFullInfo(
            allIndoorOutdoor: Boolean,
            isOutdoor: Boolean,
            allMetricImperial: Boolean,
            isMetric: Boolean,
    ): Flow<List<FullRoundInfo>>

    @Update
    override fun updateSingle(updateItem: Round)

    @Update
    fun update(vararg rounds: Round)

    @Query("DELETE FROM $TABLE_NAME WHERE roundId = :roundId")
    suspend fun delete(roundId: Int)

    @Delete
    override suspend fun deleteSingle(deleteItem: Round)

    @Query("DELETE FROM $TABLE_NAME")
    suspend fun deleteAll()
}
