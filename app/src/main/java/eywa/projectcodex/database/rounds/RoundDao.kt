package eywa.projectcodex.database.rounds

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import eywa.projectcodex.database.rounds.Round.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao : RoundTypeDao<Round> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(insertItem: Round)

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllRounds(): Flow<List<Round>>

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

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME WHERE defaultRoundId = :defaultRoundId")
    fun getFullRoundInfo(defaultRoundId: Int): Flow<FullRoundInfo?>

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
