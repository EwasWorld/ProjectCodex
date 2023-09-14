package eywa.projectcodex.core.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcase
import eywa.projectcodex.common.navigation.CodexNavHost
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavRoute
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.core.mainActivity.MainActivityIntent.ClearNoHelpShowcaseFlag
import eywa.projectcodex.core.mainActivity.MainActivityIntent.CloseHelpShowcase
import eywa.projectcodex.core.mainActivity.MainActivityIntent.GoToNextHelpShowcaseItem
import eywa.projectcodex.core.mainActivity.MainActivityIntent.StartHelpShowcase
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var shootDetailsRepo: ShootDetailsRepo

    @Inject
    lateinit var navRoutes: Set<@JvmSuppressWildcards NavRoute>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shootDetailsRepo.connect { lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.RESUMED) { it() } } }
        // TODO Don't re-run on activity recreate
        viewModel.updateDefaultRounds()


        setContent {
            CodexTheme {

                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentEntry?.currentCodexNavRoute()

                LaunchedEffect(currentRoute) {
                    currentRoute?.let {
                        viewModel.helpShowcase.handle(HelpShowcaseIntent.SetScreen(it::class), it::class)
                    }
                }

                window.statusBarColor = CodexTheme.colors.statusBar.toArgb()
                WindowCompat.getInsetsController(window, LocalView.current).isAppearanceLightStatusBars = false
                window.navigationBarColor = CodexTheme.colors.androidNavButtons.toArgb()
                WindowCompat.getInsetsController(window, LocalView.current).isAppearanceLightNavigationBars = true

                Box(
                        modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold(
                            backgroundColor = CodexTheme.colors.appBackground,
                            contentColor = CodexTheme.colors.onAppBackground,
                            topBar = { TopBar(navController) }
                    ) { padding ->
                        CodexNavHost(
                                navRoutes = navRoutes,
                                navHostController = navController,
                                modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                        )
                    }

                    HelpItem()
                }
            }
        }
    }

    @Composable
    private fun NavBackStackEntry.currentCodexNavRoute() = destination
            .route?.takeWhile { it != '/' && it != '?' }
            .let { CodexNavRoute.baseRouteMapping[it] }

    @Composable
    fun TopBar(navController: NavController) {
        val currentEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentEntry?.currentCodexNavRoute()

        TopAppBar(
                title = {
                    Text(
                            text = currentRoute?.getMenuBarTitle(currentEntry)
                                    ?: stringResource(R.string.app_name),
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center
                    )
                },
                backgroundColor = CodexTheme.colors.appBackground,
                actions = {
                    CodexIconButton(
                            icon = CodexIconInfo.PainterIcon(
                                    drawable = R.drawable.ic_help_icon,
                                    contentDescription = stringResource(R.string.action_bar__help),
                            ),
                            modifier = Modifier.testTag(MainActivityTestTag.HELP_ICON.getTestTag())
                    ) { viewModel.handle(StartHelpShowcase(currentRoute)) }

                    CodexIconButton(
                            icon = CodexIconInfo.PainterIcon(
                                    drawable = R.drawable.ic_home_icon,
                                    contentDescription = stringResource(R.string.action_bar__home),
                            ),
                            modifier = Modifier.testTag(MainActivityTestTag.HOME_ICON.getTestTag())
                    ) {
                        with(navController) {
                            if (currentRoute == CodexNavRoute.MAIN_MENU) {
                                ToastSpamPrevention.displayToast(
                                        applicationContext,
                                        resources.getString(R.string.err_action_bar__home_already_displayed),
                                )
                                return@with
                            }
                            popBackStack(CodexNavRoute.MAIN_MENU.routeBase, false)
                        }
                    }
                }
        )
    }

    @Composable
    fun HelpItem() {
        val state by viewModel.state.collectAsState()

        BackHandler(state.helpShowcaseState?.isInProgress == true) {
            viewModel.helpShowcase.endShowcase()
        }

        LaunchedEffect(state.helpShowcaseState?.startedButNoItems) {
            launch {
                if (state.helpShowcaseState?.startedButNoItems == true) {
                    ToastSpamPrevention.displayToast(
                            applicationContext,
                            resources.getString(R.string.err_action_bar__no_help_info),
                    )
                    viewModel.handle(ClearNoHelpShowcaseFlag)
                }
            }
        }

        var displayedHelpItem by remember { mutableStateOf(state.currentHelpItem) }
        // 0 for invisible, 1 for visible
        val displayedHelpItemAnimationState =
                remember { Animatable(if (displayedHelpItem == null) 0f else 1f) }

        val configuration = LocalConfiguration.current

        displayedHelpItem?.let { item ->
            item.helpShowcaseItem.asShape(
                    hasNextItem = item.hasNextItem,
                    goToNextItemListener = { viewModel.handle(GoToNextHelpShowcaseItem) },
                    endShowcaseListener = { viewModel.handle(CloseHelpShowcase) },
                    screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() },
                    screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() },
            )?.let {
                HelpShowcase(it, displayedHelpItemAnimationState.value)
            }
        }

        LaunchedEffect(state.currentHelpItem) {
            if (state.currentHelpItem == displayedHelpItem) return@LaunchedEffect

            if (state.currentHelpItem?.helpShowcaseItem?.helpTitle
                == displayedHelpItem?.helpShowcaseItem?.helpTitle
            ) {
                // Update item with no animation
                displayedHelpItem = state.currentHelpItem
                return@LaunchedEffect
            }

            val animationDuration = 300

            // Old item exit transition
            if (displayedHelpItem != null) {
                displayedHelpItemAnimationState.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                                durationMillis = animationDuration,
                                easing = FastOutLinearInEasing
                        )
                )
            }
            else if (displayedHelpItemAnimationState.targetValue != 0f) {
                displayedHelpItemAnimationState.snapTo(0f)
            }

            // Swap to the new item
            displayedHelpItem = state.currentHelpItem

            // New item entrance transition
            if (displayedHelpItem != null) {
                displayedHelpItemAnimationState.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                                durationMillis = animationDuration,
                                easing = LinearOutSlowInEasing
                        )
                )
            }
        }
    }

    enum class MainActivityTestTag : CodexTestTag {
        SCREEN,
        HOME_ICON,
        HELP_ICON,
        ;

        override val screenName: String
            get() = "MAIN_ACTIVITY"

        override fun getElement(): String = name
    }
}
