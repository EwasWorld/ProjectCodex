package eywa.projectcodex.database.archer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archers")
data class Archer(
        @PrimaryKey(autoGenerate = true)
        val archerId: Int,
        var name: String
)