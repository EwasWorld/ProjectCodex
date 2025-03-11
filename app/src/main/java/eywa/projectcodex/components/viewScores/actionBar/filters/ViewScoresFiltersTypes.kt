package eywa.projectcodex.components.viewScores.actionBar.filters

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual

enum class ViewScoresFiltersTypes(
        val label: ResOrActual<String>,
) {
    ALL(
            label = ResOrActual.StringResource(R.string.view_scores__filters_type_all),
    ),
    SCORE(
            label = ResOrActual.StringResource(R.string.create_round__score_type_score),
    ),
    COUNT(
            label = ResOrActual.StringResource(R.string.create_round__score_type_count),
    ),
    HEAD_TO_HEAD(
            label = ResOrActual.StringResource(R.string.create_round__score_type_head_to_head),
    ),
}
