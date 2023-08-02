package eywa.projectcodex.components.emailScores

import android.content.Intent
import android.content.res.Resources
import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.model.FullShootInfo

data class EmailScoresState(
        val rounds: List<FullShootInfo> = emptyList(),
        val textFields: Map<EmailScoresTextField, String> = mapOf(),
        val booleanFields: Set<EmailScoresCheckbox> = setOf(),
        val error: EmailScoresError? = null,
        /**
         * null if no intent needs to be sent.
         * Otherwise contains the email intent info minus the [Intent.EXTRA_TEXT] field, which is blank
         * (requires [Resources] to populate which is not available in the view model)
         */
        val intentWithoutTextExtra: Intent? = null,
        val navigateUpTriggered: Boolean = false,
) {
    fun isChecked(field: EmailScoresCheckbox) = booleanFields.contains(field)
    fun getText(field: EmailScoresTextField, default: String = "") = textFields[field] ?: default

    fun getRoundsText(resources: Resources) =
            if (rounds.isEmpty()) resources.getString(R.string.email_scores__loading)
            else rounds.joinToString("\n\n") { entry -> entry.getScoreSummary(resources) }
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
