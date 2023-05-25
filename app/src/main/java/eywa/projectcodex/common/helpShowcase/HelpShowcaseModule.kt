package eywa.projectcodex.common.helpShowcase

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HelpShowcaseModule {
    @Singleton
    @Provides
    fun providesDefaultRoundsInfo() = HelpShowcaseUseCase()
}
