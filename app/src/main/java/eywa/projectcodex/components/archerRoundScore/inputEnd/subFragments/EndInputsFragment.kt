package eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.End
import eywa.projectcodex.common.elements.NumberPickerDialog
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ViewHelpShowcaseItem
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.archerRoundScore.inputEnd.ScoreButtonPressedListener
import eywa.projectcodex.exceptions.UserException


class EndInputsFragment : Fragment(), ScoreButtonPressedListener, ActionBarHelp {
    private val defaultStartingEndSize = 6
    var showResetButton = false
    private var currentScoreButtonsType: ArrowInputsFragment.ArrowInputsType? = null

    // This assignment will always be overwritten, just can't have a lateinit with a custom setter :rolling_eyes:
    var end: End = End(defaultStartingEndSize, ".", "-")
        set(value) {
            field = value
            updateEndStringAndTotal()
        }

    private val numberPickerDialog: NumberPickerDialog by lazy {
        val okListener = object : NumberPickerDialog.OnOkListener {
            override fun onSelect(value: Int) {
                try {
                    end.updateEndSize(value)
                }
                catch (e: UserException) {
                    ToastSpamPrevention.displayToast(requireContext(), e.getUserMessage(resources))
                }
            }
        }
        NumberPickerDialog(
                resources.getString(R.string.input_end__change_end_size_dialog_title),
                null, 12, 2, end.endSize, okListener
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.frag_end_inputs, container, false)!!
        end = End(
                defaultStartingEndSize,
                getString(R.string.end_to_string_arrow_placeholder),
                getString(R.string.end_to_string_arrow_deliminator)
        )
        end.updateEndSizeListener = object : End.UpdateEndSizeListener {
            override fun onEndSizeUpdated() {
                updateEndStringAndTotal(view)
            }
        }
        setScoreButtons()
        view.findViewById<Button>(R.id.button_end_inputs__reset).visibility =
                if (showResetButton) View.VISIBLE else View.GONE
        updateEndStringAndTotal(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_end_inputs__clear).setOnClickListener {
            clearEnd()
        }
        view.findViewById<Button>(R.id.button_end_inputs__backspace).setOnClickListener {
            try {
                end.removeLastArrowFromEnd()
                updateEndStringAndTotal()
            }
            catch (e: IllegalStateException) {
                ToastSpamPrevention.displayToast(requireContext(), getString(R.string.err_input_end__end_empty))
            }
        }
        view.findViewById<Button>(R.id.button_end_inputs__reset).setOnClickListener {
            end.reset()
            updateEndStringAndTotal()
        }
        view.findViewById<TextView>(R.id.text_end_inputs__inputted_arrows).setOnClickListener {
            if (end.isEditEnd()) {
                ToastSpamPrevention.displayToast(
                        requireContext(),
                        getString(R.string.err_input_end__cannot_edit_end_size)
                )
                return@setOnClickListener
            }
            numberPickerDialog.show(childFragmentManager, "end size picker")
        }
    }

    fun clearEnd() {
        end.clear()
        updateEndStringAndTotal()
    }

    fun setScoreButtons(
            type: ArrowInputsFragment.ArrowInputsType = ArrowInputsFragment.ArrowInputsType.TEN_ZONE_WITH_X
    ) {
        if (currentScoreButtonsType != type) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_end_inputs__arrow_inputs, ArrowInputsFragment.newInstance(type))
                    .commit()
            currentScoreButtonsType = type
        }
    }

    override fun onScoreButtonPressed(score: String) {
        try {
            end.addArrowToEnd(score)
            updateEndStringAndTotal()
        }
        catch (e: IllegalStateException) {
            ToastSpamPrevention.displayToast(requireContext(), getString(R.string.err_input_end__end_full))
        }
    }

    private fun updateEndStringAndTotal(view: View? = this.view) {
        view?.let { requiredView ->
            requiredView.findViewById<TextView>(R.id.text_end_inputs__inputted_arrows).text = end.toString()
            requiredView.findViewById<TextView>(R.id.text_end_inputs__end_total).text = end.getScore().toString()
        }
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        val helpItems = mutableListOf(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.fragment_end_inputs__arrow_inputs)
                        .setHelpTitleId(R.string.help_input_end__arrow_inputs_title)
                        .setHelpBodyId(R.string.help_input_end__arrow_inputs_body)
                        .setShape(HelpShowcaseItem.Shape.OVAL)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.text_end_inputs__inputted_arrows)
                        .setHelpTitleId(R.string.help_input_end__end_inputs_arrows_title)
                        .setHelpBodyId(R.string.help_input_end__end_inputs_arrows_body)
                        .setShape(HelpShowcaseItem.Shape.OVAL)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.text_end_inputs__end_total)
                        .setHelpTitleId(R.string.help_input_end__end_inputs_total_title)
                        .setHelpBodyId(R.string.help_input_end__end_inputs_total_body)
                        .setShape(HelpShowcaseItem.Shape.CIRCLE)
                        .setShapePadding(85)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_end_inputs__backspace)
                        .setHelpTitleId(R.string.help_input_end__end_inputs_backspace_title)
                        .setHelpBodyId(R.string.help_input_end__end_inputs_backspace_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_end_inputs__clear)
                        .setHelpTitleId(R.string.help_input_end__end_inputs_clear_title)
                        .setHelpBodyId(R.string.help_input_end__end_inputs_clear_body)
                        .build()
        )
        if (showResetButton) {
            helpItems.add(
                    ViewHelpShowcaseItem.Builder()
                            .setViewId(R.id.button_end_inputs__reset)
                            .setHelpTitleId(R.string.help_input_end__end_inputs_reset_title)
                            .setHelpBodyId(R.string.help_input_end__end_inputs_reset_body)
                            .build()
            )
        }
        return helpItems
    }

    override fun getHelpPriority(): Int {
        return -2
    }
}
