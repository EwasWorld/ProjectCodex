package eywa.projectcodex.hiltModules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import eywa.projectcodex.common.navigation.BottomSheetNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.ScreenNavRoute
import eywa.projectcodex.common.navigation.TabSwitcherItem
import javax.inject.Singleton

enum class LocalNavRoute : ScreenNavRoute {
    ;

    override val routeBase = "local_" + name.lowercase()
    override val args: Map<NavArgument, Boolean> = emptyMap()
    override val tabSwitcherItem: TabSwitcherItem? = null
    override val bottomSheets: List<BottomSheetNavRoute>? = null
}

@Module
@InstallIn(SingletonComponent::class)
class LocalNavRouteModule {
    @Singleton
    @Provides
    @ElementsIntoSet
    fun providesLocalNavRoute(): Set<ScreenNavRoute> = LocalNavRoute.values().toSet()
}
