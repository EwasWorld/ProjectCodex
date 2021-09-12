package eywa.projectcodex.components.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import eywa.projectcodex.common.utils.AppViewModelFactory
import javax.inject.Provider


/**
 * Tells Dagger how to instantiate each object
 * TODO I think I want my two factories to have @Singleton
 */
@Module
class AppModule {
    @Provides
    fun provideViewModelFactory(
            providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory = AppViewModelFactory(providers)
}
