package eywa.projectcodex.components.archerRoundScore.inputEnd

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eywa.projectcodex.common.utils.ViewModelKey
import eywa.projectcodex.database.ScoresRoomDatabase

@Module(includes = [InputEndDaggerModule.ProvideViewModel::class])
abstract class InputEndDaggerModule {
    @ContributesAndroidInjector(modules = [InjectViewModel::class])
    abstract fun contributeMainAndroidInjector(): InputEndFragment

    @Module
    class ProvideViewModel {
        @Provides
        @IntoMap
        @ViewModelKey(InputEndViewModel::class)
        fun provideInputEndViewModel(application: Application, db: ScoresRoomDatabase): ViewModel =
                InputEndViewModel(application, 1)
    }

    @Module
    class InjectViewModel {
        @Provides
        fun provideInputEndViewModel(factory: ViewModelProvider.Factory, target: InputEndFragment): InputEndViewModel =
                ViewModelProvider(target, factory).get(InputEndViewModel::class.java)
    }
}