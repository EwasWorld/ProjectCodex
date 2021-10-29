package eywa.projectcodex.instrumentedTests.daggerObjects

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import eywa.projectcodex.components.app.App
import eywa.projectcodex.components.app.AppModule
import eywa.projectcodex.components.archerRoundScore.archerRoundStats.ArcherRoundStatsDaggerModule
import eywa.projectcodex.components.archerRoundScore.inputEnd.InputEndDaggerModule
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadDaggerModule
import eywa.projectcodex.components.mainActivity.ActivityDaggerModule
import eywa.projectcodex.components.newScore.NewScoreDaggerModule
import eywa.projectcodex.components.viewScores.ViewScoresDaggerModule
import javax.inject.Singleton


/**
 * Provides all the objects that can be instantiated from dependency injection
 */
@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class, AppModule::class, DatabaseDaggerTestModule::class,
            ActivityDaggerModule::class,
            ViewScoresDaggerModule::class, NewScoreDaggerModule::class,
            ArcherRoundStatsDaggerModule::class, InputEndDaggerModule::class, ScorePadDaggerModule::class,
        ]
)
interface AppTestComponent {
    fun inject(app: App)

    @Component.Builder
    interface TestBuilder {
        @BindsInstance
        fun application(application: Application): TestBuilder
        fun appModule(appModule: AppModule): TestBuilder
        fun dbModule(databaseDaggerTestModule: DatabaseDaggerTestModule): TestBuilder
        fun build(): AppTestComponent
    }
}
