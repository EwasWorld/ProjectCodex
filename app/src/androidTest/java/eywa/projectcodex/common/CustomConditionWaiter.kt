package eywa.projectcodex.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.mainMenu.MainMenuFragment
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.database.arrowValue.ArrowValue
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

        fun waitForScorePadToOpen(
                scenario: ActivityScenario<MainActivity>,
                arrows: Iterable<ArrowValue>,
                goldsType: GoldsType = GoldsType.NINES
        ) {
            waitForScorePadToOpen(
                    scenario,
                    "%d/%d/%d".format(
                            arrows.count { it.score != 0 },
                            arrows.sumOf { it.score },
                            arrows.count { goldsType.isGold(it) })
            )
        }

        /**
         * Returns to the main menu, clicks view scores, then waits for the given hits/score/golds string to appear
         * and clicks on it to open the score pad
         * @param hsgToClick the hits/score/golds string to click (in the form 0/0/0 where 0s are positive integers)
         */
        fun waitForScorePadToOpen(scenario: ActivityScenario<MainActivity>, hsgToClick: String) {
            R.id.action_bar__home.click()
            waitForFragmentToShow(scenario, MainMenuFragment::class.java.name)
            R.id.button_main_menu__view_scores.click()
            waitForFragmentToShow(scenario, ViewScoresFragment::class.java.name)

            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    var hsgClicked = false
                    try {
                        onView(withText(hsgToClick)).perform(ViewActions.click())
                        hsgClicked = true
                    }
                    catch (e: NoMatchingViewException) {
                    }
                    if (!hsgClicked) {
                        // Don't clog up the main thread in the onActivity method, wait a moment before trying again
                        Thread.sleep(DEFAULT_THREAD_SLEEP)
                    }
                    return hsgClicked
                }

                override fun getDescription(): String {
                    return "Wait for $hsgToClick to appear"
                }
            })
            waitForFragmentToShow(scenario, ScorePadFragment::class.java.name)
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

        fun waitForViewToAppear(viewId: Int) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    try {
                        onView(withId(viewId)).check(matches(isDisplayed()))
                        return true
                    }
                    catch (e: NoMatchingViewException) {
                        println("Waiting for view to appear")
                    }
                    return false
                }

                override fun getDescription(): String {
                    return "Waiting for view to appear"
                }
            })
        }

        fun waitForTextToAppear(text: String) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    try {
                        onView(withIndex(withText(text), 0)).check(matches(isDisplayed()))
                        return true
                    }
                    catch (e: NoMatchingViewException) {
                        println("Waiting for text '$text' to appear")
                    }
                    return false
                }

                override fun getDescription(): String {
                    return "Waiting for text '$text' to appear"
                }
            })
        }

        /**
         * @param menuItemText the text of one of the menu items
         */
        fun waitForMenuToAppear(menuItemText: String) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    try {
                        onView(withText(menuItemText)).inRoot(RootMatchers.isPlatformPopup())
                                .check(matches(isDisplayed()))
                        return true
                    }
                    catch (e: NoMatchingViewException) {
                        println("Waiting for a menu to appear")
                    }
                    return false
                }

                override fun getDescription(): String {
                    return "Waiting for a menu to appear"
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
         * Wait toast with certain text to appear
         */
        fun waitForToast(text: String) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    try {
                        checkContainsToast(text)
                        return true
                    }
                    catch (e: NoMatchingViewException) {
                    }
                    return false
                }

                override fun getDescription(): String {
                    return "Waiting for toast with text '$text' to appear"
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