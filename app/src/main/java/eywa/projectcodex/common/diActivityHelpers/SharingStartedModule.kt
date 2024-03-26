package eywa.projectcodex.common.diActivityHelpers

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SharingStartedModule {
    @Singleton
    @Provides
    fun provideSharingStarted() = SharingStarted.WhileSubscribed(1000)
}
