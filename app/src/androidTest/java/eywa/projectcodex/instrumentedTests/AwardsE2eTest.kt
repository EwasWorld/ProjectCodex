package eywa.projectcodex.instrumentedTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import eywa.projectcodex.common.CommonSetupTeardownFns
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.hiltModules.LocalUpdateDefaultRoundsModule
import eywa.projectcodex.instrumentedTests.robots.mainMenuRobot
import eywa.projectcodex.instrumentedTests.robots.referenceTables.AwardsRobot
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AwardsE2eTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun classSetup() {
            LocalUpdateDefaultRoundsModule.useActual = true
        }

        @JvmStatic
        @AfterClass
        fun classTeardown() {
            LocalUpdateDefaultRoundsModule.useActual = false
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testTimeout: Timeout = Timeout.seconds(20)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>

    private fun setup() {
        LocalUpdateDefaultRoundsModule.useActual = true
        hiltRule.inject()

        scenario = composeTestRule.activityRule.scenario
    }

    @After
    fun afterEach() {
        CommonSetupTeardownFns.teardownScenario(scenario)
    }

    @Test
    fun testAwards() {
        setup()

        composeTestRule.mainMenuRobot {
            clickReferenceTables {
                clickTab(AwardsRobot::class) {
                    checkBowStyle("Recurve")
                    checkClub252(252, 252)
                    checkFrostbite(200, 355)
                    checkAwards(6, 800)

                    setBowStyle("Longbow")
                    checkClub252(164, 101)
                    checkFrostbite(101, 351)
                    checkAwards(6, 225)
                }
            }
        }
    }
}
