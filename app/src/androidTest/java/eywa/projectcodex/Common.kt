package eywa.projectcodex

import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.evrencoskun.tableview.TableView
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.components.commonUtils.SharedPrefs.Companion.getSharedPreferences
import eywa.projectcodex.components.commonUtils.UpdateDefaultRounds
import eywa.projectcodex.database.ScoresRoomDatabase
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass


/**
 * Increases latch wait times when debugging so that it doesn't time out while debugging
 */
val latchAwaitTimeSeconds = if (Debug.isDebuggerConnected()) 60L * 60 else 10L
val latchAwaitTimeUnit = TimeUnit.SECONDS

const val testDatabaseName = "test_database"
const val testSharedPrefsName = "test_prefs"

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
    val viewId = this
    ConditionWatcher.waitForCondition(object : Instruction() {
        override fun getDescription(): String {
            return "Wait for the round spinner to be visible"
        }

        override fun checkCondition(): Boolean {
            try {
                Espresso.onView(ViewMatchers.withId(viewId)).perform(ViewActions.click())
                Espresso.onData(Matchers.hasToString(text)).perform(ViewActions.click())
                return true
            }
            catch (e: PerformException) {
            }
            return false
        }
    })
}

fun onViewWithClassName(text: String): ViewInteraction {
    return Espresso.onView(ViewMatchers.withText(text))!!
}

fun <T> onViewWithClassName(clazz: Class<T>): ViewInteraction {
    return Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(clazz.name)))!!
}

fun checkContainsToast(message: String) =
        Espresso.onView(ViewMatchers.withText(message))
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
fun ActivityScenario<MainActivity>.waitForFragmentInstruction(fragmentClassName: String): Instruction {
    return object : Instruction() {
        override fun checkCondition(): Boolean {
            var found = false
            onActivity {
                val fragments = it.navHostFragment.childFragmentManager.fragments
                for (fragment in fragments) {
                    if (fragment.javaClass.name == fragmentClassName) {
                        found = true
                    }
                }
            }
            if (!found) {
                // Don't clog up the main thread in the onActivity method, wait a moment before trying again
                Thread.sleep(2000)
            }
            return found
        }

        override fun getDescription(): String {
            return "Wait for $fragmentClassName to appear"
        }
    }
}

fun waitForOpenScorePadFromMainMenu(uniqueScoreToClick: Int): Instruction {
    return object : Instruction() {
        override fun getDescription(): String {
            return "Wait for data to appear in view rounds table so score pad can be opened"
        }

        override fun checkCondition(): Boolean {
            return try {
                R.id.button_main_menu__view_rounds.click()
                Espresso.onView(ViewMatchers.withId((R.id.table_view_view_rounds))).perform(ViewActions.swipeLeft())
                Espresso.onView(ViewMatchers.withText(uniqueScoreToClick.toString())).perform(ViewActions.click())
                true
            }
            catch (e: NoMatchingViewException) {
                Espresso.pressBack()
                false
            }
        }
    }
}

/**
 * Wait for a particular table row to appear
 */
fun TableView.waitForRowToAppear(rowIndex: Int): Instruction {
    return object : Instruction() {
        override fun checkCondition(): Boolean {
            try {
                return adapter!!.getCellRowItems(rowIndex) != null
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

fun setSharedPrefs(scenario: ActivityScenario<MainActivity>, value: Int = -1) {
    scenario.onActivity { activity ->
        val prefs = activity.getSharedPreferences().edit()
        prefs.putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, value)
        prefs.apply()
        ScoresRoomDatabase.clearInstance(activity)
    }
}

fun waitForRoundUpdateTaskToFinishInstruction(): Instruction {
    return object : Instruction() {
        override fun getDescription(): String {
            return "Wait for update to finish"
        }

        override fun checkCondition(): Boolean {
            var currentValue: UpdateDefaultRounds.UpdateTaskState? = null
            val latch = CountDownLatch(1)
            Handler(Looper.getMainLooper()).post {
                currentValue = UpdateDefaultRounds.taskProgress.getState().retrieveValue()!!
                latch.countDown()
            }
            if (!latch.await(latchAwaitTimeSeconds, latchAwaitTimeUnit)) {
                Assert.fail("Failed to retrieve state")
            }
            return currentValue == UpdateDefaultRounds.UpdateTaskState.UP_TO_DATE
                    || currentValue == UpdateDefaultRounds.UpdateTaskState.COMPLETE
        }
    }
}
