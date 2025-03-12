package eywa.projectcodex

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.model.user.Capability
import eywa.projectcodex.model.user.CodexUser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    @Singleton
    @Provides
    fun providesUser() = CodexUser(capabilities = Capability.entries)
}
