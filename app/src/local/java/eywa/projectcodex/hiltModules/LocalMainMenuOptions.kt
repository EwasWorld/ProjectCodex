package eywa.projectcodex.hiltModules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import eywa.projectcodex.common.navigation.ScreenNavRoute
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.mainMenu.MainMenuOption
import eywa.projectcodex.components.mainMenu.MainMenuState
import javax.inject.Singleton

enum class LocalMainMenuOptions(
        override val buttonTitle: ResOrActual<String>? = null,
        override val navRoute: ScreenNavRoute,
        override val helpTitle: ResOrActual<String>,
        override val helpBody: ResOrActual<String>,
        override val icon: CodexIconInfo? = null,
        override val shouldShow: (MainMenuState) -> Boolean = { true },
) : MainMenuOption, CodexTestTag {
    ;

    override val testTag: CodexTestTag
        get() = this

    override val order: Int
        get() = ordinal + 100

    override val screenName: String
        get() = "LOCAL_MAIN_MENU"

    override fun getElement(): String = name
}

@Module
@InstallIn(SingletonComponent::class)
class LocalMainMenuOptionsModule {
    @Singleton
    @Provides
    @ElementsIntoSet
    fun providesOptions(): Set<MainMenuOption> = LocalMainMenuOptions.values().toSet()
}
