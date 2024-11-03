package eywa.projectcodex.common.utils

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

abstract class ResOrActual<T> {
    data class Actual<T>(val actual: T) : ResOrActual<T>() {
        @Composable
        override fun get(): T = actual
        override fun get(resources: Resources): T = actual
    }

    data class StringResource(@StringRes val resId: Int, val args: List<Any> = emptyList()) : ResOrActual<String>() {
        @Composable
        override fun get(): String {
            val resolved = args.mapNotNull { if (it is ResOrActual<*>) it.get() else it }
            return stringResource(resId, *resolved.toTypedArray())
        }

        override fun get(resources: Resources): String {
            val resolved = args.map { if (it is ResOrActual<*>) it.get(resources) else it }
            return resources.getString(resId, *resolved.toTypedArray())
        }
    }

    data class JoinToStringResource(
            val strings: List<ResOrActual<String>>,
            @StringRes val delimiter: Int,
    ) : ResOrActual<String>() {
        @Composable
        override fun get(): String {
            val list = strings.map { it.get() }
            return list.joinToString(stringResource(delimiter))
        }

        override fun get(resources: Resources): String {
            val list = strings.map { it.get(resources) }
            return list.joinToString(resources.getString(delimiter))
        }
    }

    @Composable
    abstract fun get(): T
    abstract fun get(resources: Resources): T
}
