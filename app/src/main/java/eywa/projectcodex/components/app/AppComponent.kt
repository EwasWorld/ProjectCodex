package eywa.projectcodex.components.app

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import eywa.projectcodex.components.archerRoundScore.archerRoundStats.ArcherRoundStatsDaggerModule
import eywa.projectcodex.components.archerRoundScore.inputEnd.InputEndDaggerModule
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadDaggerModule
import eywa.projectcodex.components.mainActivity.ActivityDaggerModule
import eywa.projectcodex.components.newScore.NewScoreDaggerModule
import eywa.projectcodex.components.viewScores.ViewScoresDaggerModule
import eywa.projectcodex.database.DatabaseDaggerModule
import javax.inject.Singleton


/**
 * Provides all the objects that can be instantiated from dependency injection
 */
@Singleton
@Component(
        dependencies = [],
        modules = [
            AndroidInjectionModule::class, AppModule::class, DatabaseDaggerModule::class,
            ActivityDaggerModule::class,
            ViewScoresDaggerModule::class, NewScoreDaggerModule::class,
            ArcherRoundStatsDaggerModule::class, InputEndDaggerModule::class, ScorePadDaggerModule::class,
        ]
)
interface AppComponent {
    fun inject(app: App)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun appModule(appModule: AppModule): Builder
        fun dbModule(databaseDaggerModule: DatabaseDaggerModule): Builder
        fun build(): AppComponent
    }
}
