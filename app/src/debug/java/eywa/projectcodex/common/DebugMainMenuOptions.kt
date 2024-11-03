package eywa.projectcodex.common

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

enum class DebugMainMenuOptions(
        override val buttonTitle: ResOrActual<String>? = null,
        override val navRoute: ScreenNavRoute,
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
            shouldShow = { false },
    ),
    API(
            ResOrActual.Actual("Api"),
            DebugNavRoute.API,
            ResOrActual.Actual("Api Title"),
            ResOrActual.Actual("Api Body"),
            shouldShow = { false },
    ),
    PLOTTING(
            ResOrActual.Actual("Plotting"),
            DebugNavRoute.PLOTTING,
            ResOrActual.Actual("Plotting Title"),
            ResOrActual.Actual("Plotting Body"),
    ),
    AUTH(
            ResOrActual.Actual("Auth"),
            DebugNavRoute.AUTH,
            ResOrActual.Actual("Auth Title"),
            ResOrActual.Actual("Auth Body"),
            shouldShow = { false },
    ),
    H2H(
            ResOrActual.Actual("H2H"),
            DebugNavRoute.H2H,
            ResOrActual.Actual("H2h Title"),
            ResOrActual.Actual("H2h Body"),
    ),
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
    fun providesOptions(): Set<MainMenuOption> = DebugMainMenuOptions.entries.toSet()
}
