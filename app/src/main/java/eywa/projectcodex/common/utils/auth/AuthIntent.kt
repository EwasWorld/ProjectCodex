package eywa.projectcodex.common.utils.auth

sealed class AuthIntent {
    data object Login : AuthIntent()
    data object Logout : AuthIntent()
}
