package eywa.projectcodex.common.diActivityHelpers

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// TODO_CURRENT swap to activity
@Module
@InstallIn(SingletonComponent::class)
class ActivityScopedModule {
    @Singleton
    @Provides
    fun provideShootIdsRepo() = ShootIdsUseCase()
}
