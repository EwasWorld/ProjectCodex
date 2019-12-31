package eywa.projectcodex.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory<T>(val creator: () -> T) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(modelClass::class.java)) {
            return creator() as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
    }
}