package eywa.projectcodex.components.viewScores.emailScores

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.database.rounds.FullRoundInfo

data class EmailScoresState(
        val rounds: List<FullRoundInfo> = emptyList(),
        val textFields: Map<EmailScoresTextField, String> = mapOf(),
        val booleanFields: Set<EmailScoresCheckbox> = setOf(),
        val touchedFields: Set<EmailScoresTextField> = setOf(),
        val error: EmailScoresError? = null,
) {
    fun isChecked(field: EmailScoresCheckbox) = booleanFields.contains(field)
    fun getText(field: EmailScoresTextField) = textFields[field] ?: ""
    fun wasTouched(field: EmailScoresTextField) = touchedFields.contains(field)
}

enum class EmailScoresError(
        @StringRes val title: Int,
        @StringRes val message: Int,
        @StringRes val buttonText: Int,
) {
    NO_SELECTED_ENTRIES(
            title = R.string.err__general_error_dialog_title,
            message = R.string.email_scores__no_entries_dialog_body,
            buttonText = R.string.general_back,
    ),
    ERROR_CREATING_ATTACHMENT(
            title = R.string.email_scores__no_entries_dialog_title,
            message = R.string.err_email_scores__attachment_creation,
            buttonText = R.string.general_ok,
    ),
    NO_EMAIL_APP_FOUND(
            title = R.string.err__general_error_dialog_title,
            message = R.string.err_email_scores__no_app_found,
            buttonText = R.string.general_ok,
    ),
}