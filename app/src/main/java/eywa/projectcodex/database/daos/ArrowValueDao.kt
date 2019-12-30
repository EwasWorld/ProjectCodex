package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eywa.projectcodex.database.entities.ArrowValue

@Dao
interface ArrowValueDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(arrowValue: ArrowValue)

    @Query("SELECT * from arrow_values")
    fun getAllArrowValues(): LiveData<List<ArrowValue>>

    @Query("DELETE FROM arrow_values")
    suspend fun deleteAll()
}