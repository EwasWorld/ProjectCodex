package eywa.projectcodex.components.shootDetails.diShootComponent

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Qualifier

@InstallIn(ShootComponent::class)
@Module
object ShootCoroutineModule {
    @Provides
    @ShootScoped
    @Shoot
    fun provideShootDefaultScope(shootLifecycle: ShootLifecycle): CoroutineScope {
        val context = Dispatchers.Default + SupervisorJob()
        shootLifecycle.addOnClearedListener { context.cancel() }
        return CoroutineScope(context)
    }
}

/**
 * Though currently not required, it's common to provide different [CoroutineScope]s, so this will prevent clashes
 * in the future
 */
@Qualifier
annotation class Shoot
