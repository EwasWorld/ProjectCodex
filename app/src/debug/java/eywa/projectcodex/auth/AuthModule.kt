package eywa.projectcodex.auth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.common.utils.auth.AuthUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Singleton
    @Provides
    fun providesAuthUseCase(): AuthUseCase = AuthUseCaseImpl()
}
