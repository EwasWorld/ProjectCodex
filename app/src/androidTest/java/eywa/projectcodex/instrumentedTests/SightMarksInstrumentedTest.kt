package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.instrumentedTests.robots.MainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.SightMarksRobot
import eywa.projectcodex.model.SightMark
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.*

@HiltAndroidTest
class SightMarksInstrumentedTest {
    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var db: ScoresRoomDatabase

    @Before
    fun beforeEach() {
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario

        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!
        }
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(composeTestRule.activityRule)
    }

    @Test
    fun testAddAndDiagram() {
        MainMenuRobot(composeTestRule).run {
            clickSightMarks()

        }.run {
            checkEmptyMessage()

            // Add one
            clickAdd()

        }.run {
            setInfo(sightMarks[0])
            clickSave()

        }.run {
            checkSightMarkDisplayed(sightMarks[0])
            checkDiagramTickLabelRange("1", "4")

            // Add second
            clickAdd()

        }.run {
            setInfo(sightMarks[1])
            clickSave()

        }.run {
            checkSightMarkDisplayed(sightMarks[0], true)
            checkSightMarkDisplayed(sightMarks[1])
            checkDiagramTickLabelRange("2", "5")

            // Flip
            flipDiagram()
            checkSightMarkDisplayed(sightMarks[0], true)
            checkSightMarkDisplayed(sightMarks[1])
            checkDiagramTickLabelRange("5", "2")

            // Archive
            archiveAll()
            checkSightMarkDisplayed(sightMarks[0].copy(isArchived = true), true)
            checkSightMarkDisplayed(sightMarks[1].copy(isArchived = true))
            checkDiagramTickLabelRange("5", "2")
        }
    }

    @Test
    fun testDetail() {
        MainMenuRobot(composeTestRule).run {
            clickSightMarks()

        }.run {
            checkEmptyMessage()

            // Add new
            clickAdd()

        }.run {
            setInfo(sightMarks[0])
            clickSave()

        }.run {
            checkSightMarkDisplayed(sightMarks[0])

            // Reset & Edit
            clickSightMark(sightMarks[0])

        }.run {
            setInfo(sightMarks[1])
            clickReset()
            checkInfo(sightMarks[0], false)

            setInfo(sightMarks[1])
            clickSave()

        }.run {
            checkSightMarkDisplayed(sightMarks[1])

            // Delete
            clickSightMark(sightMarks[1])

        }.run {
            clickDelete()

        }.run {
            checkEmptyMessage()

        }
    }

    @Test
    fun testShiftAndScale() {
        runBlocking {
            sightMarks.forEach {
                db.sightMarkDao().insert(it.asDatabaseSightMark())
            }
        }

        MainMenuRobot(composeTestRule).run {
            clickSightMarks()

        }.run {
            checkSightMarkDisplayed(sightMarks[0], true)
            checkSightMarkDisplayed(sightMarks[1])
            shiftAndScale()

        }.run {
            checkSightMarkDisplayed(sightMarks[0], true)
            checkSightMarkDisplayed(sightMarks[1])

            /*
             * Flip
             */
            clickFlip()
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 4.9f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 2.3f))

            clickFlip()
            checkSightMarkDisplayed(sightMarks[0], true)
            checkSightMarkDisplayed(sightMarks[1])

            /*
             * Shift
             */
            clickShiftChange(isIncrease = true, isLarge = true)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 3.3f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 5.9f))

            clickShiftChange(isIncrease = true, isLarge = false)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 3.4f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 6f))

            clickShiftChange(isIncrease = false, isLarge = false)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 3.3f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 5.9f))

            clickShiftChange(isIncrease = false, isLarge = true)
            checkSightMarkDisplayed(sightMarks[0], true)
            checkSightMarkDisplayed(sightMarks[1])

            /*
             * Scale
             */
            clickScaleChange(isIncrease = true, isLarge = true)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 4.6f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 9.8f))

            clickScaleChange(isIncrease = true, isLarge = false)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 4.83f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 10.29f))

            clickScaleChange(isIncrease = false, isLarge = false)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 4.6f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 9.8f))

            clickScaleChange(isIncrease = false, isLarge = true)
            checkSightMarkDisplayed(sightMarks[0], true)
            checkSightMarkDisplayed(sightMarks[1])

            /*
             * Reset
             */
            clickScaleChange(isIncrease = true, isLarge = true)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 4.6f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 9.8f))

            clickShiftChange(isIncrease = true, isLarge = true)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 5.6f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 10.8f))

            clickShiftReset()
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 4.6f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 9.8f))

            clickShiftChange(isIncrease = true, isLarge = true)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 5.6f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 10.8f))

            clickScaleReset()
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 3.3f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 5.9f))

            /*
             * Complete
             */
            clickScaleChange(isIncrease = true, isLarge = true)
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 5.6f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 10.8f))

            clickComplete()
        }.run {
            checkSightMarkDisplayed(sightMarks[0].copy(sightMark = 5.6f), true)
            checkSightMarkDisplayed(sightMarks[1].copy(sightMark = 10.8f))

        }
    }

    /**
     * Example of using the back button
     * TODO Update all robots to this pattern and swap large scale back test to this
     */
    @Test
    fun testBack() {
        MainMenuRobot(composeTestRule).run {
            clickSightMarks()

        }.run {
            checkEmptyMessage()

            // Add new
            clickAdd()

        }.run {
            clickAndroidBack<SightMarksRobot>()

        }.run {
            clickAndroidBack<MainMenuRobot>()

        }.run {
            clickSightMarks()

        }
    }

    companion object {
        private val sightMarks = listOf(
                SightMark(
                        id = 1,
                        bowId = null,
                        distance = 50,
                        isMetric = false,
                        dateSet = Calendar.getInstance(),
                        sightMark = 2.3f,
                        note = "Hello this is a note",
                        isMarked = false,
                        isArchived = false,
                ),
                SightMark(
                        id = 2,
                        bowId = null,
                        distance = 60,
                        isMetric = true,
                        dateSet = Calendar.getInstance(),
                        sightMark = 4.9f,
                        isMarked = true,
                        isArchived = true,
                ),
        )
    }
}
