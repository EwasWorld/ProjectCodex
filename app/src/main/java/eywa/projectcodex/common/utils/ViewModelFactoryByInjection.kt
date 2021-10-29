package eywa.projectcodex.common.utils

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject

open class ViewModelFactoryByInjection @Inject constructor(
        private val viewModelMap: MutableMap<Class<out ViewModel>, @JvmSuppressWildcards ViewModelAssistedFactory<out ViewModel>>
) {
    open fun create(owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null): ViewModelProvider.Factory {
        return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                return viewModelMap[modelClass]?.create(handle) as? T
                        ?: throw IllegalStateException("Unknown ViewModel class")
            }
        }
    }
}