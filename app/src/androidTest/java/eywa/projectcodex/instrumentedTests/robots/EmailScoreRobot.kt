package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.emailScores.EmailScoresCheckbox
import eywa.projectcodex.components.emailScores.EmailScoresTestTag
import eywa.projectcodex.components.emailScores.EmailScoresTextField
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkInputtedText
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class EmailScoreRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
) : BaseRobot(composeTestRule, EmailScoresTestTag.Screen) {
    fun clickEmailField() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(EmailScoresTestTag.EmailTextField)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun clickEmail(text: String) {
        performV2Single {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(EmailScoresTestTag.SavedEmailDropdownItem)
            +CodexNodeMatcher.HasText(text)
            +CodexNodeInteraction.PerformClick().waitFor()
        }
    }

    fun checkEmailText(text: String) {
        performV2 {
            checkInputtedText(EmailScoresTestTag.EmailTextField, text)
        }
    }

    fun typeEmail(text: String, append: Boolean = false) {
        performV2 {
            setText(EmailScoresTestTag.EmailTextField, text, append)
        }
    }

    fun typeText(field: EmailScoresTextField, text: String, append: Boolean = false) {
        performV2 {
            setText(EmailScoresTestTag.TextField(field), text, append)
        }
    }

    fun clickCheckbox(field: EmailScoresCheckbox) {
        performV2Single {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(EmailScoresTestTag.Checkbox(field))
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkTextFieldText(field: EmailScoresTextField, text: String) {
        performV2 {
            checkInputtedText(EmailScoresTestTag.TextField(field), text)
        }
    }

    fun clickSend() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(EmailScoresTestTag.SendButton)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkCheckboxState(field: EmailScoresCheckbox, expectIsChecked: Boolean) {
        checkCheckboxState(EmailScoresTestTag.Checkbox(field), expectIsChecked, useUnmergedTree = true)
    }

    fun checkScoreText(text: String) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(EmailScoresTestTag.ScoreText)
            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }
}
