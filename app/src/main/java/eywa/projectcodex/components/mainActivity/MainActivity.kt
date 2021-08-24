package eywa.projectcodex.components.mainActivity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.*
import eywa.projectcodex.common.utils.SharedPrefs.Companion.getSharedPreferences
import eywa.projectcodex.components.about.AboutFragment
import eywa.projectcodex.components.mainMenu.MainMenuFragment
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val LOG_TAG = "MainActivity"
    }

    lateinit var navHostFragment: NavHostFragment
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private var defaultRoundsVersion = -1

    /**
     * Stores destination IDs of fragments which will be returned to when the back button is pressed
     * This is a list rather than a stack due to the need to remove all duplicates of certain items at certain times
     */
    private val customBackStack = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Ensure default rounds are up to date
        val sharedPreferences = this.getSharedPreferences()
        if (defaultRoundsVersion < 0) {
            defaultRoundsVersion = sharedPreferences.getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
            mainActivityViewModel.updateDefaultRounds(resources, sharedPreferences)
        }

        navHostFragment =
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                        ?: throw IllegalStateException("No NavHost found")) as NavHostFragment
        val bottomNav = findViewById<BottomNavigationView>(R.id.menu_archer_rounds_bottom_nav)
        bottomNav.setupWithNavController(navHostFragment.navController)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            val roundInfo = findInstanceOf<ArcherRoundBottomNavigationInfo>(navHostFragment)
            check(roundInfo != null) { "No ArcherRoundBottomNavigation found" }

            if (item.itemId == R.id.inputEndFragment && roundInfo.isRoundComplete()) {
                ToastSpamPrevention.displayToast(
                        this,
                        resources.getString(R.string.err_archer_round_nav__round_completed)
                )
                return@setOnNavigationItemSelectedListener false
            }
            val args = Bundle()
            args.putInt("archerRoundId", roundInfo.getArcherRoundId())
            navHostFragment.navController.navigate(item.itemId, args)
            true
        }

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, arguments ->
            if (arguments?.getBoolean("showArcherRoundNavBar", false) == true) {
                bottomNav.visibility = View.VISIBLE
            }
            else {
                bottomNav.visibility = View.GONE
            }

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

            /*
             * Check it's not illegally moving to input end on a completed round
             */
            if (nextDestination == R.id.inputEndFragment) {
                findInstanceOf<ArcherRoundBottomNavigationInfo>(navHostFragment)?.let { roundInfo ->
                    if (roundInfo.isRoundComplete()) {
                        clearBackStackAndReturnToMainMenu()
                        return@addCallback
                    }
                }
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
        @Suppress("UNCHECKED_CAST")
        when (item.itemId) {
            R.id.action_bar__help -> {
                ActionBarHelp.executeHelpPressed(findAllActionBarChildFragments(navHostFragment), this)
            }
            R.id.action_bar__home -> {
                val aboutFragment =
                        navHostFragment.childFragmentManager.fragments.filterIsInstance<MainMenuFragment>()
                                .firstOrNull()
                if (aboutFragment != null && aboutFragment.isVisible) {
                    ToastSpamPrevention.displayToast(
                            applicationContext,
                            resources.getString(R.string.err_action_bar__home_already_displayed)
                    )
                }
                navHostFragment.findNavController().navigate(R.id.mainMenuFragment)
            }
            R.id.action_bar__about -> {
                val aboutFragment =
                        navHostFragment.childFragmentManager.fragments.filterIsInstance<AboutFragment>().firstOrNull()
                if (aboutFragment != null && aboutFragment.isVisible) {
                    ToastSpamPrevention.displayToast(
                            applicationContext,
                            resources.getString(R.string.err_action_bar__about_already_displayed)
                    )
                }
                navHostFragment.findNavController().navigate(R.id.aboutFragment)
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
