package eywa.projectcodex.instrumentedTests.daggerObjects

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import eywa.projectcodex.components.app.AppComponent
import javax.inject.Singleton


/**
 * Provides all the objects that can be instantiated from dependency injection
 */
@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class, DatabaseDaggerTestModule::class,
        ]
)
interface AppTestComponent : AppComponent {
    @Component.Builder
    interface TestBuilder {
        @BindsInstance
        fun application(application: Application): TestBuilder
        fun dbModule(databaseDaggerTestModule: DatabaseDaggerTestModule): TestBuilder
        fun build(): AppTestComponent
    }
}
