package eywa.projectcodex.hiltModules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import eywa.projectcodex.common.navigation.NavRoute
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.mainMenu.MainMenuOption
import eywa.projectcodex.components.mainMenu.MainMenuState
import eywa.projectcodex.components.mainMenu.MainMenuTestTag
import javax.inject.Singleton

enum class LocalMainMenuOptions(
        override val buttonTitle: ResOrActual<String>? = null,
        override val navRoute: NavRoute,
        override val helpTitle: ResOrActual<String>,
        override val helpBody: ResOrActual<String>,
        override val testTag: CodexTestTag,
        override val icon: CodexIconInfo? = null,
        override val shouldShow: (MainMenuState) -> Boolean = { true },
) : MainMenuOption {
    ARCHER_HANDICAPS(
            buttonTitle = ResOrActual.Actual("Archer handicaps"),
            navRoute = LocalNavRoute.ARCHER_HANDICAPS,
            helpTitle = ResOrActual.Actual("Help title"),
            helpBody = ResOrActual.Actual("Help body"),
            testTag = MainMenuTestTag.ARCHER_INFO_BUTTON,
    ),
    ;

    override val order: Int
        get() = ordinal + 100
}

@Module
@InstallIn(SingletonComponent::class)
class LocalMainMenuOptionsModule {
    @Singleton
    @Provides
    @ElementsIntoSet
    fun providesOptions(): Set<MainMenuOption> = LocalMainMenuOptions.values().toSet()
}
