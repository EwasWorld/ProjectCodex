package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.navigation.CodexNavRoute

sealed class ShootDetailsResponse<T> {
    open val data: T? = null

    data class Error<T>(val mainMenuClicked: Boolean = false) : ShootDetailsResponse<T>()
    object Loading : ShootDetailsResponse<Any>()
    data class Loaded<T>(
            override val data: T,
            val shootId: Int,
            val navBarClicked: CodexNavRoute?,
    ) : ShootDetailsResponse<T>()
}
