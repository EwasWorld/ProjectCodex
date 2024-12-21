package eywa.projectcodex.instrumentedTests.robots.shootDetails.common

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.ArcherInfoRobot
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.referenceTables.ClassificationTablesRobot
import eywa.projectcodex.instrumentedTests.robots.referenceTables.HandicapTablesRobot

@RobotDslMarker
class HandicapAndClassificationSectionRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: CodexTestTag,
) : BaseRobot(composeTestRule, screenTestTag) {
    fun checkHandicap(text: Int?) {
        checkElementText(StatsTestTag.HANDICAP_TEXT, text?.toString() ?: "--", true)
    }

    fun checkHandicapDoesNotExist() {
        checkElementDoesNotExist(StatsTestTag.HANDICAP_TEXT, true)
        checkElementDoesNotExist(StatsTestTag.HANDICAP_TABLES, true)
    }

    fun checkClassificationCategory(value: String) {
        checkElementText(StatsTestTag.CLASSIFICATION_CATEGORY, value, true)
    }

    fun checkClassification(
            classification: Classification?,
            isOfficial: Boolean,
    ) {
        val expectedValue = classification?.classificationString() ?: "No classification"
        checkElementText(StatsTestTag.CLASSIFICATION, expectedValue, true)

        performV2Single {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION)
            +CodexNodeInteraction.AssertContentDescriptionEquals(
                    "$expectedValue " +
                            if (isOfficial) "Classification" else "Classification (unofficial)"
            )
        }
    }

    fun checkClassificationDoesNotExist() {
        checkElementDoesNotExist(StatsTestTag.CLASSIFICATION)
        checkElementDoesNotExist(StatsTestTag.CLASSIFICATION_CATEGORY)
        checkElementDoesNotExist(StatsTestTag.CLASSIFICATION_TABLES)
    }

    fun openHandicapTablesInFull(block: HandicapTablesRobot.() -> Unit) {
        try {
            performV2Single {
                +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TABLES)
                +CodexNodeInteraction.PerformScrollTo()
                +CodexNodeInteraction.AssertIsDisplayed()
                +CodexNodeInteraction.PerformClick()
            }
        }
        catch (e: AssertionError) {
            // Component is probably hidden by the bottom nav bar
            // Scroll the component above to bring it into view and try again to click
            if (e.message?.contains("The component is not displayed") == true) {
                performV2Single {
                    +CodexNodeMatcher.HasTestTag(StatsTestTag.HSG_SECTION)
                    +CodexNodeInteraction.Swipe(CodexNodeInteraction.Swipe.Direction.UP)
                }
                performV2Single {
                    +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TABLES)
                    +CodexNodeInteraction.PerformScrollTo()
                    +CodexNodeInteraction.AssertIsDisplayed()
                    +CodexNodeInteraction.PerformClick()
                }
            }
            else throw e
        }

        createRobot(HandicapTablesRobot::class, block)
    }

    fun openClassificationTablesInFull(block: ClassificationTablesRobot.() -> Unit) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION_TABLES)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
        createRobot(ClassificationTablesRobot::class, block)
    }

    fun openEditArcherInfo(block: ArcherInfoRobot.() -> Unit) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION_CATEGORY)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
        createRobot(ArcherInfoRobot::class, block)
    }

    private fun Classification.classificationString() = when (this) {
        Classification.ARCHER_3RD_CLASS -> "Archer 3rd"
        Classification.ARCHER_2ND_CLASS -> "Archer 2nd"
        Classification.ARCHER_1ST_CLASS -> "Archer 1st"
        Classification.BOWMAN_3RD_CLASS -> "Bowman 3rd"
        Classification.BOWMAN_2ND_CLASS -> "Bowman 2nd"
        Classification.BOWMAN_1ST_CLASS -> "Bowman 1st"
        Classification.MASTER_BOWMAN -> "Master Bowman"
        Classification.GRAND_MASTER_BOWMAN -> "Grand MB"
        Classification.ELITE_MASTER_BOWMAN -> "Elite GMB"
    }
}
