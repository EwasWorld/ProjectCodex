package eywa.projectcodex.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import eywa.projectcodex.database.entities.RoundSubTypeCount

@Dao
interface RoundSubTypeCountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(roundSubTypeCount: RoundSubTypeCount)
}