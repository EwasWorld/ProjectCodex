package eywa.projectcodex.database.shootData

import java.util.Calendar

data class DatabaseShootShortRecord(
        val shootId: Int,
        val dateShot: Calendar,
        val score: Int,
        val isComplete: Boolean,
)
