package eywa.projectcodex.components.mainActivity

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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.mainActivity.MainActivityIntent.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val LOG_TAG = "MainActivity"
    }

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navRoutes = CodexNavRoute.values()
        val duplicateRoutes = navRoutes
                .groupBy { it.routeBase.lowercase() }
                .filter { (_, v) -> v.size > 1 }
                .keys
        if (duplicateRoutes.isNotEmpty()) {
            throw IllegalStateException("Duplicate navRoutes found: " + duplicateRoutes.joinToString(","))
        }

        // TODO Don't re-run on activity recreate
        viewModel.updateDefaultRounds()


        setContent {
            CodexTheme {
                val navController = rememberNavController()

                val helpState by viewModel.helpShowcase.state.collectAsState()
                BackHandler(helpState.isInProgress) {
                    viewModel.helpShowcase.endShowcase()
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
                        NavHost(
                                navController = navController,
                                startDestination = CodexNavRoute.MAIN_MENU.routeBase,
                                modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                        ) {
                            navRoutes.forEach { route ->
                                route.create(this, navController)
                            }
                        }
                    }

                    val state by viewModel.state.collectAsState()

                    HelpItem(state)
                }
            }
        }
    }

    @Composable
    fun TopBar(navController: NavController) {
        val currentEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentEntry?.destination
                ?.route?.takeWhile { it != '/' && it != '?' }
                ?.let { CodexNavRoute.reverseMap[it] }

        TopAppBar(
                title = {
                    Text(
                            text = currentRoute?.getMenuBarTitle()
                                    ?: stringResource(R.string.app_name),
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center
                    )
                },
                backgroundColor = CodexTheme.colors.appBackground,
                actions = {
                    IconButton(
                            onClick = { viewModel.handle(StartHelpShowcase(null)) }
                    ) {
                        Icon(
                                painter = painterResource(R.drawable.ic_help_icon),
                                contentDescription = stringResource(R.string.action_bar__help),
                        )
                    }

                    IconButton(
                            onClick = {
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
                    ) {
                        Icon(
                                painter = painterResource(R.drawable.ic_home_icon),
                                contentDescription = stringResource(R.string.action_bar__home),
                        )
                    }
                }
        )
    }

    @Composable
    fun HelpItem(state: MainActivityState) {
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
                remember { Animatable(if (state.currentHelpItem == null) 0f else 1f) }

        val configuration = LocalConfiguration.current

        displayedHelpItem?.let { item ->
            item.helpShowcaseItem.asShape(
                    hasNextItem = item.hasNextItem,
                    goToNextItemListener = { viewModel.handle(GoToNextHelpShowcaseItem) },
                    endShowcaseListener = { viewModel.handle(CloseHelpShowcase) },
                    screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() },
                    screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() },
            )?.let { HelpShowcase(it, displayedHelpItemAnimationState.value) }
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
}
