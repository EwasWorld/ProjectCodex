package eywa.projectcodex

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.ui.MainActivity
import kotlinx.android.synthetic.main.content_main.*
import org.hamcrest.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


const val testDatabaseName = "test_database"

fun Int.click() = Espresso.onView(ViewMatchers.withId(this)).perform(ViewActions.click())!!
fun Int.write(text: String) = Espresso.onView(ViewMatchers.withId(this)).perform(ViewActions.typeText(text))!!
fun Int.textEquals(text: String) = Espresso.onView(ViewMatchers.withId(this)).check(
        ViewAssertions.matches(ViewMatchers.withText(text))
)!!

fun Int.clickSpinnerItem(text: String) = this.run {
    Espresso.onView(ViewMatchers.withId(this)).perform(ViewActions.click())
    Espresso.onData(Matchers.hasToString(text)).perform(ViewActions.click())
}!!

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