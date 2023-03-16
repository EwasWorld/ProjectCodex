package eywa.projectcodex.common.logging

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LoggerDiModule {
    @Singleton
    @Provides
    fun providesLogger(): CustomLogger = CustomLogger()
}
