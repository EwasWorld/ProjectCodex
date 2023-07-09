package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresCheckbox
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresTestTag
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresTextField

class EmailScoreRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, EmailScoresTestTag.SCREEN) {
    fun typeText(field: EmailScoresTextField, text: String, append: Boolean = false) =
            setText(EmailScoresTestTag.forTextField(field), text, append)

    fun clickCheckbox(field: EmailScoresCheckbox) =
            clickElement(EmailScoresTestTag.forCheckbox(field))

    fun checkTextFieldText(field: EmailScoresTextField, text: String) =
            checkElementText(EmailScoresTestTag.forTextField(field), text)

    fun clickSend() =
            clickElement(EmailScoresTestTag.SEND_BUTTON)

    fun checkCheckboxState(field: EmailScoresCheckbox, expectIsChecked: Boolean) =
            checkCheckboxState(EmailScoresTestTag.forCheckbox(field), expectIsChecked)

    fun checkScoreText(text: String) =
            checkElementText(EmailScoresTestTag.SCORE_TEXT, text)
}
