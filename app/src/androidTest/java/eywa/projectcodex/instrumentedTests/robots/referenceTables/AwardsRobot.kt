package eywa.projectcodex.instrumentedTests.robots.referenceTables

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.components.referenceTables.awards.ui.AwardsTestTag
import eywa.projectcodex.components.referenceTables.classificationTables.ClassificationTablesTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.common.TabSwitcherRobot

class AwardsRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
) : BaseRobot(composeTestRule, AwardsTestTag.SCREEN), TabSwitcherRobot {
    override val group: TabSwitcherGroup
        get() = TabSwitcherGroup.REFERENCES

    fun checkBowStyle(value: String) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(AwardsTestTag.BOW_SELECTOR)
            +CodexNodeInteraction.AssertContentDescriptionEquals("$value Bow:")
        }
    }

    fun setBowStyle(value: String) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(AwardsTestTag.BOW_SELECTOR)
            +CodexNodeInteraction.PerformClick()
        }
        performSingle {
            +CodexNodeMatcher.HasTestTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM)
            +CodexNodeMatcher.HasText(value)
            +CodexNodeInteraction.PerformClick()
        }
        checkBowStyle(value)
    }

    fun checkClub252(tenYardValue: Int, hundredYardValue: Int) {
        performGroup {
            +CodexNodeMatcher.HasTestTag(AwardsTestTag.CLUB_252_TABLE_SCORE)
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.AssertTextEquals(tenYardValue.toString())
            }
        }
        performGroup {
            +CodexNodeMatcher.HasTestTag(AwardsTestTag.CLUB_252_TABLE_SCORE)
            toSingle(CodexNodeGroupToOne.Last) {
                +CodexNodeInteraction.AssertTextEquals(hundredYardValue.toString())
            }
        }
    }

    fun checkFrostbite(twoHundredValue: Int, threeFiveFiveValue: Int) {
        performGroup {
            +CodexNodeMatcher.HasTestTag(AwardsTestTag.CLUB_FROSTBITE_TABLE_SCORE)
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.AssertTextEquals(twoHundredValue.toString())
            }
        }
        performGroup {
            +CodexNodeMatcher.HasTestTag(AwardsTestTag.CLUB_FROSTBITE_TABLE_SCORE)
            toSingle(CodexNodeGroupToOne.Last) {
                +CodexNodeInteraction.AssertTextEquals(threeFiveFiveValue.toString())
            }
        }
    }

    fun checkAwards(size: Int, herefordWhiteValue: Int) {
        performGroup {
            +CodexNodeMatcher.HasTestTag(AwardsTestTag.AGB_TABLE_ROUND)
            +CodexNodeMatcher.IsNotCached
            +CodexNodeGroupInteraction.AssertCount(size)
        }
        performGroup {
            +CodexNodeMatcher.HasTestTag(AwardsTestTag.AGB_TABLE_SCORE)
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.AssertTextEquals(herefordWhiteValue.toString())
            }
        }
    }
}
