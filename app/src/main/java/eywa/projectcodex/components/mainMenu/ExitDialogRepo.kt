package eywa.projectcodex.components.mainMenu

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Singleton

data class ExitDialogState(
        val isOpen: Boolean = false,
        val closeApplicationClicked: Boolean = false,
)

class ExitDialogRepo {
    private val _state = MutableStateFlow(ExitDialogState())
    val state = _state.asStateFlow()

    fun reduce(new: ExitDialogState) {
        _state.update { new }
    }
}


@Module
@InstallIn(SingletonComponent::class)
class ExitDialogModule {
    @Singleton
    @Provides
    fun providesExitDialogRepo() = ExitDialogRepo()
}
