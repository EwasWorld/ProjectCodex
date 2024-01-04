package eywa.projectcodex.common

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
import javax.inject.Singleton

enum class DebugMainMenuOptions(
        override val buttonTitle: ResOrActual<String>? = null,
        override val navRoute: NavRoute,
        override val helpTitle: ResOrActual<String>,
        override val helpBody: ResOrActual<String>,
        override val icon: CodexIconInfo? = null,
        override val shouldShow: (MainMenuState) -> Boolean = { true },
) : MainMenuOption, CodexTestTag {
    COACHING(
            ResOrActual.Actual("Coaching"),
            DebugNavRoute.COACHING,
            ResOrActual.Actual("Coaching Title"),
            ResOrActual.Actual("Coaching Body"),
    )
    ;

    override val testTag: CodexTestTag
        get() = this

    override val order: Int
        get() = ordinal + 200

    override val screenName: String
        get() = "DEBUG_MAIN_MENU"

    override fun getElement(): String = name
}

@Module
@InstallIn(SingletonComponent::class)
class DebugMainMenuOptionsModule {
    @Singleton
    @Provides
    @ElementsIntoSet
    fun providesOptions(): Set<MainMenuOption> = DebugMainMenuOptions.values().toSet()
}
