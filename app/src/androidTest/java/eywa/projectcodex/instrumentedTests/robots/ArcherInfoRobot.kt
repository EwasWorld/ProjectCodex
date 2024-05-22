package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.components.archerInfo.ArcherInfoTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchDataRowValue
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.TabSwitcherRobot

class ArcherInfoRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ArcherInfoTestTag.SCREEN), TabSwitcherRobot {
    override val group: TabSwitcherGroup
        get() = TabSwitcherGroup.ARCHER_INFO

    fun clickGender() {
        clickElement(ArcherInfoTestTag.GENDER_SELECTOR)
    }

    fun checkGenderIsGent(isGent: Boolean = true) {
        performV2Single {
            matchDataRowValue(ArcherInfoTestTag.GENDER_SELECTOR)
            +CodexNodeInteraction.AssertTextEquals(if (isGent) "Gentleman" else "Lady")
        }
    }

    fun setAge(value: String) {
        clickElement(ArcherInfoTestTag.AGE_SELECTOR)
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArcherInfoTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun setBowStyle(value: String) {
        clickElement(ArcherInfoTestTag.BOW_SELECTOR)
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArcherInfoTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
    }
}
