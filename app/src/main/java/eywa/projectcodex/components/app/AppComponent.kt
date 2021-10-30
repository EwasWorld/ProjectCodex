package eywa.projectcodex.components.app

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel
import eywa.projectcodex.components.mainActivity.MainActivityViewModel
import eywa.projectcodex.components.newScore.NewScoreViewModel
import eywa.projectcodex.components.viewScores.ViewScoresViewModel
import eywa.projectcodex.database.DatabaseDaggerModule
import javax.inject.Singleton


/**
 * Provides all the objects that can be instantiated from dependency injection
 */
@Singleton
@Component(
        dependencies = [],
        modules = [
            AndroidInjectionModule::class, DatabaseDaggerModule::class
        ]
)
interface AppComponent {
    fun inject(app: App)

    fun inject(viewModel: ArcherRoundScoreViewModel)
    fun inject(viewModel: MainActivityViewModel)
    fun inject(viewModel: NewScoreViewModel)
    fun inject(viewModel: ViewScoresViewModel)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun dbModule(databaseDaggerModule: DatabaseDaggerModule): Builder
        fun build(): AppComponent
    }
}
