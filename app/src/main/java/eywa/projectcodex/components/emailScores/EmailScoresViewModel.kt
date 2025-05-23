package eywa.projectcodex.components.emailScores

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eywa.projectcodex.R
import eywa.projectcodex.common.diActivityHelpers.ShootIdsUseCase
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.emailScores.EmailScoresIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.datastore.retrieve
import eywa.projectcodex.exceptions.UserException
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.scorePadData.ScorePadData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EmailScoresViewModel @Inject constructor(
        // TODO Don't use the context here >:[ Separation of concerns
        @ApplicationContext private val context: Context,
        private val datastore: CodexDatastore,
        private val helpShowcase: HelpShowcaseUseCase,
        db: ScoresRoomDatabase,
        private val shootIdsUseCase: ShootIdsUseCase,
        private val customLogger: CustomLogger,
) : ViewModel() {
    private val repo = db.shootsRepo()

    private val _state = MutableStateFlow(EmailScoresState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            shootIdsUseCase.getItems
                    .flatMapLatest {
                        if (it == null) emptyFlow<List<DatabaseFullShootInfo>?>()
                        else repo.getFullShootInfo(it)
                    }
                    .combine(
                            datastore.get(listOf(DatastoreKey.Use2023HandicapSystem, DatastoreKey.SavedEmails)),
                    ) { shootInfo, datastoreData -> shootInfo to datastoreData }
                    .collectLatest { (entries, datastoreData) ->
                        _state.update {
                            if (entries.isNullOrEmpty()) {
                                it.copy(error = EmailScoresError.NO_SELECTED_ENTRIES)
                            }
                            else {
                                val use2023System = datastoreData.retrieve(DatastoreKey.Use2023HandicapSystem)
                                it.copy(
                                        rounds = entries.map { entry -> FullShootInfo(entry, use2023System) },
                                        error = it.error
                                                .takeIf { error -> error != EmailScoresError.NO_SELECTED_ENTRIES },
                                )
                            }.copy(
                                    savedEmails = datastoreData
                                            .retrieve(DatastoreKey.SavedEmails)
                                            .split(DatastoreKey.SavedEmails.DELIM)
                                            .filter { email -> email.isNotBlank() }
                                            .sorted(),
                            )
                        }
                    }
        }
    }

    fun handle(action: EmailScoresIntent) {
        when (action) {
            is UpdateText -> _state.update { it.copy(textFields = it.textFields.plus(action.type to action.value)) }
            is UpdateEmail -> _state.update { it.copy(emailField = action.value) }

            is InsertEmail -> {
                _state.update {
                    val text = it.emailField.text
                    var cursor = it.emailField.selection.start
                    while (cursor > 0 && text[cursor - 1].toString() !in EmailScoresState.emailDelimiters) {
                        cursor--
                    }
                    cursor = cursor.coerceAtLeast(0)
                    val start = text.take(cursor)
                    val end = text.drop(cursor + it.currentlyTypingEmail().length)
                    it.copy(
                            emailField = TextFieldValue(
                                    text = start + action.value + end,
                                    selection = TextRange(cursor + action.value.length),
                            ),
                    )
                }
            }

            is UpdateBoolean ->
                _state.update {
                    it.copy(
                            booleanFields = it.booleanFields.let { field ->
                                if (action.value) field.plus(action.type) else field.minus(action.type)
                            },
                    )
                }

            DismissNoEntriesError -> {
                if (state.value.error == EmailScoresError.NO_SELECTED_ENTRIES) {
                    shootIdsUseCase.clear()
                    _state.update { it.copy(error = null, navigateUpTriggered = true) }
                }
            }

            is IntentHandledSuccessfully -> {
                shootIdsUseCase.clear()
                _state.update { it.copy(intentWithoutTextExtra = null) }
            }

            is OpenError ->
                _state.update {
                    it.copy(
                            error = action.error,
                            // Clear the intent if NO_EMAIL_APP_FOUND
                            intentWithoutTextExtra = it.intentWithoutTextExtra?.takeIf { action.error != EmailScoresError.NO_EMAIL_APP_FOUND },
                    )
                }

            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.EMAIL_SCORE::class)
            is SubmitClicked -> sendButtonListener(state.value)
            NavigateUpHandled -> _state.update { it.copy(navigateUpTriggered = false) }
            ToggleSaveEmailsCheckbox -> _state.update { it.copy(saveEmails = !it.saveEmails) }
            is RemoveEmail -> viewModelScope.launch {
                datastore.set(
                        key = DatastoreKey.SavedEmails,
                        value = _state.value.savedEmails
                                .minus(action.email)
                                .joinToString(DatastoreKey.SavedEmails.DELIM),
                )
            }
        }
    }

    /**
     * Creates a file in [Context.getExternalFilesDir] with the name [EMAIL_ATTACHMENT_FILENAME]. Writes the data from
     * [EmailScoresState.rounds] to the file
     *
     * @return the created file's [Uri]
     * @throws IllegalStateException on IO error
     * @throws UserException
     */
    private fun createAttachment(state: EmailScoresState): Uri {
        /*
         * Create file
         */
        val externalFilesDir = context.getExternalFilesDir(null)
                ?: throw IllegalStateException("Unable to access storage")
        val attachment = File(externalFilesDir, EMAIL_ATTACHMENT_FILENAME)

        // Make sure we're writing to a clean file as the previous one may not have been deleted
        // Note: we cannot delete this file because we don't know when the email application is finished with it
        //      (send could still be pending)
        attachment.delete()
        if (!attachment.createNewFile()) {
            throw IllegalStateException("Unable to create attachment file")
        }

        /*
         * Write contents
         */
        val fileWriter = FileWriter(attachment)
        for (entry in state.rounds) {
            val detailedScorePad =
                    if (entry.h2h == null) {
                        entry.getScorePadData(END_SIZE)
                                ?.getDetailsAsCsv(
                                        columnOrder,
                                        context.resources,
                                        state.isChecked(EmailScoresCheckbox.DISTANCE_TOTAL),
                                )
                    }
                    else {
                        null
                    }
            fileWriter.append(entry.getScoreSummary().get(context.resources))
            detailedScorePad?.let {
                fileWriter.append("\n\n")
                fileWriter.append(detailedScorePad.headerRow)
                fileWriter.append("\n")
                fileWriter.append(detailedScorePad.details)
            }
            fileWriter.append("\n\n\n")
        }
        fileWriter.flush()
        fileWriter.close()

        return FileProvider.getUriForFile(
                context,
                context.packageName + ".fileProvider",
                attachment,
        )
    }

    private fun sendButtonListener(currentState: EmailScoresState) {
        fun EmailScoresTextField.getText() = currentState.getText(this).trim()

        var uri: Uri? = null
        if (currentState.isChecked(EmailScoresCheckbox.FULL_SCORE_SHEET)) {
            try {
                uri = createAttachment(currentState)
            }
            // TODO Exception catch is very broad
            catch (e: Exception) {
                if (!e.message.isNullOrBlank()) {
                    customLogger.e(LOG_TAG, "Email attachment creation error: ${e.message!!}")
                }
                _state.update { it.copy(error = EmailScoresError.ERROR_CREATING_ATTACHMENT) }
                return
            }
        }
        val emails = currentState.emailField.text
                .split(*EmailScoresState.emailDelimiters)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        val message = "%s\n\n%s\n\n%s\n\n\n\n%s".format(
                currentState.getText(EmailScoresTextField.MESSAGE_HEADER),
                currentState.getRoundsText(context.resources).trim(),
                currentState.getText(EmailScoresTextField.MESSAGE_FOOTER),
                context.getString(R.string.email_default_message_signature),
        )

        val emailSelectorIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
        }
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
            putExtra(Intent.EXTRA_SUBJECT, EmailScoresTextField.SUBJECT.getText())
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_STREAM, uri)
            selector = emailSelectorIntent
        }

        if (_state.value.saveEmails) {
            viewModelScope.launch {
                datastore.set(
                        key = DatastoreKey.SavedEmails,
                        value = (_state.value.savedEmails + emails)
                                .distinct()
                                .joinToString(DatastoreKey.SavedEmails.DELIM),
                )
            }
        }

        _state.update { it.copy(intentWithoutTextExtra = emailIntent) }
    }

    companion object {
        private const val LOG_TAG = "EmailScores"
        private const val EMAIL_ATTACHMENT_FILENAME = "emailAttachment.csv"
        private const val END_SIZE = 6
        private val columnOrder = listOf(
                ScorePadData.ScorePadColumnType.ARROWS,
                ScorePadData.ScorePadColumnType.HITS,
                ScorePadData.ScorePadColumnType.SCORE,
                ScorePadData.ScorePadColumnType.GOLDS,
                ScorePadData.ScorePadColumnType.RUNNING_TOTAL,
        )
    }
}
