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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcase
import eywa.projectcodex.common.navigation.CodexNavHost
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.ScreenNavRoute
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.core.mainActivity.MainActivityIntent.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var navRoutes: Set<@JvmSuppressWildcards ScreenNavRoute>

    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO Don't re-run on activity recreate
        viewModel.updateDefaultRounds()


        setContent {
            CodexTheme {
                val sheetState = rememberModalBottomSheetState(
                        initialValue = ModalBottomSheetValue.Hidden,
                        skipHalfExpanded = true,
                )
                val bottomSheetNavigator = remember { BottomSheetNavigator(sheetState) }
                val navController = rememberNavController(bottomSheetNavigator)

                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = CodexNavRoute.fromBackStackEntry(currentEntry)

                LaunchedEffect(currentRoute) {
                    if (currentRoute != null && currentRoute is ScreenNavRoute) {
                        viewModel.helpShowcase.handle(
                                action = HelpShowcaseIntent.SetScreen(currentRoute::class),
                                screen = currentRoute::class,
                        )
                    }
                }

                Auth(viewModel)

                window.statusBarColor = CodexTheme.colors.statusBar.toArgb()
                WindowCompat.getInsetsController(window, LocalView.current).isAppearanceLightStatusBars = false
                window.navigationBarColor = CodexTheme.colors.androidNavButtons.toArgb()
                WindowCompat.getInsetsController(window, LocalView.current).isAppearanceLightNavigationBars = true

                Box(
                        modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitPointerEvent()
                                        viewModel.handle(MainActivityIntent.PressDetected)
                                    }
                                }
                ) {

                    ModalBottomSheetLayout(
                            bottomSheetNavigator = bottomSheetNavigator,
                            sheetShape = RoundedCornerShape(
                                    topStart = CodexTheme.dimens.cornerRounding,
                                    topEnd = CodexTheme.dimens.cornerRounding,
                            ),
                            sheetBackgroundColor = CodexTheme.colors.dialogBackground,
                    ) {
                        Scaffold(
                                backgroundColor = CodexTheme.colors.appBackground,
                                contentColor = CodexTheme.colors.onAppBackground,
                                topBar = { TopBar(navController) },
                        ) { padding ->
                            CodexNavHost(
                                    navRoutes = navRoutes,
                                    navHostController = navController,
                                    modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
                            )
                        }
                    }

                    HelpItem()
                }
            }
        }
    }

    @Composable
    fun Auth(viewModel: MainActivityViewModel) {
        val state by viewModel.authUseCase.state.collectAsState()

        LaunchedEffect(state.intents) {
            if (state.intents.isNotEmpty()) {
                // Because this launches an activity, it needs to be called from an activity context
                viewModel.authUseCase.handleEvent(state.intents[0], this@MainActivity)
            }
        }
    }

    @Composable
    fun TopBar(navController: NavController) {
        val currentEntry by navController.currentBackStackEntryAsState()
        val currentRoute = CodexNavRoute.fromBackStackEntry(currentEntry)

        TopAppBar(
                title = {
                    Text(
                            text = currentRoute?.getMenuBarTitle(currentEntry)
                                    ?: stringResource(R.string.app_name),
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center,
                    )
                },
                backgroundColor = CodexTheme.colors.appBackground,
                actions = {
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(
                                    imageVector = CodexTheme.icons.helpInfo,
                                    contentDescription = stringResource(R.string.action_bar__help),
                            ),
                            modifier = Modifier.testTag(MainActivityTestTag.HELP_ICON)
                    ) { viewModel.handle(StartHelpShowcase(currentRoute)) }

                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = stringResource(R.string.action_bar__home),
                            ),
                            modifier = Modifier.testTag(MainActivityTestTag.HOME_ICON)
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
                },
        )
    }

    @Composable
    fun HelpItem() {
        val state by viewModel.state.collectAsState()
        val configuration = LocalConfiguration.current
        val screenSize = Size(
                height = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() },
                width = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() },
        )

        BackHandler(state.helpShowcaseState?.isInProgress == true) {
            viewModel.helpShowcase.endShowcase()
        }

        LaunchedEffect(Unit) {
            viewModel.handle(MainActivityIntent.SetScreenSize(screenSize))
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

        displayedHelpItem?.let { item ->
            item.helpShowcaseItem.asShape(
                    boundaries = state.helpShowcaseState?.boundaries ?: emptyMap(),
                    hasNextItem = item.hasNextItem,
                    goToNextItemListener = { viewModel.handle(GoToNextHelpShowcaseItem) },
                    endShowcaseListener = { viewModel.handle(CloseHelpShowcase) },
                    screenSize = screenSize,
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
                                easing = FastOutLinearInEasing,
                        ),
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
                                easing = LinearOutSlowInEasing,
                        ),
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
