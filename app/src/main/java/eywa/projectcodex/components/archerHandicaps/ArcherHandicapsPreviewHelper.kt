package eywa.projectcodex.components.archerHandicaps

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.DEFAULT_ARCHER_ID
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.archer.HandicapType
import java.util.*

object ArcherHandicapsPreviewHelper {
    val handicaps = listOf(
            HandicapType.OUTDOOR_TOURNAMENT to 30,
            HandicapType.OUTDOOR to 25,
            HandicapType.INDOOR to 20,
            HandicapType.OUTDOOR_TOURNAMENT to 27,
    ).mapIndexed { index, (type, handicap) ->
        DatabaseArcherHandicap(
                archerHandicapId = index + 1,
                archerId = DEFAULT_ARCHER_ID,
                bowStyle = ClassificationBow.RECURVE,
                handicapType = type,
                handicap = handicap,
                dateSet = Calendar.getInstance().apply { add(Calendar.DATE, -index - 1) },
        )
    }
}
