package eywa.projectcodex.components.sightMarks

import kotlin.math.*

data class SightMarksState(
        val sightMarks: List<SightMark> = listOf(),
        val isHighestNumberAtTheTop: Boolean = true,
) {
    val highestSightMark = sightMarks.maxOf { it.sightMark }
    val lowestSightMark = sightMarks.minOf { it.sightMark }

    val majorTickDifference =
            (if (highestSightMark == lowestSightMark) highestSightMark else abs(highestSightMark - lowestSightMark))
                    .let { difference ->
                        10f.pow(
                                log10(difference).let { if (it > 0) ceil(it) - 1 else floor(it) }
                        )
                    }
    val maxMajorTick =
            (ceil(highestSightMark / majorTickDifference) * majorTickDifference)
                    .let { if (highestSightMark == lowestSightMark) it + majorTickDifference * 2 else it }
    val minMajorTick =
            (floor(lowestSightMark / majorTickDifference) * majorTickDifference)
                    .let { if (highestSightMark == lowestSightMark) it - majorTickDifference * 2 else it }

    val totalMajorTicks = ((maxMajorTick - minMajorTick) / majorTickDifference).toInt()

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
}
