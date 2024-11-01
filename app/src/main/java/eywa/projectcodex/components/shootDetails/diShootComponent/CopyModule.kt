package eywa.projectcodex.components.shootDetails.diShootComponent

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo

@InstallIn(ViewModelComponent::class)
@Module
object CopyModule {
    @Provides
    @Reusable
    fun provideEntryPoint(component: ShootComponent): CopyModuleEntryPoint {
        return getEntryPoint<CopyModuleEntryPoint>(component)
    }

    @Provides
    fun provideShootDetailsRepo(entryPoint: CopyModuleEntryPoint): ShootDetailsRepo {
        return entryPoint.getShootDetailsRepo()
    }
}

@InstallIn(ShootComponent::class)
@EntryPoint
interface CopyModuleEntryPoint {
    fun getShootDetailsRepo(): ShootDetailsRepo
}
