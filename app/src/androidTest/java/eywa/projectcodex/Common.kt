package eywa.projectcodex

import android.view.View
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import eywa.projectcodex.ui.MainActivity
import kotlinx.android.synthetic.main.content_main.*
import org.hamcrest.*
import org.hamcrest.CoreMatchers.containsString
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


const val testDatabaseName = "test_database"

fun Int.click() = Espresso.onView(ViewMatchers.withId(this)).perform(ViewActions.click())!!
fun Int.write(text: String) = Espresso.onView(ViewMatchers.withId(this)).perform(ViewActions.typeText(text))!!
fun Int.textEquals(text: String) = Espresso.onView(ViewMatchers.withId(this)).check(
        ViewAssertions.matches(ViewMatchers.withText(text))
)!!

fun Int.textContains(text: String) = Espresso.onView(ViewMatchers.withId(this)).check(
        ViewAssertions.matches(ViewMatchers.withText(containsString(text)))
)!!

fun Int.visibilityIs(visibility: ViewMatchers.Visibility) = Espresso.onView(ViewMatchers.withId(this)).check(
        ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(visibility))
)!!

fun Int.clickSpinnerItem(text: String) = this.run {
    Espresso.onView(ViewMatchers.withId(this)).perform(ViewActions.click())
    Espresso.onData(Matchers.hasToString(text)).perform(ViewActions.click())
}!!

fun onView(text: String): ViewInteraction {
    return Espresso.onView(ViewMatchers.withText(text))!!
}

fun <T> onView(clazz: Class<T>): ViewInteraction {
    return Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(clazz.name)))!!
}

infix fun ActivityTestRule<MainActivity>.containsToast(message: String) =
        Espresso.onView(ViewMatchers.withText(message))
                .inRoot(RootMatchers.withDecorView(CoreMatchers.not(activity.window.decorView)))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))!!

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
 * Wait for a particular fragment to appear on the screen
 */
fun ActivityTestRule<MainActivity>.waitForFragmentInstruction(fragmentClassName: String): Instruction {
    return object : Instruction() {
        override fun checkCondition(): Boolean {
            val fragments = activity.nav_host_fragment.childFragmentManager.fragments
            for (fragment in fragments) {
                if (fragment.javaClass.name == fragmentClassName) {
                    return true
                }
            }
            return false
        }

        override fun getDescription(): String {
            return "Wait for $fragmentClassName to appear"
        }
    }
}

/**
 * Wait for a particular table row to appear
 */
fun AbstractTableAdapter<*, *, *>.waitForRowToAppear(rowIndex: Int): Instruction {
    return object : Instruction() {
        override fun checkCondition(): Boolean {
            try {
                return getCellRowItems(rowIndex) != null
            }
            catch (e: NullPointerException) {
                println("Waiting for score pad entries to load")
            }
            return false
        }

        override fun getDescription(): String {
            return "Waiting for row $rowIndex to load"
        }
    }
}

/**
 * Wait for a set amount of time (non blocking)
 */
fun waitFor(milli: Long): Instruction {
    return object : Instruction() {
        override fun checkCondition(): Boolean {
            Thread.sleep(milli)
            return true
        }

        override fun getDescription(): String {
            return "Wait for a given length of time"
        }
    }
}

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

fun setNumberPickerValue(value: Int): ViewAction? {
    return object : ViewAction {
        override fun perform(uiController: UiController?, view: View) {
            check(view is NumberPicker) { "View must be a number picker to use this" }
            view.value = value
        }

        override fun getDescription(): String {
            return "Set the NumberPicker value"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(NumberPicker::class.java)
        }
    }
}

fun setDatePickerValue(value: Calendar): ViewAction? {
    return object : ViewAction {
        override fun perform(uiController: UiController?, view: View) {
            check(view is DatePicker) { "View must be a date picker to use this" }
            view.updateDate(value.get(Calendar.YEAR), value.get(Calendar.MONTH), value.get(Calendar.DAY_OF_MONTH))
        }

        override fun getDescription(): String {
            return "Set the DatePicker value"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(DatePicker::class.java)
        }
    }
}

fun setTimePickerValue(hours: Int, minutes: Int): ViewAction? {
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
            return ViewMatchers.isAssignableFrom(TimePicker::class.java)
        }
    }
}
