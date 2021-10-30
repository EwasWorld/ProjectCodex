package eywa.projectcodex.components.app

import androidx.annotation.VisibleForTesting
import androidx.multidex.MultiDexApplication
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import eywa.projectcodex.database.DatabaseDaggerModule
import javax.inject.Inject

open class App : MultiDexApplication(), HasAndroidInjector {
    @Inject
    open lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    lateinit var appComponent: AppComponent
        protected set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
                .application(this)
                .dbModule(DatabaseDaggerModule(this))
                .build()
        appComponent.inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    /**
     * Allow the test implementation to call super.onCreate without generating an [appComponent] at this level
     * as we don't want to generate any of the real modules during testing
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun superOnCreate() {
        super.onCreate()
    }
}