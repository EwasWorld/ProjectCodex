package eywa.projectcodex.common.utils

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.assisted.AssistedInject

/**
 * Part of injecting a view model with parameters.
 *
 * The bindFactory() method inside each [Module] [Provides] these factories.
 *
 * @see AssistedInject
 */
interface ViewModelAssistedFactory<T : ViewModel> {
    fun create(stateHandle: SavedStateHandle): T
}