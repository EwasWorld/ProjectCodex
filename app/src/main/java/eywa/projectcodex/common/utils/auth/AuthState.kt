package eywa.projectcodex.common.utils.auth

data class AuthState(
        val profile: AuthUseCase.Profile? = null,
        val error: String? = null,
        val intents: List<AuthIntent> = listOf(),
)
