package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.referenceTables.HeadToHeadReferenceRobot
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HeadToHeadReferenceE2eTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(30)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>

    private fun setup() {
        CommonSetupTeardownFns.generalSetup()
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testNoRound() {
        setup()

        composeTestRule.mainMenuRobot {
            clickReferenceTables {
                clickTab(HeadToHeadReferenceRobot::class) {
                    setArcherARank(7)
                    setArcherBRank(10)
                    setTotalArchers(20)
                    checkMeetInString(7, 10, "1/8")
                    checkTable(
                            mapOf(
                                    7 to HeadToHeadUseCase.getOpponents(7, 20).reversed(),
                                    10 to HeadToHeadUseCase.getOpponents(10, 20).reversed(),
                            ),
                    )
                }
            }
        }
    }
}
