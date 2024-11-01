package eywa.projectcodex.components.shootDetails.diShootComponent

import androidx.lifecycle.SavedStateHandle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get

/**
 * Separate to [CopyModule] because different components will access the shootId in different ways.
 * If [CopyModule] also installed in the ActivityComponent, everything in there will work as long as there is a way
 * for the activity to get the component (by creating an activity equivalent of this module)
 */
@InstallIn(ViewModelComponent::class)
@Module
object ViewModelShootComponentModule {
    @Provides
    fun provideShootComponent(
            shootComponentManager: ShootComponentManager,
            savedStateHandle: SavedStateHandle,
    ): ShootComponent {
        val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!
        return shootComponentManager.getShootComponent(shootId)
    }
}
