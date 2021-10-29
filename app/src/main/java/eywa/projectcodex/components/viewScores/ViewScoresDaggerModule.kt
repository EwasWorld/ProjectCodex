package eywa.projectcodex.components.viewScores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eywa.projectcodex.common.utils.ViewModelKey
import eywa.projectcodex.database.ScoresRoomDatabase

@Module(includes = [ViewScoresDaggerModule.ProvideViewModel::class])
abstract class ViewScoresDaggerModule {
    @ContributesAndroidInjector(modules = [InjectViewModel::class])
    abstract fun contributeMainAndroidInjector(): ViewScoresFragment

    @Module
    class ProvideViewModel {
        @Provides
        @IntoMap
        @ViewModelKey(ViewScoresViewModel::class)
        fun provideNewScoreViewModel(application: Application, db: ScoresRoomDatabase): ViewModel =
                ViewScoresViewModel(application, db)
    }

    @Module
    class InjectViewModel {
        @Provides
        fun provideNewScoreViewModel(
                factory: ViewModelProvider.Factory,
                target: ViewScoresFragment
        ): ViewScoresViewModel = ViewModelProvider(target, factory).get(ViewScoresViewModel::class.java)
    }
}