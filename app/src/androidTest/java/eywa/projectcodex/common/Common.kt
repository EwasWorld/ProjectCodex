@file:Suppress("unused")

package eywa.projectcodex.common

import android.app.Activity
import android.os.Debug
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.common.utils.SharedPrefs.Companion.getSharedPreferences
import eywa.projectcodex.components.mainActivity.MainActivity
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass


/**
 * Increases latch wait times when debugging so that it doesn't time out while debugging
 */
val latchAwaitTimeSeconds = if (Debug.isDebuggerConnected()) 60L * 60 else 10L
val latchAwaitTimeUnit = TimeUnit.SECONDS

fun logMessage(logClass: KClass<*>, message: String) {
    Log.i("ProjCodexTest" + logClass.simpleName, message)
}

fun Int.click() = onView(withId(this)).perform(ViewActions.click())!!
fun Int.write(text: String) = onView(withId(this)).perform(ViewActions.typeText(text))!!
fun Int.scrollTo() = onView(withId(this)).perform(ViewActions.scrollTo())!!
fun Int.clearText() = onView(withId(this)).perform(ViewActions.clearText())!!
fun Int.textEquals(text: String) = onView(withId(this)).check(matches(withText(text)))!!

fun Int.spinnerTextEquals(text: String) =
        onView(allOf(withParent(withId(this)), withText(text))).check(matches(isDisplayed()))!!


fun Int.textContains(text: String) =
        onView(withId(this)).check(matches(withText(containsString(text))))!!

fun Int.visibilityIs(visibility: Visibility) =
        onView(withId(this)).check(matches(withEffectiveVisibility(visibility)))!!

fun Int.clickSpinnerItem(text: String) = this.run {
    val viewId = this
    ConditionWatcher.waitForCondition(object : Instruction() {
        override fun getDescription(): String {
            return "Wait for the designated spinner to be visible"
        }

        override fun checkCondition(): Boolean {
            try {
                onView(withId(viewId)).perform(ViewActions.click())
                Espresso.onData(Matchers.hasToString(text)).perform(ViewActions.click())
                return true
            }
            catch (e: PerformException) {
            }
            return false
        }
    })
}

fun <T> onViewWithClassName(clazz: Class<T>) = onView(withClassName(Matchers.equalTo(clazz.name)))!!

/**
 * If the matcher matches multiple elements, get the element with the specified index
 */
fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        var currentIndex = 0

        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        override fun matchesSafely(view: View): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}

fun checkContainsToast(message: String, failureHandler: FailureHandler? = null): ViewInteraction {
    val interaction = onView(withText(message))
            .inRoot(object : TypeSafeMatcher<Root>() {
                override fun matchesSafely(item: Root?): Boolean {
                    val type = item?.windowLayoutParams?.orNull()?.type ?: return false
                    // Deprecation advises using TYPE_APPLICATION_OVERLAY instead, but this causes the test to hang
                    return type == WindowManager.LayoutParams.TYPE_TOAST
                            && item.decorView.windowToken == item.decorView.applicationWindowToken
                }

                override fun describeTo(description: Description?) {
                    description?.appendText("toast with text")
                }
            })
    if (failureHandler != null) {
        interaction.withFailureHandler(failureHandler)
    }
    interaction.check(matches(isDisplayed()))!!
    return interaction
}

fun AppCompatActivity.getString(name: String): String {
    return getString(resources.getIdentifier(name, "string", packageName))
}

fun AppCompatActivity.idFromString(name: String): Int {
    return resources.getIdentifier(name, "id", packageName)
}

fun <T> LiveData<T>.retrieveValue(): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val observer = Observer<T> { t ->
        value = t
        latch.countDown()
    }
    observeForever(observer)
    latch.await(2, TimeUnit.SECONDS)
    return value
}

/**
 * Fills the end by pressing one of the score buttons then pressing complete
 * @param scoreButtonId the score button to press to fill up the end
 * @param submitButtonId the button to press to submit the end
 */
fun completeEnd(
        scoreButtonId: Int,
        activityScenario: ActivityScenario<MainActivity>? = null,
        fragmentScenario: FragmentScenario<*>? = null,
        submitButtonId: Int
) {
    require(activityScenario == null || fragmentScenario == null) { "Cannot define a fragment and activity scenario" }
    require(activityScenario != null || fragmentScenario != null) { "Must define a fragment or activity scenario" }

    var contains = true
    fun Activity.containsPlaceholder() = false
//            this.findViewById<TextView>(R.id.text_end_inputs__inputted_arrows).text.contains('.')
    while (contains) {
        activityScenario?.let { scenario ->
            scenario.onActivity { contains = it.containsPlaceholder() }
        }
        fragmentScenario?.let { scenario ->
            @Suppress("UNCHECKED_CAST")
            (scenario as FragmentScenario<Fragment>).onFragment {
                contains = it.requireActivity().containsPlaceholder()
            }
        }
        if (contains) {
            scoreButtonId.click()
        }
    }
    submitButtonId.click()
}

fun setNumberPickerValue(value: Int): ViewAction {
    return object : ViewAction {
        override fun perform(uiController: UiController?, view: View) {
            check(view is NumberPicker) { "View must be a number picker to use this" }
            view.value = value
        }

        override fun getDescription(): String {
            return "Set the NumberPicker value"
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(NumberPicker::class.java)
        }
    }
}

fun setDatePickerValue(value: Calendar): ViewAction {
    return object : ViewAction {
        override fun perform(uiController: UiController?, view: View) {
            check(view is DatePicker) { "View must be a date picker to use this" }
            view.updateDate(value.get(Calendar.YEAR), value.get(Calendar.MONTH), value.get(Calendar.DAY_OF_MONTH))
        }

        override fun getDescription(): String {
            return "Set the DatePicker value"
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(DatePicker::class.java)
        }
    }
}

fun setTimePickerValue(calendar: Calendar): ViewAction {
    return setTimePickerValue(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
}

fun setTimePickerValue(hours: Int, minutes: Int): ViewAction {
    return object : ViewAction {
        override fun perform(uiController: UiController?, view: View) {
            check(view is TimePicker) { "View must be a time picker to use this" }
            view.currentHour = hours
            view.currentMinute = minutes
        }

        override fun getDescription(): String {
            return "Set the TimePicker value"
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(TimePicker::class.java)
        }
    }
}

/**
 * Wait for an alert dialog to appear then click the specified button
 */
fun clickAlertDialog(alertDialogText: String, buttonText: String = "OK") {
    CustomConditionWaiter.waitFor(200)
    ConditionWatcher.waitForCondition(object : Instruction() {
        override fun getDescription(): String {
            return "Waiting for alert dialog to appear"
        }

        override fun checkCondition(): Boolean {
            try {
                onView(withText(alertDialogText)).check(matches(isDisplayed()))
                return true
            }
            catch (e: NoMatchingViewException) {
            }
            return false
        }
    })
    onView(withText(buttonText)).perform(ViewActions.click())
}

fun setSharedPrefs(scenario: ActivityScenario<MainActivity>, value: Int = -1) {
    scenario.onActivity { activity ->
        val prefs = activity.getSharedPreferences().edit()
        prefs.putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, value)
        prefs.apply()
    }
}
