package eywa.projectcodex.components.mainActivity

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eywa.projectcodex.common.utils.ViewModelKey
import eywa.projectcodex.database.ScoresRoomDatabase

@Module(includes = [ActivityDaggerModule.ProvideViewModel::class])
abstract class ActivityDaggerModule {
    @ContributesAndroidInjector(modules = [InjectViewModel::class])
    abstract fun contributeMainAndroidInjector(): MainActivity

    @Module
    class ProvideViewModel {
        @Provides
        @IntoMap
        @ViewModelKey(MainActivityViewModel::class)
        fun provideMainActivityViewModel(application: Application, db: ScoresRoomDatabase): ViewModel =
                MainActivityViewModel(application, db)
    }

    @Module
    class InjectViewModel {
        @Provides
        fun provideMainActivityViewModel(
                factory: ViewModelProvider.Factory,
                target: MainActivity
        ): MainActivityViewModel =
                ViewModelProvider(target, factory).get(MainActivityViewModel::class.java)
    }
}