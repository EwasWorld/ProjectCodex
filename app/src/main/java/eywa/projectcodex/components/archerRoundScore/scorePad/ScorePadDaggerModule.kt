package eywa.projectcodex.components.archerRoundScore.scorePad

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eywa.projectcodex.common.utils.ViewModelAssistedFactory
import eywa.projectcodex.common.utils.ViewModelKey

@Module
abstract class ScorePadDaggerModule {
    @ContributesAndroidInjector
    abstract fun contributeInputEndFragmentAndroidInjector(): ScorePadFragment

    @Binds
    @IntoMap
    @ViewModelKey(ScorePadViewModel::class)
    abstract fun bindFactory(factory: ScorePadViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}