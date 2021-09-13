package eywa.projectcodex.components.archerRoundScore.archerRoundStats

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eywa.projectcodex.common.utils.ViewModelAssistedFactory
import eywa.projectcodex.common.utils.ViewModelKey

@Module
abstract class ArcherRoundStatsDaggerModule {
    @ContributesAndroidInjector
    abstract fun contributeArcherRoundStatsFragmentAndroidInjector(): ArcherRoundStatsFragment

    @Binds
    @IntoMap
    @ViewModelKey(ArcherRoundStatsViewModel::class)
    abstract fun bindFactory(factory: ArcherRoundStatsViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}