package eywa.projectcodex.components.viewScores.ui.filters

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.shootData.ShootFilter

enum class ViewScoresFiltersTypes(
        val label: ResOrActual<String>,
        val filter: ShootFilter?,
) {
    ALL(
            label = ResOrActual.StringResource(R.string.view_scores__filters_type_all),
            filter = null,
    ),
    SCORE(
            label = ResOrActual.StringResource(R.string.create_round__score_type_score),
            filter = ShootFilter.ArrowCounts(false),
    ),
    COUNT(
            label = ResOrActual.StringResource(R.string.create_round__score_type_count),
            filter = ShootFilter.ArrowCounts(true),
    ),
}
