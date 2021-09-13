package eywa.projectcodex.components.archerRoundScore.inputEnd

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eywa.projectcodex.common.utils.ViewModelAssistedFactory
import eywa.projectcodex.common.utils.ViewModelKey

@Module
abstract class InputEndDaggerModule {
    @ContributesAndroidInjector
    abstract fun contributeInputEndFragmentAndroidInjector(): InputEndFragment

    @ContributesAndroidInjector
    abstract fun contributeEditEndFragmentAndroidInjector(): EditEndFragment

    @ContributesAndroidInjector
    abstract fun contributeInsertEndFragmentAndroidInjector(): InsertEndFragment

    @Binds
    @IntoMap
    @ViewModelKey(InputEndViewModel::class)
    abstract fun bindFactory(factory: InputEndViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}