package eywa.projectcodex.components.mainMenu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavRoute
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import javax.inject.Singleton

interface MainMenuOption {
    val buttonTitle: ResOrActual<String>?
    val navRoute: NavRoute
    val helpTitle: ResOrActual<String>
    val helpBody: ResOrActual<String>
    val testTag: CodexTestTag
    val icon: CodexIconInfo?
    val shouldShow: (MainMenuState) -> Boolean
    val order: Int
}

enum class MainMenuDefaultOptions(
        override val buttonTitle: ResOrActual<String>? = null,
        override val navRoute: NavRoute,
        override val helpTitle: ResOrActual<String>,
        override val helpBody: ResOrActual<String>,
        override val testTag: CodexTestTag,
        override val icon: CodexIconInfo? = null,
        override val shouldShow: (MainMenuState) -> Boolean = { true },
) : MainMenuOption {
    NEW_SCORE(
            buttonTitle = ResOrActual.StringResource(R.string.main_menu__new_score),
            navRoute = CodexNavRoute.NEW_SCORE,
            helpTitle = ResOrActual.StringResource(R.string.help_main_menu__new_score_title),
            helpBody = ResOrActual.StringResource(R.string.help_main_menu__new_score_body),
            testTag = MainMenuTestTag.NEW_SCORE_BUTTON,
    ),
    VIEW_SCORES(
            buttonTitle = ResOrActual.StringResource(R.string.main_menu__view_scores),
            navRoute = CodexNavRoute.VIEW_SCORES,
            helpTitle = ResOrActual.StringResource(R.string.help_main_menu__view_scores_title),
            helpBody = ResOrActual.StringResource(R.string.help_main_menu__view_scores_body),
            testTag = MainMenuTestTag.VIEW_SCORE_BUTTON,
    ),
    ARCHER_INFO(
            buttonTitle = ResOrActual.StringResource(R.string.main_menu__archer_info),
            navRoute = CodexNavRoute.ARCHER_INFO,
            helpTitle = ResOrActual.StringResource(R.string.help_main_menu__archer_info_title),
            helpBody = ResOrActual.StringResource(R.string.help_main_menu__archer_info_body),
            testTag = MainMenuTestTag.ARCHER_INFO_BUTTON,
    ),
    HANDICAP_TABLE(
            buttonTitle = ResOrActual.StringResource(R.string.main_menu__handicap_tables),
            navRoute = CodexNavRoute.HANDICAP_TABLES,
            helpTitle = ResOrActual.StringResource(R.string.help_main_menu__handicap_tables_title),
            helpBody = ResOrActual.StringResource(R.string.help_main_menu__handicap_tables_body),
            testTag = MainMenuTestTag.HANDICAP_TABLES_BUTTON,
    ),
    CLASSIFICATIONS(
            buttonTitle = ResOrActual.StringResource(R.string.main_menu__classification_tables),
            navRoute = CodexNavRoute.CLASSIFICATION_TABLES,
            helpTitle = ResOrActual.StringResource(R.string.help_main_menu__classification_tables_title),
            helpBody = ResOrActual.StringResource(R.string.help_main_menu__classification_tables_body),
            testTag = MainMenuTestTag.CLASSIFICATION_TABLES_BUTTON,
    ),
    SIGHT_MARKS(
            buttonTitle = ResOrActual.StringResource(R.string.main_menu__sight_marks),
            navRoute = CodexNavRoute.SIGHT_MARKS,
            helpTitle = ResOrActual.StringResource(R.string.help_main_menu__sight_marks_title),
            helpBody = ResOrActual.StringResource(R.string.help_main_menu__sight_marks_body),
            testTag = MainMenuTestTag.SIGHT_MARKS_BUTTON,
    ),
    SETTINGS(
            icon = CodexIconInfo.VectorIcon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = ResOrActual.StringResource(R.string.main_menu__settings),
            ),
            navRoute = CodexNavRoute.SETTINGS,
            helpTitle = ResOrActual.StringResource(R.string.help_main_menu__settings_title),
            helpBody = ResOrActual.StringResource(R.string.help_main_menu__settings_body),
            testTag = MainMenuTestTag.SETTINGS_BUTTON,
    ),
    ABOUT(
            icon = CodexIconInfo.VectorIcon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = ResOrActual.StringResource(R.string.main_menu__about),
            ),
            navRoute = CodexNavRoute.ABOUT,
            helpTitle = ResOrActual.StringResource(R.string.help_main_menu__about_title),
            helpBody = ResOrActual.StringResource(R.string.help_main_menu__about_body),
            testTag = MainMenuTestTag.ABOUT_BUTTON,
    ),
    ;

    override val order: Int
        get() = ordinal
}

@Module
@InstallIn(SingletonComponent::class)
class MainMenuDefaultOptionsModule {
    @Singleton
    @Provides
    @ElementsIntoSet
    fun providesOptions(): Set<MainMenuOption> = MainMenuDefaultOptions.values().toSet()
}
