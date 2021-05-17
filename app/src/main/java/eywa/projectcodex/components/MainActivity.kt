package eywa.projectcodex.components

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.ActionBarHelp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        @Suppress("UNCHECKED_CAST")
        return when (item.itemId) {
            R.id.action_bar__help -> {
                ActionBarHelp.executeHelpPressed(
                        findAllActionBarChildFragments(
                                supportFragmentManager.fragments.find { it is NavHostFragment }
                                        ?: throw IllegalStateException("No help info found")
                        ),
                        this
                )
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
