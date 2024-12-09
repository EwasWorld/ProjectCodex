package eywa.projectcodex.common.navigation

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType

const val DEFAULT_INT_NAV_ARG = -1

fun Bundle.toNavArgMap() =
        keySet()
                .mapNotNull { keyString ->
                    val arg = NavArgument.fromArgName(keyString) ?: return@mapNotNull null
                    val value = when (arg.type) {
                        NavType.IntType -> getInt(keyString).takeIf { it != DEFAULT_INT_NAV_ARG }?.toString()
                        else -> throw IllegalStateException("Unsupported type")
                    } ?: return@mapNotNull null
                    return@mapNotNull arg to value
                }
                .toMap()

fun <T> SavedStateHandle.get(argument: NavArgument): T? {
    var value = get<T>(argument.toArgName())
    if (value is Int) {
        value = value.takeIf { it != DEFAULT_INT_NAV_ARG }
    }
    return value
}

enum class NavArgument(val type: NavType<*>, val defaultValue: Any? = null) {
    SHOOT_ID(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    SIGHT_MARK_ID(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    DISTANCE(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    IS_METRIC(type = NavType.BoolType, defaultValue = true),
    FILTERS_ID(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    HANDICAP(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    ROUND_ID(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    ROUND_SUB_TYPE_ID(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    IS_SIGHTERS(type = NavType.BoolType, defaultValue = false),
    MATCH_NUMBER(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    SET_NUMBER(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    ;

    fun toArgName() = Regex("_[a-z]").replace(name.lowercase()) { it.value.drop(1).uppercase() }

    companion object {
        fun fromArgName(name: String) =
                try {
                    NavArgument.valueOf(
                            Regex("[A-Z]").replace(name) { "_" + it.value }.uppercase()
                    )
                }
                catch (e: IllegalArgumentException) {
                    null
                }
    }
}
