package eywa.projectcodex.common

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import eywa.projectcodex.coaching.CoachingCrossHairTestScreen
import eywa.projectcodex.coaching.CoachingScreen
import eywa.projectcodex.common.navigation.BottomSheetNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.NavRoute
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.common.navigation.TabSwitcherItem
import eywa.projectcodex.common.utils.ResOrActual
import javax.inject.Singleton

enum class DebugNavRoute : NavRoute {
    COACHING {
        override val tabSwitcherItem = TabSwitcherItem(
                label = ResOrActual.Actual("Video"),
                group = TabSwitcherGroup.DEBUG_COACHING,
                navRoute = this,
                position = 1,
        )

        @Composable override fun getMenuBarTitle(entry: NavBackStackEntry?): String = "Coaching"

        @Composable override fun Screen(navController: NavController) {
            CoachingScreen()
        }
    },
    CROSS_HAIR_TEST {
        override val tabSwitcherItem = TabSwitcherItem(
                label = ResOrActual.Actual("Cross Hair"),
                group = TabSwitcherGroup.DEBUG_COACHING,
                navRoute = this,
                position = 0,
        )

        @Composable override fun getMenuBarTitle(entry: NavBackStackEntry?): String = "Cross Hair Test"

        @Composable override fun Screen(navController: NavController) {
            CoachingCrossHairTestScreen()
        }
    }
    ;

    override val routeBase = "debug_" + name.lowercase()
    override val args: Map<NavArgument, Boolean> = emptyMap()
    override val tabSwitcherItem: TabSwitcherItem? = null
    override val bottomSheets: List<BottomSheetNavRoute>? = null
}

@Module
@InstallIn(SingletonComponent::class)
class DebugNavRouteModule {
    @Singleton
    @Provides
    @ElementsIntoSet
    fun providesLocalNavRoute(): Set<NavRoute> = DebugNavRoute.values().toSet()
}