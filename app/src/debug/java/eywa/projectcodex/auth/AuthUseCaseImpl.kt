package eywa.projectcodex.auth

import android.content.Context
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.auth.AuthIntent
import eywa.projectcodex.common.utils.auth.AuthState
import eywa.projectcodex.common.utils.auth.AuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthUseCaseImpl : AuthUseCase {
    private val scheme = "demo"
    private var account: Auth0? = null
    private var accessToken: String? = null

    private val _state = MutableStateFlow(AuthState())
    override val state = _state.asStateFlow()

    private fun init(context: Context) {
        require(account == null) { "Already initialized" }
        account = Auth0(
                context.getString(R.string.com_auth0_client_id),
                context.getString(R.string.com_auth0_domain),
        )
    }

    override fun sendEvent(action: AuthIntent) {
        _state.update { it.copy(intents = it.intents.plus(action)) }
    }

    override suspend fun handleEvent(action: AuthIntent, context: Context) {
        when (action) {
            AuthIntent.Login -> {
                Log.i(LOG_TAG, "LOGIN")
                login(context).handleCall("Login") { _ ->
                    Log.i(LOG_TAG, "PROFILE")
                    showUserProfile().handleCall("Profile") { profile ->
                        Log.i(LOG_TAG, "PROFILE SUCCESS")
                        _state.update { it.copy(profile = profile) }
                    }
                }
            }

            AuthIntent.Logout -> {
                Log.i(LOG_TAG, "LOGOUT")
                logout(context).handleCall("Logout") { _ ->
                    Log.i(LOG_TAG, "LOGOUT SUCCESS")
                    _state.update { it.copy(profile = null) }
                }
            }
        }

        Log.i(LOG_TAG, "COMPLETE")
        _state.update { it.copy(intents = it.intents.minus(action)) }
    }

    private suspend fun <D> Response<D>.handleCall(functionName: String, onSuccess: suspend (D) -> Unit) {
        when (this) {
            is Response.Success -> onSuccess(this.data)
            is Response.Error -> {
                Log.i(LOG_TAG, "$functionName ERROR")
                _state.update { it.copy(error = "$functionName error: ${error.message}") }
            }
        }
    }

    private suspend fun login(context: Context): Response<Unit> =
            suspendCoroutine { cont ->
                if (account == null) {
                    init(context)
                }

                // Setup the WebAuthProvider, using the custom scheme and scope.
                WebAuthProvider
                        .login(account!!)
                        .withScheme(scheme)
                        .withScope("openid profile email")
                        // Launch the authentication passing the callback where the results will be received
                        .start(
                                context = context,
                                callback = object : Callback<Credentials, AuthenticationException> {
                                    // Called when authentication completed successfully
                                    override fun onSuccess(result: Credentials) {
                                        // Get the access token from the credentials object.
                                        // This can be used to call APIs
                                        Log.i(LOG_TAG, "Login success")
                                        accessToken = result.accessToken
                                        cont.resume(Response.Success(Unit))
                                    }

                                    // Called when there is an authentication failure
                                    override fun onFailure(error: AuthenticationException) {
                                        // Something went wrong!
                                        Log.i(LOG_TAG, "Login failure")
                                        cont.resume(Response.Error(error))
                                    }
                                },
                        )
            }

    private suspend fun logout(context: Context): Response<Unit> =
            suspendCoroutine { cont ->
                if (account == null) {
                    cont.resume(Response.Success(Unit))
                    return@suspendCoroutine
                }

                WebAuthProvider
                        .logout(account!!)
                        .withScheme(scheme)
                        .start(
                                context = context,
                                callback = object : Callback<Void?, AuthenticationException> {
                                    override fun onSuccess(result: Void?) {
                                        // The user has been logged out!
                                        Log.i(LOG_TAG, "Logout success")
                                        accessToken = null
                                        cont.resume(Response.Success(Unit))
                                    }

                                    override fun onFailure(error: AuthenticationException) {
                                        // Something went wrong!
                                        Log.i(LOG_TAG, "Logout failure")
                                        accessToken = null
                                        cont.resume(Response.Error(error))
                                    }
                                },
                        )
            }

    private suspend fun showUserProfile(): Response<AuthUseCase.Profile> =
            suspendCoroutine { cont ->
                val client = AuthenticationAPIClient(account!!)

                // With the access token, call `userInfo` and get the profile from Auth0.
                client
                        .userInfo(accessToken!!)
                        .start(
                                object : Callback<UserProfile, AuthenticationException> {
                                    override fun onSuccess(result: UserProfile) {
                                        // We have the user's profile!
                                        Log.i(LOG_TAG, "Profile success")
                                        val profile = AuthUseCase.Profile(result.email, result.name)
                                        cont.resume(Response.Success(profile))
                                    }

                                    override fun onFailure(error: AuthenticationException) {
                                        // Something went wrong!
                                        Log.i(LOG_TAG, "Profile failure")
                                        cont.resume(Response.Error(error))
                                    }
                                },
                        )
            }

    sealed class Response<Data> {
        data class Success<D>(val data: D) : Response<D>()
        data class Error<D>(val error: AuthenticationException) : Response<D>()
    }

    companion object {
        private const val LOG_TAG = "AUTH_USE_CASE"
    }
}
