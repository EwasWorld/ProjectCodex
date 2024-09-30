package eywa.projectcodex.components.emailScores

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.TextFieldValue
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import java.io.File

sealed class EmailScoresIntent {
    data class UpdateEmail(val value: TextFieldValue) : EmailScoresIntent()
    data class InsertEmail(val value: String) : EmailScoresIntent()
    data class UpdateText(val value: String, val type: EmailScoresTextField) : EmailScoresIntent()
    data class UpdateBoolean(val value: Boolean, val type: EmailScoresCheckbox) : EmailScoresIntent()
    data class OpenError(val error: EmailScoresError) : EmailScoresIntent()
    data object DismissNoEntriesError : EmailScoresIntent()
    data object NavigateUpHandled : EmailScoresIntent()
    data class SubmitClicked(val externalFilesDir: File) : EmailScoresIntent()
    data object IntentHandledSuccessfully : EmailScoresIntent()
    data object ToggleSaveEmailsCheckbox : EmailScoresIntent()
    data class RemoveEmail(val email: String) : EmailScoresIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : EmailScoresIntent()
}

enum class EmailScoresTextField(@StringRes val default: Int? = null) {
    SUBJECT(R.string.email_default_message_subject),
    MESSAGE_HEADER(R.string.email_default_message_header),
    MESSAGE_FOOTER(R.string.email_default_message_footer),
}

enum class EmailScoresCheckbox { FULL_SCORE_SHEET, DISTANCE_TOTAL }
