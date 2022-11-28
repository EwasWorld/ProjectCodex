package eywa.projectcodex.common

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadFragment
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import kotlin.reflect.KClass

class CustomConditionWaiter {
    companion object {
        const val DEFAULT_THREAD_SLEEP = 2000L

        /**
         * Wait for a particular fragment to appear on the screen
         */
        fun waitForFragmentToShow(scenario: ActivityScenario<MainActivity>, fragment: KClass<out Fragment>) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    val isShowing = TestUtils.isFragmentShowing(scenario, fragment)
                    if (!isShowing) {
                        // Don't clog up the main thread in the onActivity method, wait a moment before trying again
                        Thread.sleep(DEFAULT_THREAD_SLEEP)
                    }
                    return isShowing
                }

                override fun getDescription(): String {
                    return "Wait for ${fragment.simpleName} to appear"
                }
            })
        }

        fun waitForScorePadToOpen(
                composeTestRule: ComposeTestRule<MainActivity>,
                arrows: Iterable<ArrowValue>,
                goldsType: GoldsType = GoldsType.NINES,
                rowIndex: Int
        ) {
            waitForScorePadToOpen(
                    composeTestRule,
                    "%d/%d/%d".format(
                            arrows.count { it.score != 0 },
                            arrows.sumOf { it.score },
                            arrows.count { goldsType.isGold(it) }),
                    rowIndex
            )
        }

        /**
         * Returns to the main menu, clicks view scores, then waits for the given hits/score/golds string to appear
         * and clicks on it to open the score pad
         * @param expectedHsg the hits/score/golds string to click (in the form 0/0/0 where 0s are positive integers)
         */
        fun waitForScorePadToOpen(composeTestRule: ComposeTestRule<MainActivity>, expectedHsg: String, rowIndex: Int) {
            R.id.action_bar__home.click()

            composeTestRule.mainMenuRobot {
                clickViewScores {
                    waitForHsg(rowIndex, expectedHsg)
                    clickRow(rowIndex)
                    waitForFragmentToShow(composeTestRule.activityRule.scenario, ScorePadFragment::class)
                }
            }
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
                    val failureHandler = CustomWaiterFailHandle("Waiting for view to appear")
                    onView(withId(viewId))
                            .withFailureHandler(failureHandler)
                            .check(matches(isDisplayed()))
                    return !failureHandler.wasTriggered
                }

                override fun getDescription(): String {
                    return "Waiting for view to appear"
                }
            })
        }

        enum class ClickType { NONE, CLICK, LONG_CLICK }

        fun waitForTextToAppear(text: String, clickType: ClickType = ClickType.NONE) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    val failureHandler = CustomWaiterFailHandle("Waiting for text '$text' to appear")
                    val interaction = onView(withIndex(withText(containsString(text)), 0))
                            .withFailureHandler(failureHandler)
                    when (clickType) {
                        ClickType.NONE -> interaction.check(matches(isDisplayed()))
                        ClickType.CLICK -> interaction.perform(click())
                        ClickType.LONG_CLICK -> interaction.perform(longClick())
                    }
                    return !failureHandler.wasTriggered
                }

                override fun getDescription(): String {
                    return "Waiting for text '$text' to appear"
                }
            })
        }

        fun waitForTextToAppear(text: String, viewId: Int, index: Int, clickText: Boolean = false) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    val failureHandler = CustomWaiterFailHandle("Waiting for text '$text' to appear")
                    val interaction = onView(allOf(withIndex(withId(viewId), index), withText(text)))
                            .withFailureHandler(failureHandler)
                    if (clickText) {
                        interaction.perform(click())
                    }
                    else {
                        interaction.check(matches(isDisplayed()))
                    }
                    return !failureHandler.wasTriggered
                }

                override fun getDescription(): String {
                    return "Waiting for text '$text' to appear"
                }
            })
        }

        fun waitForSpinnerTextToAppear(spinnerId: Int, text: String) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    val failureHandler = CustomWaiterFailHandle("Waiting for text '$text' to appear in spinner")
                    onView(CoreMatchers.allOf(withParent(withId(spinnerId)), withText(text)))
                            .withFailureHandler(failureHandler)
                            .check(matches(isDisplayed()))
                    return !failureHandler.wasTriggered
                }

                override fun getDescription(): String {
                    return "Waiting for text '$text' to appear in spinner"
                }
            })
        }

        /**
         * @param menuItemText the text of one of the menu items
         */
        fun waitForMenuToAppear(menuItemText: String) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    val failureHandler = CustomWaiterFailHandle("Waiting for a menu to appear")
                    onView(withText(menuItemText)).inRoot(RootMatchers.isPlatformPopup())
                            .withFailureHandler(failureHandler)
                            .check(matches(isDisplayed()))
                    return !failureHandler.wasTriggered
                }

                override fun getDescription(): String {
                    return "Waiting for a menu to appear"
                }
            })
        }

        /**
         * @param menuItemText the text of one of the menu items
         */
        fun waitForMenuItemAndPerform(menuItemText: String, action: ViewAction = click()) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    val failureHandler = CustomWaiterFailHandle("Waiting for a menu to appear")
                    onView(withText(menuItemText)).inRoot(RootMatchers.isPlatformPopup())
                            .withFailureHandler(failureHandler)
                            .perform(action)
                    return !failureHandler.wasTriggered
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
         * Wait for the duration of [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG]
         */
        fun waitForToastToDisappear(isShortToast: Boolean = true) {
            waitFor(if (isShortToast) 2000 else 3500)
        }

        /**
         * Wait toast with certain text to appear
         */
        fun waitForToast(text: String) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    val failureHandler = CustomWaiterFailHandle()
                    checkContainsToast(text, failureHandler)
                    return !failureHandler.wasTriggered
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

        fun waitForComposeCondition(description: String? = null, assertion: () -> Unit) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun getDescription(): String {
                    return description ?: "Waiting compose condition"
                }

                override fun checkCondition(): Boolean {
                    try {
                        assertion()
                        return true
                    }
                    catch (e: AssertionError) {
                        println(e)
                    }
                    return false
                }
            })
        }
    }

    /**
     * Espresso [FailureHandler] that logs a message and set [wasTriggered] to true on fail. Does not propagate the error.
     *
     * Class is private because if we're not checking in a loop/waiter, the error should propagate
     */
    private class CustomWaiterFailHandle(private val failMessage: String? = null) : FailureHandler {
        var wasTriggered = false
            private set

        override fun handle(error: Throwable?, viewMatcher: Matcher<View>?) {
            if (failMessage != null) {
                println(failMessage)
            }
            else {
                println("Custom failure handler triggered: ${error?.message}")
            }

            wasTriggered = true
        }
    }
}