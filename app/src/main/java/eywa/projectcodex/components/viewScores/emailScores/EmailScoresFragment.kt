package eywa.projectcodex.components.viewScores.emailScores

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.CodexChipState
import eywa.projectcodex.common.sharedUi.CodexTextFieldState
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadData
import eywa.projectcodex.components.viewScores.ViewScoresViewModel
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.exceptions.UserException
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter


@AndroidEntryPoint
class EmailScoresFragment : Fragment(), ActionBarHelp {
    companion object {
        private const val EMAIL_ATTACHMENT_FILENAME = "emailAttachment.csv"
        private const val LOG_TAG = "EmailScoresFragment"
    }

    private val args: EmailScoresFragmentArgs by navArgs()

    // TODO Remove this view model
    private val viewScoresViewModel: ViewScoresViewModel by activityViewModels()
    private val emailScoresViewModel: EmailScoresViewModel by viewModels()
    private var emailScoresScreen = EmailScoresScreen()
    private val endSize = 6

    private val columnHeaderOrder = listOf(
            ScorePadData.ColumnHeader.END_STRING,
            ScorePadData.ColumnHeader.HITS,
            ScorePadData.ColumnHeader.SCORE,
            ScorePadData.ColumnHeader.GOLDS,
            ScorePadData.ColumnHeader.RUNNING_TOTAL
    )

    private fun EmailScoresTextField.asState() = CodexTextFieldState(
            text = emailScoresViewModel.state.getText(this),
            onValueChange = { emailScoresViewModel.handle(EmailScoresIntent.UpdateText(it, this)) },
            testTag = EmailScoresScreen.TestTag.forTextField(this),
    )

    private fun EmailScoresCheckbox.asState(enabled: Boolean = true) = emailScoresViewModel.state.isChecked(this).let {
        CodexChipState(
                selected = it,
                onToggle = { emailScoresViewModel.handle(EmailScoresIntent.UpdateBoolean(!it, this)) },
                enabled = enabled,
                testTag = EmailScoresScreen.TestTag.forCheckbox(this),
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CodexTheme {
                    LaunchedEffect(Unit) {
                        launch {
                            emailScoresViewModel.effects.collect { effect ->
                                @Suppress("REDUNDANT_ELSE_IN_WHEN")
                                when (effect) {
                                    EmailScoresEffect.NavigateUp -> requireView().findNavController().popBackStack()
                                    else -> throw NotImplementedError()
                                }
                            }
                        }
                    }

                    if (getSelectedEntries().isEmpty()) {
                        emailScoresViewModel.handle(EmailScoresIntent.OpenError(EmailScoresError.NO_SELECTED_ENTRIES))
                    }

                    // TODO Change to entries coming in from the navigation therefore can call this every time on startup?
                    val initialValuesIntent = EmailScoresIntent.SetInitialValues(
                            subject = stringResource(R.string.email_default_message_subject),
                            messageHeader = stringResource(R.string.email_default_message_header),
                            messageFooter = stringResource(R.string.email_default_message_footer),
                    )
                    rememberSaveable(getSelectedEntries().map { it.id }) {
                        // TODO Is this the right place to do this?
                        emailScoresViewModel.handle(initialValuesIntent)
                        mutableStateOf(true)
                    }

                    val messageScoreText = getSelectedEntries().joinToString("\n\n") { entry ->
                        entry.getScoreSummary(resources)
                    }
                    emailScoresScreen.ComposeContent(
                            error = emailScoresViewModel.state.error,
                            toState = EmailScoresTextField.TO.asState(),
                            subjectState = EmailScoresTextField.SUBJECT.asState(),
                            messageHeaderState = EmailScoresTextField.MESSAGE_HEADER.asState(),
                            messageScoreText = messageScoreText,
                            messageFooterState = EmailScoresTextField.MESSAGE_FOOTER.asState(),
                            fullScoreSheetState = EmailScoresCheckbox.FULL_SCORE_SHEET.asState(),
                            distanceTotalsSheetState = EmailScoresCheckbox.DISTANCE_TOTAL.asState(
                                    emailScoresViewModel.state.isChecked(EmailScoresCheckbox.FULL_SCORE_SHEET)
                            ),
                            onSubmit = { sendButtonListener(messageScoreText) },
                            onErrorOkClicked = { emailScoresViewModel.handle(EmailScoresIntent.CloseError) },
                    )
                }
            }
        }
    }

    /**
     * Creates a file in [Context.getExternalFilesDir] with the name [EMAIL_ATTACHMENT_FILENAME]. Writes the data from
     * [getSelectedEntries] to the file
     *
     * @return the created file's [Uri]
     * @throws IllegalStateException on IO error
     * @throws UserException
     */
    private fun createAttachment(includeDistanceHeaders: Boolean): Uri {
        val selectedEntries = getSelectedEntries()

        /*
         * Create file
         */
        val externalFilesDir = requireContext().getExternalFilesDir(null)
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
        for (entry in selectedEntries) {
            val detailedScorePad = entry.getScorePadData(endSize, resources)!!
                    .getDetailsAsCsv(columnHeaderOrder, resources, includeDistanceHeaders)
            fileWriter.append(entry.getScoreSummary(resources))
            fileWriter.append("\n\n")
            fileWriter.append(detailedScorePad.headerRow)
            fileWriter.append("\n")
            fileWriter.append(detailedScorePad.details)
            fileWriter.append("\n\n\n")
        }
        fileWriter.flush()
        fileWriter.close()

        return FileProvider.getUriForFile(
                requireContext().applicationContext,
                requireContext().packageName + ".fileProvider",
                attachment
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.email_scores__title)
    }

    private fun sendButtonListener(messageScoreText: String) {
        fun EmailScoresTextField.getText() = emailScoresViewModel.state.getText(this).trim()

        var uri: Uri? = null
        if (emailScoresViewModel.state.isChecked(EmailScoresCheckbox.FULL_SCORE_SHEET)) {
            try {
                uri = createAttachment(emailScoresViewModel.state.isChecked(EmailScoresCheckbox.DISTANCE_TOTAL))
            }
            // TODO Exception catch is very broad
            catch (e: Exception) {
                if (!e.message.isNullOrBlank()) {
                    CustomLogger.customLogger.e(LOG_TAG, "Email attachment creation error: ${e.message!!}")
                }
                emailScoresViewModel.handle(EmailScoresIntent.OpenError(EmailScoresError.ERROR_CREATING_ATTACHMENT))
                return
            }
        }
        val message = "%s\n\n%s\n\n%s\n\n\n\n%s".format(
                EmailScoresTextField.MESSAGE_HEADER.getText(),
                messageScoreText.trim(),
                EmailScoresTextField.MESSAGE_FOOTER.getText(),
                resources.getString(R.string.email_default_message_signature)
        )
        val emails = EmailScoresTextField.TO.getText()
                .split(",", ";", " ")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

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

        try {
            requireContext().startActivity(emailIntent)
        }
        catch (e: ActivityNotFoundException) {
            emailScoresViewModel.handle(EmailScoresIntent.OpenError(EmailScoresError.NO_EMAIL_APP_FOUND))
        }
    }

    /**
     * @return the archer round given by [args] else all entries where [ViewScoresEntry.isSelected] is true
     */
    private fun getSelectedEntries(): List<ViewScoresEntry> {
        val entries = viewScoresViewModel.state.data
        if (args.archerRoundId >= 0) {
            val entry = entries.find { it.id == args.archerRoundId }
                    ?: throw IllegalArgumentException("Could not find round with ID: ${args.archerRoundId}")
            return listOf(entry)
        }
        return entries.filter { it.isSelected }
    }

    override fun getHelpShowcases() = emailScoresScreen.getHelpShowcases()
    override fun getHelpPriority() = emailScoresScreen.getHelpPriority()
}
