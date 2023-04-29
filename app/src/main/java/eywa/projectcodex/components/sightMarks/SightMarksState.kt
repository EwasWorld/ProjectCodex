package eywa.projectcodex.components.sightMarks

import kotlin.math.*

data class SightMarksState(
        val sightMarks: List<SightMark> = listOf(),
        val isHighestNumberAtTheTop: Boolean = true,
) {
    private val highestSightMark = sightMarks.maxOf { it.sightMark }
    private val lowestSightMark = sightMarks.minOf { it.sightMark }
    private val majorTickDifferenceLog10 =
            (if (highestSightMark == lowestSightMark) highestSightMark else (highestSightMark - lowestSightMark)).let { difference ->
                floor(log10(abs(difference))).roundToInt()
            }

    val majorTickDifference = 10f.pow(majorTickDifferenceLog10)
    val maxMajorTick = roundMajorDiff(highestSightMark, ::ceil).addMajorTick(2)
    val minMajorTick = roundMajorDiff(lowestSightMark, ::floor).addMajorTick(-2)
    val totalMajorTicks = ((maxMajorTick - minMajorTick) / majorTickDifference).toInt()

    private fun Float.addMajorTick(n: Int) =
            if (highestSightMark == lowestSightMark) this + majorTickDifference * n else this

    private fun roundMajorDiff(value: Float, roundingFunction: (Float) -> Float) =
            (roundingFunction(value / majorTickDifference) * majorTickDifference)

    /**
     * Adjusts [SightMark.sightMark] to a vertical offset accounting for [minMajorTick] being non-zero
     * and [isHighestNumberAtTheTop]
     */
    fun getSightMarkAsPercentage(sightMark: SightMark) =
            (((sightMark.sightMark - minMajorTick) + (majorTickDifference / 2f)) / (maxMajorTick - minMajorTick))
                    .let { if (isHighestNumberAtTheTop) 1 - it else it }

    fun getMajorTickLabel(index: Int) =
            if (isHighestNumberAtTheTop) maxMajorTick - (index * majorTickDifference)
            else maxMajorTick + (index * majorTickDifference)

    fun formatTickLabel(label: Float) =
            if (majorTickDifferenceLog10 < 0) "%.${abs(majorTickDifferenceLog10)}f".format(label)
            else roundMajorDiff(label, ::round).roundToInt().toString()
}
