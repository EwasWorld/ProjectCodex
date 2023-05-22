package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.*
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresCheckbox
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresScreen
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresTextField

class EmailScoreRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, EmailScoresScreen.TestTag.SCREEN) {
    fun typeText(field: EmailScoresTextField, text: String, clearTextFirst: Boolean = false) {
        val node = composeTestRule.onNodeWithTag(EmailScoresScreen.TestTag.forTextField(field))
        if (clearTextFirst) node.performTextClearance()
        node.performTextInput(text)
    }

    fun clickCheckbox(field: EmailScoresCheckbox) {
        composeTestRule
                .onNodeWithTag(EmailScoresScreen.TestTag.forCheckbox(field))
                .performClick()
    }

    fun checkTextFieldText(field: EmailScoresTextField, text: String) {
        composeTestRule
                .onNode(
                        hasTestTag(EmailScoresScreen.TestTag.forTextField(field))
                                .and(hasText(text))
                )
                .assertIsDisplayed()
    }

    fun clickSend() {
        composeTestRule
                .onNodeWithTag(EmailScoresScreen.TestTag.SEND_BUTTON)
                .performClick()
    }

    fun checkCheckboxState(field: EmailScoresCheckbox, expectIsChecked: Boolean) {
        checkCheckboxState(EmailScoresScreen.TestTag.forCheckbox(field), expectIsChecked)
    }

    fun checkScoreText(text: String) {
        composeTestRule
                .onNodeWithTag(EmailScoresScreen.TestTag.SCORE_TEXT)
                .assertTextEquals(text)
    }
}
