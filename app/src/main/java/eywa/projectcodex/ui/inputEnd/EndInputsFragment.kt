package eywa.projectcodex.ui.inputEnd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import eywa.projectcodex.End
import eywa.projectcodex.R
import kotlinx.android.synthetic.main.frag_end_inputs.*


class EndInputsFragment : Fragment(), ArrowInputsFragment10ZoneWithX.ScoreButtonPressedListener {
    // This assignment should always be overwritten, just can't have a lateinit with a custom setter :rolling_eyes:
    var end: End = End(6, ".", "-")
        set(value) {
            field = value
            updateEndStringAndTotal()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_end_inputs, container, false)!!
        end = End(
                6, getString(R.string.end_to_string_arrow_placeholder),
                getString(R.string.end_to_string_arrow_deliminator)
        )
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
    }

    fun clearEnd() {
        end.clear()
        updateEndStringAndTotal()
    }

    fun showResetButton(value: Boolean) {
        view?.findViewById<Button>(R.id.button_end_inputs__reset)?.visibility = if (value) View.VISIBLE else View.GONE
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

    private fun updateEndStringAndTotal() {
        view?.let { view ->
            view.findViewById<TextView>(R.id.text_end_inputs__inputted_arrows).text = end.toString()
            view.findViewById<TextView>(R.id.text_end_inputs__end_total).text = end.getScore().toString()
        }
    }
}
