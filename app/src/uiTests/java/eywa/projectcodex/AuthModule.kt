package eywa.projectcodex

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.common.utils.auth.AuthIntent
import eywa.projectcodex.common.utils.auth.AuthState
import eywa.projectcodex.common.utils.auth.AuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Singleton
    @Provides
    fun providesAuthUseCase() = object : AuthUseCase {
        override val state = MutableStateFlow(AuthState()).asStateFlow()

        override fun sendEvent(action: AuthIntent) {
            throw UnsupportedOperationException()
        }

        override suspend fun handleEvent(action: AuthIntent, context: Context) {
            throw UnsupportedOperationException()
        }
    }
}
