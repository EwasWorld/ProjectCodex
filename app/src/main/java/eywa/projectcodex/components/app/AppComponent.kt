package eywa.projectcodex.components.app

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import eywa.projectcodex.components.mainActivity.ActivityDaggerModule
import eywa.projectcodex.components.newScore.NewScoreDaggerModule
import eywa.projectcodex.database.DatabaseDaggerModule
import eywa.projectcodex.database.ScoresRoomDatabase
import javax.inject.Singleton


/**
 * Provides all the objects that can be instantiated from dependency injection
 */
@Singleton
@Component(
        dependencies = [],
        modules = [
            AndroidInjectionModule::class, AppModule::class, DatabaseDaggerModule::class,
            ActivityDaggerModule::class, NewScoreDaggerModule::class
        ]
)
interface AppComponent {
    fun inject(app: App)
    fun scoresRoomDatabase(): ScoresRoomDatabase

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun appModule(appModule: AppModule): Builder
        fun dbModule(databaseDaggerModule: DatabaseDaggerModule): Builder
        fun build(): AppComponent
    }
}
