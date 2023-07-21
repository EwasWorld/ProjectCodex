package eywa.projectcodex.model

import eywa.projectcodex.R

fun List<PbType>.getOverallPbString(isMultiRound: Boolean): Int {
    val types = distinct()
    val only = types.first()
    return when {
        types.size > 1 -> R.string.view_score__multiple_personal_bests
        isMultiRound && only.isSingle -> R.string.view_score__single_personal_best
        isMultiRound -> R.string.view_score__multi_personal_best
        only == PbType.SINGLE -> R.string.view_score__round_personal_best
        only == PbType.SINGLE_TIED -> R.string.view_score__round_personal_best_tied
        else -> throw IllegalStateException()
    }
}

enum class PbType(val isSingle: Boolean) {
    /**
     * Sole PB for a single round
     */
    SINGLE(true),

    /**
     * Tied PB for a single round
     */
    SINGLE_TIED(true),

    /**
     * Sole PB for a double (or more) round
     */
    MULTI(false),

    /**
     * Tied PB for a double (or more) round
     */
    MULTI_TIED(false),
}
