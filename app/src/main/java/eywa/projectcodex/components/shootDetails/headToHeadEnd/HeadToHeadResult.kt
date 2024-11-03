package eywa.projectcodex.components.shootDetails.headToHeadEnd

import eywa.projectcodex.common.utils.ResOrActual

enum class HeadToHeadResult(
        val defaultPoints: Int,
        val shootOffPoints: Int,
        val title: ResOrActual<String>,
) {
    WIN(2, 1, title = ResOrActual.Actual("Win")),
    LOSS(0, 0, title = ResOrActual.Actual("Loss")),
    TIE(1, 0, title = ResOrActual.Actual("Tie")),
    INCOMPLETE(0, 0, title = ResOrActual.Actual("Incomplete")),
}
