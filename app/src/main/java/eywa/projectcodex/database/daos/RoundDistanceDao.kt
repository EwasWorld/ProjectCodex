package eywa.projectcodex.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import eywa.projectcodex.database.entities.RoundDistance

@Dao
interface RoundDistanceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(roundDistance: RoundDistance)
}