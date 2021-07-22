package eywa.projectcodex

import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.components.commonUtils.UpdateDefaultRounds
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.RoundRepo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class UpdateDefaultRoundsInstrumentedTests {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = CommonStrings.testDatabaseName
            SharedPrefs.sharedPreferencesCustomName = CommonStrings.testSharedPrefsName
        }

    }

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        RoundRepo.reCreateLock()
    }

    @After
    fun afterEach() {
        /*
         * Check that the current update rounds task is completed so it doesn't interfere with other tests
         * It runs in a separate thread which will not be stopped on scenario close
         */
        val state = UpdateDefaultRounds.taskProgress.getState()
        val completeLatch = CountDownLatch(1)
        val observer = Observer { taskState: UpdateDefaultRounds.UpdateTaskState ->
            if (taskState.isCompletedState) {
                completeLatch.countDown()
            }
        }
        scenario.onActivity {
            state.observeForever(observer)
        }
        if (!completeLatch.await(latchAwaitTimeSeconds, latchAwaitTimeUnit)) {
            Assert.fail("Update task did not finish")
        }
        scenario.onActivity {
            state.removeObserver(observer)
        }

        /*
         * Normal cleanup
         */
        scenario.onActivity {
            ScoresRoomDatabase.clearInstance(it.applicationContext)
        }
        setSharedPrefs(scenario)
        scenario.close()
    }

    /**
     * Test that an UpdateDefaultRounds task is started when the activity is started
     */
    @Test
    fun testUpdateIsCalledOnActivityStart() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        val state = UpdateDefaultRounds.taskProgress.getState()
        val latch = CountDownLatch(1)
        val observer = Observer { taskState: UpdateDefaultRounds.UpdateTaskState ->
            if (taskState != UpdateDefaultRounds.UpdateTaskState.NOT_STARTED) {
                latch.countDown()
            }
        }
        scenario.onActivity {
            state.observeForever(observer)
        }
        if (!latch.await(latchAwaitTimeSeconds, latchAwaitTimeUnit)) {
            Assert.fail("Did not move out of not_started state")
        }
        scenario.onActivity {
            state.removeObserver(observer)
        }
    }

    /**
     * Test that an error is shown when the database is locked
     */
    @Test
    fun testDatabaseIsLocked() {
        /*
         * Set up thread to hold lock
         */
        val lockHolderThread = object : Runnable {
            var isRunning = true
            var isComplete = false

            override fun run() {
                Assert.assertEquals(0, RoundRepo.repositoryWriteLock.holdCount)
                Assert.assertTrue(RoundRepo.repositoryWriteLock.tryLock(1, TimeUnit.SECONDS))
                Assert.assertEquals(1, RoundRepo.repositoryWriteLock.holdCount)

                ConditionWatcher.waitForCondition(object : Instruction() {
                    override fun getDescription(): String {
                        return "Waiting for thread to be stopped"
                    }

                    override fun checkCondition(): Boolean {
                        return !isRunning
                    }
                })

                Assert.assertEquals(1, RoundRepo.repositoryWriteLock.holdCount)
                RoundRepo.repositoryWriteLock.unlock()
                Assert.assertEquals(0, RoundRepo.repositoryWriteLock.holdCount)
                isComplete = true
            }
        }
        Thread(lockHolderThread).start()

        /*
         * Set up observer to wait for desired message
         */
        val state = UpdateDefaultRounds.taskProgress.getMessage()
        val latch = CountDownLatch(1)
        val observer = Observer { message: String? ->
            if (message != null && message.contains("database", ignoreCase = true)) {
                latch.countDown()
            }
        }

        /*
         * Start activity and wait for message
         */
        ActivityScenarioRule(MainActivity::class.java)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        R.id.action_bar__about.click()
        scenario.onActivity {
            state.observeForever(observer)
        }
        if (!latch.await(latchAwaitTimeSeconds, latchAwaitTimeUnit)) {
            Assert.fail("Did not show error message about database")
        }
        scenario.onActivity {
            state.removeObserver(observer)
        }

        /*
         * Clean-up
         */
        lockHolderThread.isRunning = false
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "Waiting for thread to complete"
            }

            override fun checkCondition(): Boolean {
                return lockHolderThread.isComplete
            }
        })
    }
}