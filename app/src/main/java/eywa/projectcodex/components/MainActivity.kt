package eywa.projectcodex.components

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.components.about.AboutFragment
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.components.commonUtils.SharedPrefs.Companion.getSharedPreferences
import eywa.projectcodex.components.commonUtils.ToastSpamPrevention

class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private var defaultRoundsVersion = -1

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val navHostFragment by lazy {
            supportFragmentManager.fragments.find { it is NavHostFragment }
                    ?: throw IllegalStateException("No NavHost found")
        }
        @Suppress("UNCHECKED_CAST")
        when (item.itemId) {
            R.id.action_bar__help -> {
                ActionBarHelp.executeHelpPressed(findAllActionBarChildFragments(navHostFragment), this)
            }
            R.id.action_bar__about -> {
                val aboutFragment =
                        navHostFragment.childFragmentManager.fragments.filterIsInstance<AboutFragment>().firstOrNull()
                if (aboutFragment != null && aboutFragment.isVisible) {
                    ToastSpamPrevention.displayToast(
                            applicationContext,
                            resources.getString(R.string.err__about_already_displayed)
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
