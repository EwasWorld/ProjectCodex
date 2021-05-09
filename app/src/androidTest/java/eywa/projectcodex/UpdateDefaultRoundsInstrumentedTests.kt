package eywa.projectcodex

import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.repositories.RoundsRepo
import eywa.projectcodex.ui.MainMenuFragment
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class UpdateDefaultRoundsInstrumentedTests {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }

        private lateinit var scenario: FragmentScenario<MainMenuFragment>
    }


    @After
    fun afterEach() {
        scenario.onFragment {
            it.context?.let { context -> ScoresRoomDatabase.clearInstance(context) }
        }
    }

    /**
     * Test that the default rounds can import successfully and that buttons are shown/hidden as desired
     */
    @Test
    fun testDefaultRounds() {
        scenario = launchFragmentInContainer<MainMenuFragment>()

        R.id.button_main_menu__update_default_rounds.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.label_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.button_main_menu__update_default_rounds_cancel.visibilityIs(ViewMatchers.Visibility.GONE)

        R.id.button_main_menu__update_default_rounds.click()

        R.id.button_main_menu__update_default_rounds.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.text_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.label_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.button_main_menu__update_default_rounds_cancel.visibilityIs(ViewMatchers.Visibility.VISIBLE)

        ConditionWatcher.waitForCondition(WaitForMessageInstruction("complete"))

        R.id.button_main_menu__update_default_rounds.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.label_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.button_main_menu__update_default_rounds_cancel.visibilityIs(ViewMatchers.Visibility.GONE)
    }

    /**
     * Test that the default rounds process can be cancelled
     */
    @Test
    fun testDefaultRoundsCancel() {
        scenario = launchFragmentInContainer<MainMenuFragment>()
        R.id.button_main_menu__update_default_rounds.click()
        // Give it a second to change the buttons
        Thread.sleep(500)

        R.id.button_main_menu__update_default_rounds_cancel.click()
        ConditionWatcher.waitForCondition(WaitForMessageInstruction("Cancelled"))

        R.id.button_main_menu__update_default_rounds_cancel.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.button_main_menu__update_default_rounds.visibilityIs(ViewMatchers.Visibility.VISIBLE)
    }

    /**
     * Test that when navigating away from the fragment, the process continues in the background and displays the
     * correct values
     */
    @Test
    fun testFragmentStateTransitions() {
        scenario = launchFragmentInContainer<MainMenuFragment>()
        R.id.button_main_menu__update_default_rounds.click()

        // Returning to the fragment while still in progress should display status and cancel button
        scenario.recreate()
        R.id.button_main_menu__update_default_rounds.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.text_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.label_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.button_main_menu__update_default_rounds_cancel.visibilityIs(ViewMatchers.Visibility.VISIBLE)

        ConditionWatcher.waitForCondition(WaitForMessageInstruction("complete"))

        // Destroying the fragment after completion should reset to just the start button
        scenario.recreate()
        R.id.button_main_menu__update_default_rounds.visibilityIs(ViewMatchers.Visibility.VISIBLE)
        R.id.text_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.label_main_menu__update_default_rounds_progress.visibilityIs(ViewMatchers.Visibility.GONE)
        R.id.button_main_menu__update_default_rounds_cancel.visibilityIs(ViewMatchers.Visibility.GONE)
    }

    /**
     * Test that an error is shown when the database is locked
     */
    @Test
    fun testDatabaseIsLocked() {
        scenario = launchFragmentInContainer<MainMenuFragment>()
        val lockHolderThread = object : Runnable {
            var isRunning = true
            var complete = false

            override fun run() {
                Assert.assertEquals(0, RoundsRepo.repositoryWriteLock.holdCount)
                Assert.assertTrue(RoundsRepo.repositoryWriteLock.tryLock(1, TimeUnit.SECONDS))
                Assert.assertEquals(1, RoundsRepo.repositoryWriteLock.holdCount)

                ConditionWatcher.waitForCondition(object : Instruction() {
                    override fun getDescription(): String {
                        return "Waiting for thread to be stopped"
                    }

                    override fun checkCondition(): Boolean {
                        return !isRunning
                    }
                })

                Assert.assertEquals(1, RoundsRepo.repositoryWriteLock.holdCount)
                RoundsRepo.repositoryWriteLock.unlock()
                Assert.assertEquals(0, RoundsRepo.repositoryWriteLock.holdCount)
                complete = true
            }
        }
        Thread(lockHolderThread).start()
        R.id.button_main_menu__update_default_rounds.click()
        ConditionWatcher.waitForCondition(WaitForMessageInstruction("database"))
        lockHolderThread.isRunning = false

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "Waiting for thread to complete"
            }

            override fun checkCondition(): Boolean {
                return lockHolderThread.complete
            }
        })
    }

    /**
     * Waits for main menu to show a message containing [message]. Waits for 1 second if message is not spotted to
     *   prevent clogging up the main thread
     */
    private class WaitForMessageInstruction(private val message: String) : Instruction() {
        override fun checkCondition(): Boolean {
            var result = false
            scenario.onFragment {
                result = it.activity!!.findViewById<TextView>(R.id.text_main_menu__update_default_rounds_progress).text
                        .contains(message, ignoreCase = true)
            }
            if (!result) {
                Thread.sleep(1000)
            }
            return result
        }

        override fun getDescription(): String {
            return "Wait for a message to appear in the status of update task"
        }
    }
}