package eywa.projectcodex.instrumentedTests.robots.selectRound

import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.TestActionDsl

@SelectRoundBaseDsl
class SelectRoundRobot internal constructor(val perform: (TestActionDsl.() -> Unit) -> Unit) {
    init {
        perform {
            +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun checkRoundOptions(displayNames: List<String>) {
        displayNames.forEach { displayName ->
            perform {
                useUnmergedTree = true
                displayName
                        .split(" ")
                        .forEach { +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(it)) }
                +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM)
                +CodexNodeInteraction.AssertIsDisplayed().waitFor()
            }
        }
    }

    fun checkRoundOptionsNotExist(displayNames: List<String>) {
        displayNames.forEach { displayName ->
            perform {
                useUnmergedTree = true
                displayName
                        .split(" ")
                        .forEach { +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(it)) }
                +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM)
                +CodexNodeInteraction.AssertDoesNotExist().waitFor()
            }
        }
    }

    fun checkNoFiltersAreOn() {
        Filter.values().forEach {
            perform {
                useUnmergedTree = true
                +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.FILTER)
                +CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(it.label))
                scrollToParentIndex = it.index
                +CodexNodeInteraction.AssertIsSelected(false)
            }
        }
    }

    fun clickFilter(filter: Filter, isNowOn: Boolean = true) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.FILTER)
            +CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(filter.label))
            scrollToParentIndex = filter.index
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.FILTER)
            +CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(filter.label))
            +CodexNodeInteraction.AssertIsSelected(isNowOn).waitFor()
        }
    }

    fun clickRound(displayName: String) {
        perform {
            useUnmergedTree = true
            displayName
                    .split(" ")
                    .forEach { +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(it)) }
            +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun clickNoRound() {
        perform {
            +CodexNodeMatcher.HasTestTag(SimpleDialogTestTag.POSITIVE_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
    }

    enum class Filter(val label: String, val index: Int) {
        METRIC("Metric", 1),
        IMPERIAL("Imperial", 2),
        INDOOR("Indoor", 3),
        OUTDOOR("Outdoor", 4),
    }
}
