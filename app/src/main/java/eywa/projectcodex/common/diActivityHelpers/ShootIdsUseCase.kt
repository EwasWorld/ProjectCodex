package eywa.projectcodex.common.diActivityHelpers

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShootIdsUseCase {
    private val items: MutableStateFlow<List<Int>?> = MutableStateFlow(null)
    val getItems = items.asStateFlow()

    fun setItems(list: List<Int>) {
        items.update { list }
    }

    fun clear() {
        items.update { null }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
class ActivityRetainedScopedModule {
    @ActivityRetainedScoped
    @Provides
    fun provideShootIdsRepo() = ShootIdsUseCase()
}
