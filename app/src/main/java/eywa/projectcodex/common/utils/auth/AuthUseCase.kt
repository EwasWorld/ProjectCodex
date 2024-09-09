package eywa.projectcodex.common.utils.auth

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface AuthUseCase {
    val state: StateFlow<AuthState>
    fun sendEvent(action: AuthIntent)
    suspend fun handleEvent(action: AuthIntent, context: Context)

    data class Profile(
            val email: String?,
            val name: String?,
    )
}
