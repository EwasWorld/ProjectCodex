package eywa.projectcodex.common.utils

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class ResOrActual<T> private constructor(val res: Int?, val actual: T?) {
    init {
        require(((res == null) xor (actual == null))) { "Must define exactly one" }
    }

    @Composable
    internal fun getComposable(getFromRes: @Composable (Int) -> T) = actual ?: getFromRes(res!!)
    internal fun get(getFromRes: (Int) -> T) = actual ?: getFromRes(res!!)

    override fun equals(other: Any?): Boolean {
        if (other !is ResOrActual<*>) return false
        return res == other.res && actual == other.actual
    }

    override fun hashCode(): Int {
        var result = res ?: 0
        result = 31 * result + (actual?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun <T> fromRes(res: Int) = ResOrActual<T>(res = res, actual = null)
        fun <T> fromActual(actual: T) = ResOrActual(res = null, actual = actual)
    }
}

@Composable
fun ResOrActual<String>.get() = getComposable(getFromRes = { stringResource(it) })
fun ResOrActual<String>.get(resources: Resources) = get(getFromRes = { resources.getString(it) })