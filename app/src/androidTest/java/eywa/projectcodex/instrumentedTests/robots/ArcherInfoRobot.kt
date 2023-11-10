package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.components.archerInfo.ArcherInfoTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.TabSwitcherRobot

class ArcherInfoRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ArcherInfoTestTag.SCREEN), TabSwitcherRobot {
    override val group: TabSwitcherGroup
        get() = TabSwitcherGroup.ARCHER_INFO

    fun clickGender() {
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherInfoTestTag.GENDER_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun setAge(value: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherInfoTestTag.AGE_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherInfoTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun setBowStyle(value: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherInfoTestTag.BOW_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ArcherInfoTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
    }
}
