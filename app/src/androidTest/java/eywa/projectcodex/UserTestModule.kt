package eywa.projectcodex

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import eywa.projectcodex.model.user.Capability
import eywa.projectcodex.model.user.CodexUser
import javax.inject.Singleton

@Module
@TestInstallIn(
        components = [SingletonComponent::class],
        replaces = [UserModule::class],
)
object UserTestModule {
    var capabilities: List<Capability> = emptyList()
        set(value) {
            println("ECHDEBUG Setting capabilities to $value")
            field = value
        }

    @Singleton
    @Provides
    fun providesUser(): CodexUser {
        println("ECHDEBUG Providing user with capabilities $capabilities")
        return CodexUser(capabilities = capabilities)
    }
}
