package eywa.projectcodex.instrumentedTests.robots.common

import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.common.sharedUi.TabSwitcherTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.ArcherHandicapRobot
import eywa.projectcodex.instrumentedTests.robots.ArcherInfoRobot
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.ClassificationTablesRobot
import eywa.projectcodex.instrumentedTests.robots.HandicapTablesRobot
import kotlin.reflect.KClass

interface TabSwitcherRobot : Robot {
    val group: TabSwitcherGroup

    fun <T : BaseRobot> clickTab(tab: KClass<T>, block: T.() -> Unit) {
        require(tab.getGroup() == group) { "Tab not available from this robot" }

        perform {
            +CodexNodeMatcher.HasTestTag(TabSwitcherTestTag.ITEM)
            +CodexNodeMatcher.HasText(tab.getTextLabel())
            +CodexNodeInteraction.PerformClick
        }

        createRobot(tab, block)
    }
}

private fun <T : BaseRobot> KClass<T>.getTextLabel() = when (this) {
    ArcherHandicapRobot::class -> "Handicap"
    ArcherInfoRobot::class -> "Category"
    HandicapTablesRobot::class -> "Handicaps"
    ClassificationTablesRobot::class -> "Classifications"
    else -> throw NotImplementedError()
}

private fun <T : BaseRobot> KClass<T>.getGroup() = when (this) {
    ArcherHandicapRobot::class -> CodexNavRoute.ARCHER_HANDICAPS
    ArcherInfoRobot::class -> CodexNavRoute.ARCHER_INFO
    HandicapTablesRobot::class -> CodexNavRoute.HANDICAP_TABLES
    ClassificationTablesRobot::class -> CodexNavRoute.CLASSIFICATION_TABLES
    else -> throw NotImplementedError()
}.tabSwitcherItem?.group
