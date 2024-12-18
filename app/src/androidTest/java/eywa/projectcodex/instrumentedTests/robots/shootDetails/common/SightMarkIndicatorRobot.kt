package eywa.projectcodex.instrumentedTests.robots.shootDetails.common

import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.SightMarkDetailRobot
import eywa.projectcodex.instrumentedTests.robots.SightMarksRobot
import eywa.projectcodex.instrumentedTests.robots.common.Robot

@RobotDslMarker
class SightMarkIndicatorRobot(private val robot: Robot) {
    fun checkSightMarkIndicator(distance: String, sightMark: String?) {
        robot.performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(AddEndTestTag.SIGHT_MARK)
                +CodexNodeInteraction.AssertContentDescriptionEquals((sightMark ?: "None") + " $distance:")
            }
        }
    }

    fun checkAllSightMarkOnly() {
        robot.performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(AddEndTestTag.EXPAND_SIGHT_MARK)
                +CodexNodeInteraction.AssertIsDisplayed()
            }
        }
        robot.performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(AddEndTestTag.SIGHT_MARK)
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }
    }

    fun clickAllSightMarks(block: SightMarksRobot.() -> Unit) {
        robot.performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(AddEndTestTag.EXPAND_SIGHT_MARK)
                +CodexNodeInteraction.PerformClick()
            }
        }
        robot.createRobot(SightMarksRobot::class, block)
    }

    fun clickEditSightMark(block: SightMarkDetailRobot.() -> Unit) {
        robot.performV2 {
            clickDataRow(AddEndTestTag.SIGHT_MARK)
        }
        robot.createRobot(SightMarkDetailRobot::class, block)
    }
}
