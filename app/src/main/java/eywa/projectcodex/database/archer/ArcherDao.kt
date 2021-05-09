package eywa.projectcodex.database.archer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface ArcherDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archer: Archer)
}