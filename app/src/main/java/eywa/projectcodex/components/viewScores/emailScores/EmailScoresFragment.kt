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
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ViewHelpShowcaseItem
import eywa.projectcodex.common.utils.ToastSpamPrevention
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
        private const val LOG_TAG = "EmailScoresFragment"
    }

    private val args: EmailScoresFragmentArgs by navArgs()
    private val viewScoresViewModel: ViewScoresViewModel by activityViewModels()
    private var allEntries: ViewScoreData? = null
    private val formErrors = FormErrors()
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
    private fun createAttachment(includeDistanceHeaders: Boolean): Uri {
        val selectedEntries = getSelectedEntries()!!
        if (selectedEntries.isNullOrEmpty()) {
            throw UserException(R.string.err_email_scores__no_items_selected)
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

        viewScoresViewModel.getViewScoreData().observe(viewLifecycleOwner, {
            allEntries = it
            displaySelectedEntries()
        })

        button_email_scores__send.setOnClickListener {
            try {
                formErrors.throwFirstFormErrorAsUserException()

                var uri: Uri? = null
                if (check_box_email_scores__attach_full.isChecked) {
                    uri = createAttachment(check_box_email_scores__include_distance.isChecked)
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
                    throw UserException(R.string.err_email_scores__no_app_found)
                }
            }
            catch (e: UserException) {
                ToastSpamPrevention.displayToast(requireContext(), e.getUserMessage(resources))
            }
            catch (e: Exception) {
                if (!e.message.isNullOrBlank()) CustomLogger.customLogger.e(LOG_TAG, e.message!!)
                ToastSpamPrevention.displayToast(requireContext(), getString(R.string.err__internal_error))
            }
        }

        check_box_email_scores__attach_full.setOnCheckedChangeListener { _, isChecked ->
            check_box_email_scores__include_distance.isEnabled = isChecked
        }
    }

    /**
     * @return the archer round given by [args] else all entries where [ViewScoresEntry.isSelected] is true
     */
    private fun getSelectedEntries(): List<ViewScoresEntry>? {
        allEntries?.getData()?.let { entries ->
            if (args.archerRoundId >= 0) {
                val entry = entries.find { it.id == args.archerRoundId }
                        ?: throw IllegalArgumentException("Could not find round with ID: ${args.archerRoundId}")
                return listOf(entry)
            }
            return entries.filter { it.isSelected }
        }
        return null
    }

    private fun displaySelectedEntries() {
        val selectedItems = getSelectedEntries()
        if (selectedItems.isNullOrEmpty()) {
            formErrors.addFormError(R.string.err_email_scores__no_items_selected)
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
        formErrors.removeFormError(R.string.err_email_scores__no_items_selected)
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        return listOf(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.input_text_email_scores__to)
                        .setHelpTitleId(R.string.help_email_scores__to_title)
                        .setHelpBodyId(R.string.help_email_scores__to_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.input_text_email_scores__subject)
                        .setHelpTitleId(R.string.help_email_scores__subject_title)
                        .setHelpBodyId(R.string.help_email_scores__subject_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.input_text_email_scores__message_start)
                        .setHelpTitleId(R.string.help_email_scores__message_start_title)
                        .setHelpBodyId(R.string.help_email_scores__message_start_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.text_email_scores__message_scores)
                        .setHelpTitleId(R.string.help_email_scores__scores_title)
                        .setHelpBodyId(R.string.help_email_scores__scores_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.input_text_email_scores__message_end)
                        .setHelpTitleId(R.string.help_email_scores__message_end_title)
                        .setHelpBodyId(R.string.help_email_scores__message_end_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.check_box_email_scores__attach_full)
                        .setHelpTitleId(R.string.help_email_scores__full_score_sheet_attachment_title)
                        .setHelpBodyId(R.string.help_email_scores__full_score_sheet_attachment_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.check_box_email_scores__include_distance)
                        .setHelpTitleId(R.string.help_email_scores__include_distance_totals_title)
                        .setHelpBodyId(R.string.help_email_scores__include_distance_totals_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_email_scores__send)
                        .setHelpTitleId(R.string.help_email_scores__send_title)
                        .setHelpBodyId(R.string.help_email_scores__send_body)
                        .build()
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }

    private class FormErrors {
        private val formErrorResourceIds = mutableSetOf<Int>()

        fun addFormError(resourceId: Int) {
            formErrorResourceIds.add(resourceId)
        }

        fun removeFormError(resourceId: Int) {
            formErrorResourceIds.remove(resourceId)
        }

        /**
         * If there are any form errors, throw the first as a [UserException], else do nothing
         */
        fun throwFirstFormErrorAsUserException() {
            if (formErrorResourceIds.isNullOrEmpty()) {
                return
            }
            throw UserException(formErrorResourceIds.first())
        }
    }
}
