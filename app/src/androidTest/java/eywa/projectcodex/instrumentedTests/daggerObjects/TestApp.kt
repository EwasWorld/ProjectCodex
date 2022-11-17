package eywa.projectcodex.instrumentedTests.daggerObjects

import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import eywa.projectcodex.components.app.App

// TODO Move into /uiTests
class TestApp : App(), HasAndroidInjector {
    override fun onCreate() {
        super.superOnCreate()
        appComponent = DaggerAppTestComponent.builder()
                .application(this)
                .dbModule(DatabaseDaggerTestModule())
                .build()
        appComponent.inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }
}