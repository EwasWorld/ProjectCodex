package eywa.projectcodex.hiltModules

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.NavRoute
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsScreen
import javax.inject.Singleton

enum class LocalNavRoute : NavRoute {
    ARCHER_HANDICAPS {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String =
                stringResource(R.string.archer_handicaps__title)

        @Composable
        override fun Screen(navController: NavController) {
            ArcherHandicapsScreen()
        }
    },
    ;

    override val routeBase = "local_" + name.lowercase()
    override val args: Map<NavArgument, Boolean> = emptyMap()
}

@Module
@InstallIn(SingletonComponent::class)
class LocalNavRouteModule {
    @Singleton
    @Provides
    @ElementsIntoSet
    fun providesLocalNavRoute(): Set<NavRoute> = LocalNavRoute.values().toSet()
}
