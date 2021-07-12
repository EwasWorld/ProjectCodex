package eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import eywa.projectcodex.R
import eywa.projectcodex.components.archeryObjects.End
import eywa.projectcodex.components.commonElements.NumberPickerDialog
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.ToastSpamPrevention
import eywa.projectcodex.exceptions.UserException
import kotlinx.android.synthetic.main.frag_end_inputs.*


class EndInputsFragment : Fragment(), ArrowInputsFragment10ZoneWithX.ScoreButtonPressedListener, ActionBarHelp {
    private val defaultStartingEndSize = 6
    var showResetButton = false

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
        view.findViewById<Button>(R.id.button_end_inputs__reset).visibility =
                if (showResetButton) View.VISIBLE else View.GONE
        updateEndStringAndTotal(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_end_inputs__clear.setOnClickListener {
            clearEnd()
        }
        button_end_inputs__backspace.setOnClickListener {
            try {
                end.removeLastArrowFromEnd()
                updateEndStringAndTotal()
            }
            catch (e: IllegalStateException) {
                ToastSpamPrevention.displayToast(requireContext(), getString(R.string.err_input_end__end_empty))
            }
        }
        button_end_inputs__reset.setOnClickListener {
            end.reset()
            updateEndStringAndTotal()
        }
        text_end_inputs__inputted_arrows.setOnClickListener {
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

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        val helpItems = mutableListOf(
                ActionBarHelp.HelpShowcaseItem(
                        R.id.fragment_end_inputs__arrow_inputs,
                        getString(R.string.help_input_end__arrow_inputs_title),
                        getString(R.string.help_input_end__arrow_inputs_body),
                        shape = ActionBarHelp.ShowcaseShape.OVAL
                ),
                ActionBarHelp.HelpShowcaseItem(
                        R.id.text_end_inputs__inputted_arrows,
                        getString(R.string.help_input_end__end_inputs_arrows_title),
                        getString(R.string.help_input_end__end_inputs_arrows_body),
                        shape = ActionBarHelp.ShowcaseShape.OVAL
                ),
                ActionBarHelp.HelpShowcaseItem(
                        R.id.text_end_inputs__end_total,
                        getString(R.string.help_input_end__end_inputs_total_title),
                        getString(R.string.help_input_end__end_inputs_total_body),
                        shape = ActionBarHelp.ShowcaseShape.CIRCLE,
                        shapePadding = 85
                ),
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_end_inputs__backspace,
                        getString(R.string.help_input_end__end_inputs_backspace_title),
                        getString(R.string.help_input_end__end_inputs_backspace_body)
                ),
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_end_inputs__clear,
                        getString(R.string.help_input_end__end_inputs_clear_title),
                        getString(R.string.help_input_end__end_inputs_clear_body)
                )
        )
        if (showResetButton) {
            helpItems.add(
                    ActionBarHelp.HelpShowcaseItem(
                            R.id.button_end_inputs__reset,
                            getString(R.string.help_input_end__end_inputs_reset_title),
                            getString(R.string.help_input_end__end_inputs_reset_body)
                    )
            )
        }
        return helpItems
    }

    override fun getHelpPriority(): Int {
        return -2
    }
}
