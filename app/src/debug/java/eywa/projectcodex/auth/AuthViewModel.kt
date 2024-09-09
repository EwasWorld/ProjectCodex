package eywa.projectcodex.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.utils.auth.AuthUseCase
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
        val authUseCase: AuthUseCase,
) : ViewModel() {
    val state = authUseCase.state
}
