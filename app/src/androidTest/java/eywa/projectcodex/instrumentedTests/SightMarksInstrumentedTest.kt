package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule
import eywa.projectcodex.instrumentedTests.robots.MainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.SightMarksRobot
import eywa.projectcodex.model.SightMark
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
    private lateinit var navController: NavController
    private lateinit var db: ScoresRoomDatabase

    @Before
    fun beforeEach() {
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario

        scenario.onActivity {
            db = LocalDatabaseModule.scoresRoomDatabase!!
            navController = it.navHostFragment.navController
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
            checkSightMarkDisplayed(sightMarks[0])
            checkSightMarkDisplayed(sightMarks[1])
            checkDiagramTickLabelRange("2", "5")

            // Flip
            flipDiagram()
            checkSightMarkDisplayed(sightMarks[0])
            checkSightMarkDisplayed(sightMarks[1])
            checkDiagramTickLabelRange("5", "2")

            // Archive
            archiveAll()
            checkSightMarkDisplayed(sightMarks[0].copy(isArchived = true))
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

            // Cancel
            clickSightMark(sightMarks[1])

        }.run {
            setInfo(sightMarks[0])
            clickCancel()

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
                        bowId = -1,
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
                        bowId = -1,
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
