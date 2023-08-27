package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.navigation.CodexNavRoute

fun <T: Any> ShootDetailsResponse<T>.getData() = when (this) {
    is ShootDetailsResponse.Loaded<T> -> data
    else -> null
}

sealed class ShootDetailsResponse<in C : Any> {
    data class Error<T : Any>(val mainMenuClicked: Boolean = false) : ShootDetailsResponse<T>()
    object Loading : ShootDetailsResponse<Any>()
    data class Loaded<T : Any>(
            val data: T,
            val shootId: Int,
            val navBarClicked: CodexNavRoute?,
    ) : ShootDetailsResponse<T>()
}
