package eywa.projectcodex.components.newScore

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eywa.projectcodex.common.utils.ViewModelKey
import eywa.projectcodex.database.ScoresRoomDatabase

@Module(includes = [NewScoreDaggerModule.ProvideViewModel::class])
abstract class NewScoreDaggerModule {
    @ContributesAndroidInjector(modules = [InjectViewModel::class])
    abstract fun contributeMainAndroidInjector(): NewScoreFragment

    @Module
    class ProvideViewModel {
        @Provides
        @IntoMap
        @ViewModelKey(NewScoreViewModel::class)
        fun provideNewScoreViewModel(application: Application, db: ScoresRoomDatabase): ViewModel =
                NewScoreViewModel(application, db)
    }

    @Module
    class InjectViewModel {
        @Provides
        fun provideNewScoreViewModel(factory: ViewModelProvider.Factory, target: NewScoreFragment): NewScoreViewModel =
                ViewModelProvider(target, factory).get(NewScoreViewModel::class.java)
    }
}