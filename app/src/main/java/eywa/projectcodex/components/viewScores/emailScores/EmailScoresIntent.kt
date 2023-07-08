package eywa.projectcodex.components.viewScores.emailScores

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import java.io.File

sealed class EmailScoresIntent {
    data class UpdateText(val value: String, val type: EmailScoresTextField) : EmailScoresIntent()
    data class UpdateBoolean(val value: Boolean, val type: EmailScoresCheckbox) : EmailScoresIntent()
    data class SetInitialValues(
            val subject: String,
            val messageHeader: String,
            val messageFooter: String,
    ) : EmailScoresIntent()

    data class OpenError(val error: EmailScoresError) : EmailScoresIntent()
    object DismissNoEntriesError : EmailScoresIntent()
    object NavigateUpHandled : EmailScoresIntent()
    data class SubmitClicked(val externalFilesDir: File) : EmailScoresIntent()
    object IntentHandledSuccessfully : EmailScoresIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : EmailScoresIntent()
}

enum class EmailScoresTextField { TO, SUBJECT, MESSAGE_HEADER, MESSAGE_FOOTER }
enum class EmailScoresCheckbox { FULL_SCORE_SHEET, DISTANCE_TOTAL }
