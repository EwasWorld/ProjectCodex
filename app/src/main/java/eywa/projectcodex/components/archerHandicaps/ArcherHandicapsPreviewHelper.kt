package eywa.projectcodex.components.archerHandicaps

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
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
                archerId = -1,
                bowStyle = ClassificationBow.RECURVE,
                handicapType = type,
                handicap = handicap,
                dateSet = Calendar.getInstance().apply { add(Calendar.DATE, -index - 1) },
        )
    }

    @Composable
    fun Display(initialState: ArcherHandicapsState) {
        var state by remember { mutableStateOf(initialState) }
        val context = LocalContext.current

        CodexTheme {
            ArcherHandicapsScreen(state) { action ->
                when (action) {
                    is ArcherHandicapsIntent.RowClicked ->
                        state = state.copy(
                                menuShownForId = action.item.archerHandicapId.takeIf { state.menuShownForId != it }
                        )
                    else -> ToastSpamPrevention.displayToast(context, action::class.simpleName.toString())
                }
            }
        }
    }
}
