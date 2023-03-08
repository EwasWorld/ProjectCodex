package eywa.projectcodex.common

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.components.mainActivity.MainActivity
import org.hamcrest.Matcher
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

        fun waitForClassToAppear(clazz: Class<*>) {
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun checkCondition(): Boolean {
                    val failureHandler = CustomWaiterFailHandle("Waiting for class to appear")
                    onViewWithClassName(clazz)
                            .withFailureHandler(failureHandler)
                            .check(matches(isDisplayed()))
                    return !failureHandler.wasTriggered
                }

                override fun getDescription(): String {
                    return "Waiting for class to appear"
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
