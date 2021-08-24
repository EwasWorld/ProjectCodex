package eywa.projectcodex.components.viewScores

import eywa.projectcodex.database.arrowValue.ArrowValue
import kotlinx.coroutines.Job

enum class ConvertScore(private val convert: (ArrowValue) -> ArrowValue) {
    TO_FIVE_ZONE({
        val scoreChange = when {
            it.score == 0 -> 0
            it.score % 2 == 0 -> -1
            else -> 0
        }
        ArrowValue(it.archerRoundId, it.arrowNumber, it.score + scoreChange, false)
    }),
    XS_TO_TENS({ ArrowValue(it.archerRoundId, it.arrowNumber, it.score, false) });

    /**
     * @return the database update job if any arrows were updated, else null
     */
    fun convertScore(arrows: List<ArrowValue>, convertScoreViewModel: ConvertScoreViewModel): Job? {
        val newArrows = mutableListOf<ArrowValue>()
        for (arrow in arrows) {
            val newArrow = convert(arrow)
            if (newArrow != arrow) {
                newArrows.add(newArrow)
            }
        }
        if (newArrows.isNotEmpty()) {
            return convertScoreViewModel.updateArrowValues(*newArrows.toTypedArray())
        }
        return null
    }

    interface ConvertScoreViewModel {
        fun updateArrowValues(vararg arrows: ArrowValue): Job
    }
}