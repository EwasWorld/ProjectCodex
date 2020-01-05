package eywa.projectcodex

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.ui.MainActivity
import org.junit.Assert.assertEquals
import kotlinx.coroutines.GlobalScope
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewRoundInstrumentedTest {
    companion object {
        init {
            ScoresRoomDatabase.DATABASE_NAME = testDatabaseName
        }
    }

    @get:Rule
    val activity = ActivityTestRule(MainActivity::class.java)

    @Before
    fun beforeEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
        activity.activity.supportFragmentManager.beginTransaction()
    }

    @After
    fun afterEach() {
        activity.activity.applicationContext.deleteDatabase(testDatabaseName)
    }

    /**
     * Test row added to archer_round
     * Test id is correct
     */
    @Test
    fun addRoundButton() {
        var archerRounds: List<ArcherRound> = listOf()
        val db = ScoresRoomDatabase.getDatabase(activity.activity.applicationContext, GlobalScope)
        Handler(Looper.getMainLooper()).post {
            db.archerRoundDao().getAllArcherRounds().observe(activity.activity, Observer { obArcherRounds ->
                archerRounds = obArcherRounds
            })
        }

        R.id.button_start_new_round.click()
        R.id.button_create_round.click()

        assertEquals(1, archerRounds.size)
        assertEquals(1, archerRounds[0].archerRoundId)

        Espresso.pressBack()
        R.id.button_start_new_round.click()

        val before = archerRounds
        assertEquals(1, before.size)

        R.id.button_create_round.click()
        val after = archerRounds.toMutableList()
        assertEquals(2, after.size)

        after.removeAll(before)
        assertEquals(1, after.size)
        assert(after[0].archerRoundId > before.maxBy { round -> round.archerId }!!.archerRoundId)
    }
}