package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.database.arrowValue.ArrowValue

object ArcherRoundsPreviewHelper {
    val round = RoundPreviewHelper.outdoorImperialRoundData

    val inputArrows = listOf(
            Arrow(10, true),
            Arrow(10, false),
            Arrow(3, false),
            Arrow(0, false),
    )

    val SIMPLE = ArcherRoundState.Loaded(
            currentScreen = ArcherRoundScreen.INPUT_END,
            fullArcherRoundInfo = ArcherRoundPreviewHelper.fullArcherRoundInfo,
            goldsType = GoldsType.NINES,
            inputArrows = inputArrows,
    )

    val FEW_ARROWS = SIMPLE.copy(
            fullArcherRoundInfo = SIMPLE.fullArcherRoundInfo.copy(
                    arrows = List(6) { ArrowValue(1, it, 7, false) },
            )
    )
}