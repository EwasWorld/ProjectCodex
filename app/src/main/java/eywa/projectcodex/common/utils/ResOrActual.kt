package eywa.projectcodex.common.utils

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class ResOrActual<T> {
    data class Actual<T>(val actual: T): ResOrActual<T>() {
        @Composable
        override fun get(): T = actual
        override fun get(resources: Resources): T = actual
    }

    data class StringResource(@StringRes val resId: Int, val args: List<String> = emptyList()): ResOrActual<String>() {
        @Composable
        override fun get(): String = stringResource(resId, *args.toTypedArray())
        override fun get(resources: Resources): String = resources.getString(resId, *args.toTypedArray())
    }

    @Composable
    abstract fun get(): T
    abstract fun get(resources: Resources): T
}
