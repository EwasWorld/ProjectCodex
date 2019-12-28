package eywa.projectcodex

import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers

const val testDatabaseName = "test_database"

fun Int.click() = Espresso.onView(ViewMatchers.withId(this)).perform(ViewActions.click())
fun Int.write(text: String) = Espresso.onView(ViewMatchers.withId(this)).perform(ViewActions.typeText(text))
fun Int.textEquals(text: String) = Espresso.onView(ViewMatchers.withId(this)).check(
        ViewAssertions.matches(ViewMatchers.withText(text))
)

infix fun ActivityTestRule<MainActivity>.containsToast(message: String) =
    Espresso.onView(ViewMatchers.withText(message))
            .inRoot(RootMatchers.withDecorView(CoreMatchers.not(activity.window.decorView)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

fun AppCompatActivity.getString(name: String): String {
    return resources.getString(resources.getIdentifier(name, "string", packageName))
}

fun AppCompatActivity.idFromString(name: String): Int {
    return resources.getIdentifier(name, "id", packageName)
}