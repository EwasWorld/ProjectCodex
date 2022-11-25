package eywa.projectcodex.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.database.LocalDatabaseDaggerModule

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
         * - Ensures [UpdateDefaultRounds] is complete
         * - Clears the database instance
         * - Resets shared preferences
         */
        private fun teardownScenarioWithoutClosing(scenario: ActivityScenario<MainActivity>) {
            /*
             * Wait for the update rounds task to complete so it doesn't interfere with other tests
             * It runs in a separate thread which will not be stopped on scenario close
             */
            CustomConditionWaiter.waitForUpdateRoundsTaskToFinish(scenario)

            /*
             * General cleanup
             */
            setSharedPrefs(scenario)

            /*
             * Destroy the fragment before clearing the database
             *      else the UI can react to the empty database and fail
             */
            scenario.moveToState(Lifecycle.State.DESTROYED)
            LocalDatabaseDaggerModule.teardown()
        }

        @Suppress("UNCHECKED_CAST")
        fun teardownScenario(scenario: FragmentScenario<*>) {
            val fragmentScenario = scenario as FragmentScenario<Fragment>

            /*
             * Destroy the fragment before clearing the database
             *      else the UI can react to the empty database and fail
             */
            fragmentScenario.moveToState(Lifecycle.State.DESTROYED)
            LocalDatabaseDaggerModule.teardown()
        }
    }
}