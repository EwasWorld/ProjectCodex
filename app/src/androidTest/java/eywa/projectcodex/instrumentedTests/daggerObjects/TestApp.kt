package eywa.projectcodex.instrumentedTests.daggerObjects

import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import eywa.projectcodex.components.app.App
import eywa.projectcodex.components.app.AppModule

class TestApp : App(), HasAndroidInjector {
    override fun onCreate() {
        super.onCreate()
        DaggerAppTestComponent.builder()
                .application(this)
                .appModule(AppModule())
                .dbModule(DatabaseDaggerTestModule())
                .build()
                .inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }
}