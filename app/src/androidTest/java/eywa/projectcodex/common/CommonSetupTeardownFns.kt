package eywa.projectcodex.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.hiltModules.LocalDatastoreModule

class CommonSetupTeardownFns {
    companion object {
        /**
         * Tears down the scenario ([ActivityScenarioRule] itself will close the scenario)
         * @see teardownScenarioWithoutClosing
         */
        fun teardownScenario(rule: ActivityScenarioRule<MainActivity>) {
            teardownScenarioWithoutClosing(rule.scenario)
        }

        /**
         * Tears down and closes the scenario
         * @see teardownScenarioWithoutClosing
         */
        fun teardownScenario(scenario: ActivityScenario<MainActivity>) {
            teardownScenarioWithoutClosing(scenario)
            scenario.close()
        }

        /**
         * - Clears the database instance
         * - Resets shared preferences
         */
        private fun teardownScenarioWithoutClosing(scenario: ActivityScenario<MainActivity>) {
            /*
             * General cleanup
             */
            setSharedPrefs(scenario)

            /*
             * Destroy the fragment before clearing the database
             *      else the UI can react to the empty database and fail
             */
            scenario.moveToState(Lifecycle.State.DESTROYED)
            LocalDatabaseModule.teardown()
            LocalDatastoreModule.teardown()
        }

        @Suppress("UNCHECKED_CAST")
        fun teardownScenario(scenario: FragmentScenario<*>) {
            val fragmentScenario = scenario as FragmentScenario<Fragment>

            /*
             * Destroy the fragment before clearing the database
             *      else the UI can react to the empty database and fail
             */
            fragmentScenario.moveToState(Lifecycle.State.DESTROYED)
            LocalDatabaseModule.teardown()
            LocalDatastoreModule.teardown()
        }
    }
}
