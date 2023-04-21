package eywa.projectcodex.components.mainActivity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.common.utils.getColourResource
import eywa.projectcodex.components.mainActivity.MainActivityIntent.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private const val LOG_TAG = "MainActivity"
    }

    lateinit var navHostFragment: NavHostFragment
    private val viewModel: MainActivityViewModel by viewModels()

    /**
     * Stores destination IDs of fragments which will be returned to when the back button is pressed
     * This is a list rather than a stack due to the need to remove all duplicates of certain items at certain times
     */
    private val customBackStack = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setStatusBarColor(viewModel.state.value.currentHelpItem != null)


        // TODO Don't re-run on activity recreate
        viewModel.updateDefaultRounds()

        findViewById<ComposeView>(R.id.content_main_compose).apply {
            setContent {
                val state by viewModel.state.collectAsState()

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

                displayedHelpItem?.let { item ->
                    item.helpShowcaseItem.Showcase(
                            hasNextItem = item.hasNextItem,
                            goToNextItemListener = { viewModel.handle(GoToNextHelpShowcaseItem) },
                            endShowcaseListener = { viewModel.handle(CloseHelpShowcase) },
                            screenHeight = height.toFloat(),
                            screenWidth = width.toFloat(),
                            animationState = displayedHelpItemAnimationState.value,
                    )
                }

                LaunchedEffect(key1 = state.currentHelpItem) {
                    if (state.currentHelpItem == displayedHelpItem) return@LaunchedEffect

                    if (state.currentHelpItem?.helpShowcaseItem?.helpTitle
                        == displayedHelpItem?.helpShowcaseItem?.helpTitle
                    ) {
                        // Update item with no animation
                        displayedHelpItem = state.currentHelpItem
                        setStatusBarColor(displayedHelpItem != null)
                        return@LaunchedEffect
                    }

                    val oldItem = displayedHelpItem
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

                    // Set action bar color based on whether a compose help item is displayed
                    // TODO Remove when swapped to a compose action bar (help showcase should sit over that one)
                    if (oldItem == null || displayedHelpItem == null) {
                        setStatusBarColor(displayedHelpItem != null)
                    }
                }
            }
        }

        navHostFragment = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                ?: throw IllegalStateException("No NavHost found")) as NavHostFragment

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            if (getBackStackBehaviour(destination) != BackStackBehaviour.NONE) {
                customBackStack.add(destination.id)
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            val navController = navHostFragment.navController

            fun clearBackStackAndReturnToMainMenu() {
                CustomLogger.customLogger.i(LOG_TAG, "Popping backstack to main menu")
                if (!navController.popBackStack(R.id.mainMenuFragment, false)) {
                    navController.navigate(R.id.mainMenuFragment)
                }
            }

            /*
             * Find the first destination that is not the current
             */
            var nextDestination: Int
            do {
                if (customBackStack.isEmpty()) {
                    if (!navController.popBackStack()) {
                        // If there was nowhere to pop to, go to the main menu
                        navController.navigate(R.id.mainMenuFragment)
                    }
                    return@addCallback
                }
                nextDestination = customBackStack.removeLast()
            } while (navController.currentDestination?.id == nextDestination)

            /*
             * Ensure it won't pop to the same place next time
             */
            while (customBackStack.isNotEmpty() && customBackStack.last() == nextDestination) {
                customBackStack.removeLast()
            }

            if (getBackStackBehaviour(navController.currentDestination) == BackStackBehaviour.SINGLE) {
                customBackStack.removeAll { it == navController.currentDestination?.id }
            }

            /*
             * Actually pop the back stack
             */
            if (!navController.popBackStack(nextDestination, false)) {
                // If there was nowhere to pop to
                CustomLogger.customLogger.w(LOG_TAG, "Pop to $nextDestination failed")
                clearBackStackAndReturnToMainMenu()
            }
        }
    }

    private fun setStatusBarColor(helpShowcaseShown: Boolean) {
        val color = if (helpShowcaseShown) R.color.colorPrimaryDarkTransparent else R.color.colorPrimary
        supportActionBar?.setBackgroundDrawable(
                ColorDrawable(getColourResource(resources, color, theme))
        )
    }

    private fun getBackStackBehaviour(destination: NavDestination?): BackStackBehaviour = destination?.arguments
            ?.get("backStackBehaviour")?.defaultValue as? BackStackBehaviour ?: BackStackBehaviour.NORMAL

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (viewModel.state.value.helpShowcaseState?.isInProgress == true) {
            viewModel.handle(GoToNextHelpShowcaseItem)
            return true
        }

        @Suppress("UNCHECKED_CAST")
        when (item.itemId) {
            R.id.action_bar__help ->
                viewModel.handle(StartHelpShowcase(findAllActionBarChildFragments(navHostFragment).firstOrNull()))
            R.id.action_bar__home -> {
                with(navHostFragment.findNavController()) {
                    if (currentDestination?.id == R.id.mainMenuFragment) {
                        ToastSpamPrevention.displayToast(
                                applicationContext,
                                resources.getString(R.string.err_action_bar__home_already_displayed),
                        )
                        return@with
                    }
                    popBackStack(R.id.mainMenuFragment, false)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * @return all visible children of [fragment] that are instances of [ActionBarHelp]. Includes all children of
     * children. Does not include [fragment] itself
     */
    private fun findAllActionBarChildFragments(fragment: Fragment): List<ActionBarHelp> {
        val allFragments = mutableListOf<ActionBarHelp>()
        for (childFragment in fragment.childFragmentManager.fragments) {
            if (childFragment == null || !childFragment.isVisible) {
                continue
            }
            if (childFragment is ActionBarHelp) {
                allFragments.add(childFragment)
            }
            allFragments.addAll(findAllActionBarChildFragments(childFragment))
        }
        return allFragments
    }
}
