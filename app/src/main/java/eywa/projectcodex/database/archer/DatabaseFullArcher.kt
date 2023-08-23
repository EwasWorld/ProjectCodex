package eywa.projectcodex.database.archer

import androidx.room.Embedded
import androidx.room.Relation

data class DatabaseFullArcher(
        @Embedded val archer: DatabaseArcher,

        @Relation(parentColumn = "archerId", entityColumn = "archerId")
        val archerHandicaps: List<DatabaseArcherHandicap>? = null
)
