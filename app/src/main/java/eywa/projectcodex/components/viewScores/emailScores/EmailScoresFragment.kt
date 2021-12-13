package eywa.projectcodex.components.viewScores.emailScores

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ActionBarHelp
import eywa.projectcodex.common.utils.getColourResource
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadData
import eywa.projectcodex.components.viewScores.ViewScoresViewModel
import eywa.projectcodex.components.viewScores.data.ViewScoreData
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.exceptions.UserException
import kotlinx.android.synthetic.main.fragment_email_scores.*
import java.io.File
import java.io.FileWriter


class EmailScoresFragment : Fragment(), ActionBarHelp {
    companion object {
        private const val EMAIL_ATTACHMENT_FILENAME = "emailAttachment.csv"
    }

    private val viewScoresViewModel: ViewScoresViewModel by activityViewModels()
    private var allEntries: ViewScoreData? = null
    private var selectedEntryIds: Set<Int>? = null
    private var formHasError = false
    private val endSize = 6

    private val columnHeaderOrder = listOf(
            ScorePadData.ColumnHeader.END_STRING,
            ScorePadData.ColumnHeader.HITS,
            ScorePadData.ColumnHeader.SCORE,
            ScorePadData.ColumnHeader.GOLDS,
            ScorePadData.ColumnHeader.RUNNING_TOTAL
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_email_scores, container, false)
    }

    /**
     * Creates a file in [Context.getExternalFilesDir] with the name [EMAIL_ATTACHMENT_FILENAME]. Writes the data from
     * [getSelectedEntries] to the file
     *
     * @return the created file's [Uri]
     * @throws IllegalStateException on IO error
     * @throws UserException
     */
    private fun createAttachment(): Uri {
        val selectedEntries = getSelectedEntries()!!
        if (selectedEntries.isNullOrEmpty()) {
            // TODO_CURRENT
            throw UserException(1)
        }

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
                    .getDetailsAsCsv(columnHeaderOrder, entry.goldsType, resources)
            fileWriter.append(entry.getScoreSummary(resources))
            fileWriter.append(detailedScorePad.headerRow)
            fileWriter.append(detailedScorePad.details)
            fileWriter.append("\n\n")
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

        viewScoresViewModel.getSelectedArcherIds().observe(viewLifecycleOwner, {
            selectedEntryIds = it
            displaySelectedEntries()
        })
        viewScoresViewModel.viewScoresData.observe(viewLifecycleOwner, {
            allEntries = it
            displaySelectedEntries()
        })

        button_email_scores__send.setOnClickListener {
            try {
                if (formHasError) {
                    // TODO_CURRENT
                    throw UserException(1)
                }

                var uri: Uri? = null
                if (check_box_email_scores__attach_full.isChecked) {
                    uri = createAttachment()
                }
                val message = "%s\n\n%s\n\n%s\n\n\n\n%s".format(
                        input_text_email_scores__message_start.text.trim(),
                        text_email_scores__message_scores.text.trim(),
                        input_text_email_scores__message_end.text.trim(),
                        resources.getString(R.string.email_default_message_signature)
                )
                val emails = input_text_email_scores__to.text
                        .split(",", ";", " ")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                val emailSelectorIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                }
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
                    putExtra(Intent.EXTRA_SUBJECT, input_text_email_scores__subject.text.toString())
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    selector = emailSelectorIntent
                }

                try {
                    requireContext().startActivity(emailIntent)
                }
                catch (e: ActivityNotFoundException) {
                    // TODO_CURRENT - no email app on phone
                    throw UserException(1)
                }
            }
            catch (e: UserException) {
                // TODO_CURRENT Print message
            }
            catch (e: Exception) {
                // TODO_CURRENT Print message and log error
            }
        }
    }

    private fun getSelectedEntries(): List<ViewScoresEntry>? {
        selectedEntryIds?.let { selectedIds ->
            allEntries?.getData()?.let { entries ->
                if (entries.isNotEmpty()) {
                    return entries.filter { selectedIds.contains(it.id) }
                }
            }
        }
        return null
    }

    private fun displaySelectedEntries() {
        val selectedItems = getSelectedEntries()
        if (selectedItems.isNullOrEmpty()) {
            formHasError = true
            text_email_scores__message_scores.text = resources.getString(R.string.email_empty_round_summary)
            text_email_scores__message_scores.setTextColor(
                    getColourResource(resources, R.color.warningText, requireContext().theme)
            )
            return
        }

        text_email_scores__message_scores.setTextColor(
                getColourResource(resources, R.color.offBlack, requireContext().theme)
        )
        text_email_scores__message_scores.text = selectedItems
                .joinToString("\n\n") { entry ->
                    entry.getScoreSummary(resources)
                }
        formHasError = false
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        // TODO_CURRENT
        return listOf()
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
