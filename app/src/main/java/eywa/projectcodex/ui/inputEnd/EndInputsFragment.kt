package eywa.projectcodex.ui.inputEnd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import eywa.projectcodex.R
import eywa.projectcodex.exceptions.UserException
import eywa.projectcodex.logic.End
import eywa.projectcodex.ui.commonElements.NumberPickerDialog
import kotlinx.android.synthetic.main.frag_end_inputs.*


class EndInputsFragment : Fragment(), ArrowInputsFragment10ZoneWithX.ScoreButtonPressedListener {
    var usualEndSize: Int? = null
    var remainingArrows: Int? = null
        set(value) {
            val original = field
            field = value
            // Update the endSize in case this should be used instead
            if (value != original) endSize = endSize
        }
    var showResetButton = false

    /**
     * @throws UserException in setter
     * @see End.updateEndSize
     */
    var endSize = 6
        set(value) {
            val useRemaining = remainingArrows != null && remainingArrows!! < value
            val newEndSize: Int
            when {
                useRemaining -> {
                    usualEndSize = field
                    newEndSize = remainingArrows!!
                }
                usualEndSize != null -> {
                    newEndSize = usualEndSize!!
                    usualEndSize = null
                }
                else -> newEndSize = value
            }
            end.updateEndSize(newEndSize, true)
            field = newEndSize
            updateEndStringAndTotal()
        }

    // This assignment will always be overwritten, just can't have a lateinit with a custom setter :rolling_eyes:
    var end: End =
            End(endSize, ".", "-")
        set(value) {
            field = value
            updateEndStringAndTotal()
        }

    /**
     * Lazy loaded
     */
    private var numberPickerDialog: NumberPickerDialog? = null
        get() {
            if (field != null) {
                return field
            }

            val okListener = object : NumberPickerDialog.OnSelectListener {
                override fun onSelect(value: Int) {
                    try {
                        endSize = value
                    }
                    catch (e: UserException) {
                        Toast.makeText(context, e.getMessage(resources), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            numberPickerDialog = NumberPickerDialog(
                    resources.getString(R.string.input_end__change_end_size_dialog_title),
                    null, 12, 2, endSize, okListener
            )
            @Suppress("RecursivePropertyAccessor")
            return numberPickerDialog
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_end_inputs, container, false)!!
        end = End(
                endSize,
                getString(R.string.end_to_string_arrow_placeholder),
                getString(R.string.end_to_string_arrow_deliminator)
        )
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
                Toast.makeText(context, getString(R.string.err_input_end__end_empty), Toast.LENGTH_SHORT).show()
            }
        }
        button_end_inputs__reset.setOnClickListener {
            end.reset()
            updateEndStringAndTotal()
        }
        text_end_inputs__inputted_arrows.setOnClickListener {
            if (end.isEditEnd()) {
                Toast.makeText(context, getString(R.string.err_input_end__cannot_edit_end_size), Toast.LENGTH_SHORT)
                        .show()
                return@setOnClickListener
            }
            numberPickerDialog!!.show(childFragmentManager, "time picker")
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
            Toast.makeText(context, getString(R.string.err_input_end__end_full), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEndStringAndTotal(view: View? = this.view) {
        view?.let { requiredView ->
            requiredView.findViewById<TextView>(R.id.text_end_inputs__inputted_arrows).text = end.toString()
            requiredView.findViewById<TextView>(R.id.text_end_inputs__end_total).text = end.getScore().toString()
        }
    }
}
