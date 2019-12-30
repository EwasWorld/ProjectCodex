package eywa.projectcodex.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import eywa.projectcodex.database.entities.RoundReference

@Dao
interface RoundReferenceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(roundReference: RoundReference)
}