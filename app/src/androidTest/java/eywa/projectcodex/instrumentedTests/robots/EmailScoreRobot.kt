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
    fun typeText(field: EmailScoresTextField, text: String, append: Boolean = false) {
        perform {
            setText(EmailScoresTestTag.TextField(field), text, append)
        }
    }

    fun clickCheckbox(field: EmailScoresCheckbox) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(EmailScoresTestTag.Checkbox(field))
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkTextFieldText(field: EmailScoresTextField, text: String) {
        perform {
            checkInputtedText(EmailScoresTestTag.TextField(field), text)
        }
    }

    fun clickSend() {
        perform {
            +CodexNodeMatcher.HasTestTag(EmailScoresTestTag.SendButton)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkCheckboxState(field: EmailScoresCheckbox, expectIsChecked: Boolean) {
        checkCheckboxState(EmailScoresTestTag.Checkbox(field), expectIsChecked, useUnmergedTree = true)
    }

    fun checkScoreText(text: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(EmailScoresTestTag.ScoreText)
            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }
}
