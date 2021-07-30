package eywa.projectcodex.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.commonUtils.UpdateDefaultRounds
import org.junit.Assert
import java.util.concurrent.CountDownLatch

class CustomConditionWaiter {
    companion object {
        const val DEFAULT_THREAD_SLEEP = 2000L

        /**
         * Wait for a particular fragment to appear on the screen
         */
        fun waitForFragmentToShow(scenario: ActivityScenario<MainActivity>, fragmentClassName: String) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    var found = false
                    scenario.onActivity {
                        val fragments = it.navHostFragment.childFragmentManager.fragments
                        for (fragment in fragments) {
                            if (fragment.javaClass.name == fragmentClassName) {
                                found = true
                            }
                        }
                    }
                    if (!found) {
                        // Don't clog up the main thread in the onActivity method, wait a moment before trying again
                        Thread.sleep(DEFAULT_THREAD_SLEEP)
                    }
                    return found
                }

                override fun getDescription(): String {
                    return "Wait for $fragmentClassName to appear"
                }
            })
        }

        /**
         * Wait for a particular table row to appear
         */
        fun waitForRowToAppear(tableView: TableView, rowIndex: Int) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    try {
                        return tableView.adapter!!.getCellRowItems(rowIndex) != null
                    }
                    catch (e: NullPointerException) {
                        println("Waiting for score pad entries to load")
                    }
                    return false
                }

                override fun getDescription(): String {
                    return "Waiting for row $rowIndex to load"
                }
            })
        }

        /**
         * Wait for a set amount of time (non blocking)
         */
        fun waitFor(milli: Long) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    Thread.sleep(milli)
                    return true
                }

                override fun getDescription(): String {
                    return "Wait for a given length of time"
                }
            })
        }

        /**
         * @see waitFor
         */
        fun waitFor(milli: Int) {
            waitFor(milli.toLong())
        }

        fun waitForUpdateRoundsTaskToFinish(scenario: ActivityScenario<MainActivity>) {
            waitForUpdateRoundsTaskToFinish(activityScenario = scenario)
        }

        fun waitForUpdateRoundsTaskToFinish(scenario: FragmentScenario<*>) {
            waitForUpdateRoundsTaskToFinish(fragmentScenario = scenario as FragmentScenario<Fragment>)
        }

        private fun waitForUpdateRoundsTaskToFinish(
                activityScenario: ActivityScenario<MainActivity>? = null,
                fragmentScenario: FragmentScenario<Fragment>? = null
        ) {
            check(activityScenario == null || fragmentScenario == null) { "Either activity or fragment scenario must be null" }
            check(activityScenario != null || fragmentScenario != null) { "Activity and fragment scenario cannot both be null" }
            val state = UpdateDefaultRounds.taskProgress.getState()
            val completeLatch = CountDownLatch(1)
            val observer = Observer { taskState: UpdateDefaultRounds.UpdateTaskState ->
                if (taskState.isCompletedState) {
                    completeLatch.countDown()
                }
            }
            activityScenario?.let { it.onActivity { state.observeForever(observer) } }
            fragmentScenario?.let { it.onFragment { state.observeForever(observer) } }
            if (!completeLatch.await(latchAwaitTimeSeconds, latchAwaitTimeUnit)) {
                Assert.fail("Update task did not finish")
            }
            activityScenario?.let { it.onActivity { state.removeObserver(observer) } }
            fragmentScenario?.let { it.onFragment { state.removeObserver(observer) } }
        }


    }
}