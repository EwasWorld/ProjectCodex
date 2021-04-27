package eywa.projectcodex.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import eywa.projectcodex.R
import eywa.projectcodex.ui.commonUtils.ActionBarHelp

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
        return when (item.itemId) {
            R.id.action_bar__help -> {
                val hostFragment = supportFragmentManager.fragments.find { it is NavHostFragment }
                        ?: throw IllegalStateException("No help info found")
                ActionBarHelp.executeHelpPressed(
                        hostFragment.childFragmentManager.fragments.filterIsInstance(ActionBarHelp::class.java),
                        this
                )
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
