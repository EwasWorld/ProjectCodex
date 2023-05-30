package eywa.projectcodex.common.utils

import kotlin.math.abs
import kotlin.math.sqrt

fun Collection<Int>.standardDeviationInt(isSample: Boolean = false) =
        map { it.toFloat() }.standardDeviation(isSample)

fun Collection<Float>.standardDeviation(isSample: Boolean = false): Float {
    val count = count().toFloat()
    val mean = sum() / count

    val divider = count - (if (isSample) 1 else 0)

    return map { value ->
        abs(value - mean).let { it * it }
    }.let {
        sqrt(it.sum() / divider)
    }
}
