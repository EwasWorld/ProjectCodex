package eywa.projectcodex.common

import android.view.View
import android.widget.Toast
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import org.hamcrest.Matcher

class CustomConditionWaiter {
    companion object {
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
                var lastError: AssertionError? = null

                override fun getDescription(): String {
                    return listOfNotNull(
                            "Waiting compose condition",
                            description,
                            lastError?.message,
                    ).joinToString()
                }

                override fun checkCondition(): Boolean {
                    try {
                        assertion()
                        return true
                    }
                    catch (e: AssertionError) {
                        lastError = e
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
