package eywa.projectcodex.common.utils

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Collection<Int>.standardDeviationFromInts(isSample: Boolean = false) =
        map { it.toFloat() }.standardDeviation(isSample)

fun Collection<Float>.standardDeviation(isSample: Boolean = false): Float {
    val count = count().toFloat()
    val mean = sum() / count

    val divider = count - (if (isSample) 1 else 0)

    return map { value ->
        (value - mean).let { it * it }
    }.let {
        sqrt(it.sum() / divider)
    }
}

fun Float.roundToDp(decimalPlaces: Int = 2) =
        10f.pow(decimalPlaces).let { multiplier ->
            (this * multiplier).roundToInt() / multiplier
        }
