package eywa.projectcodex.components.shootDetails.headToHead

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual

enum class HeadToHeadResult(
        val defaultPoints: Int,
        val shootOffPoints: Int,
        val title: ResOrActual<String>,
        val isComplete: Boolean = false,
) {
    WIN(
            defaultPoints = 2,
            shootOffPoints = 1,
            title = ResOrActual.StringResource(R.string.head_to_head_add_end__result_win),
            isComplete = true,
    ),
    LOSS(
            defaultPoints = 0,
            shootOffPoints = 0,
            title = ResOrActual.StringResource(R.string.head_to_head_add_end__result_loss),
            isComplete = true,
    ),
    TIE(
            defaultPoints = 1,
            shootOffPoints = 0,
            title = ResOrActual.StringResource(R.string.head_to_head_add_end__result_tie),
            isComplete = true,
    ),
    INCOMPLETE(
            defaultPoints = 0,
            shootOffPoints = 0,
            title = ResOrActual.StringResource(R.string.head_to_head_add_end__result_incomplete),
    ),
    UNKNOWN(
            defaultPoints = 0,
            shootOffPoints = 0,
            title = ResOrActual.StringResource(R.string.head_to_head_add_end__result_unknown),
    ),
    ;

    fun opposite(): HeadToHeadResult = when (this) {
        WIN -> LOSS
        LOSS -> WIN
        else -> this
    }

    companion object {
        val defaultPointsBackwardsMap = entries.associateBy { it.defaultPoints }
    }
}
