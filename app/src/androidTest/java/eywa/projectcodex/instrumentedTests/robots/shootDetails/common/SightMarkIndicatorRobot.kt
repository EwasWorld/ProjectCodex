package eywa.projectcodex.instrumentedTests.robots.shootDetails.common

import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchDataRowValue
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.SightMarkDetailRobot
import eywa.projectcodex.instrumentedTests.robots.SightMarksRobot
import eywa.projectcodex.instrumentedTests.robots.common.Robot

@RobotDslMarker
class SightMarkIndicatorRobot(
        private val robot: Robot,
        private val verticalScrollParent: CodexTestTag,
) {
    fun checkSightMarkIndicator(distance: String, sightMark: String?) {
        scrollToComponent()
        robot.performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(AddEndTestTag.SIGHT_MARK)
                +CodexNodeInteraction.AssertContentDescriptionEquals((sightMark ?: "None") + " $distance:")
            }
        }
    }

    fun checkAllSightMarkOnly() {
        scrollToComponent(AddEndTestTag.EXPAND_SIGHT_MARK)
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
        scrollToComponent(AddEndTestTag.EXPAND_SIGHT_MARK)
        robot.performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(AddEndTestTag.EXPAND_SIGHT_MARK)
                +CodexNodeInteraction.PerformClick()
            }
        }
        robot.createRobot(SightMarksRobot::class, block)
    }

    fun clickEditSightMark(block: SightMarkDetailRobot.() -> Unit) {
        scrollToComponent()
        robot.performV2 {
            singleNode {
                matchDataRowValue(AddEndTestTag.SIGHT_MARK)
                +CodexNodeInteraction.PerformScrollTo()
                +CodexNodeInteraction.PerformClick()
            }
        }
        robot.createRobot(SightMarkDetailRobot::class, block)
    }

    private fun scrollToComponent(component: AddEndTestTag = AddEndTestTag.SIGHT_MARK) {
        val componentMatcher = when (component) {
            AddEndTestTag.SIGHT_MARK -> {
                listOf(
                        CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(AddEndTestTag.SIGHT_MARK)),
                        CodexNodeMatcher.HasClickAction,
                )
            }

            AddEndTestTag.EXPAND_SIGHT_MARK -> listOf(CodexNodeMatcher.HasTestTag(AddEndTestTag.EXPAND_SIGHT_MARK))
            else -> throw UnsupportedOperationException()
        }

        robot.performV2 {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(verticalScrollParent)
                +CodexNodeInteraction.PerformScrollToNode(componentMatcher).waitFor()
            }
        }
    }
}
