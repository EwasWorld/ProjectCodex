package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Embedded
import androidx.room.Relation

data class DatabaseFullHeadToHead(
        @Embedded
        val headToHead: DatabaseHeadToHead,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val heats: List<DatabaseHeadToHeadHeat>? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val details: List<DatabaseHeadToHeadDetail>? = null,
)
