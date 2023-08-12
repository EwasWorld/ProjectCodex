package eywa.projectcodex.common.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen

const val DEFAULT_INT_NAV_ARG = -1

fun String.toNavArgument() =
        try {
            NavArgument.valueOf(
                    Regex("[A-Z]").replace(this) { "_" + it.value }.uppercase()
            )
        }
        catch (e: IllegalArgumentException) {
            null
        }

fun <T> SavedStateHandle.get(argument: NavArgument): T? {
    var value = get<T>(argument.toArgName())
    if (value is Int) {
        value = value.takeIf { it != DEFAULT_INT_NAV_ARG }
    }
    return value
}

enum class NavArgument(val type: NavType<*>, val defaultValue: Any? = null) {
    SHOOT_ID(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    SCREEN(type = NavType.StringType, defaultValue = ArcherRoundScreen.SCORE_PAD.toString()),
    SIGHT_MARK_ID(type = NavType.IntType, defaultValue = DEFAULT_INT_NAV_ARG),
    ;

    fun toArgName() = Regex("_[a-z]").replace(name.lowercase()) { it.value.drop(1).uppercase() }
}
