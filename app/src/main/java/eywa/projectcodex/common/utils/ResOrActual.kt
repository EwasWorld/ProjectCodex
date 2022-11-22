package eywa.projectcodex.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class ResOrActual<T> private constructor(val res: Int?, val actual: T?) {
    init {
        require(((res == null) xor (actual == null))) { "Must define exactly one" }
    }

    @Composable
    fun get(getFromRes: @Composable (Int) -> T) = actual ?: getFromRes(res!!)

    companion object {
        fun <T> fromRes(res: Int) = ResOrActual<T>(res = res, actual = null)
        fun <T> fromActual(actual: T) = ResOrActual(res = null, actual = actual)
    }
}

@Composable
fun ResOrActual<String>.get() = get(getFromRes = { stringResource(it) })