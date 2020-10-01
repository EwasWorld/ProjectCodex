package eywa.projectcodex.ui.inputEnd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import eywa.projectcodex.End
import eywa.projectcodex.R
import kotlinx.android.synthetic.main.frag_end_inputs.*


class EndInputsFragment : Fragment(), ArrowInputsFragment10ZoneWithX.ScoreButtonPressedListener {
    private lateinit var end: End

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_end_inputs, container, false)!!
        if (!view.isInEditMode) {
            view.findViewById<View>(R.id.fragment_end_inputs__arrow_inputs_preview).visibility = View.GONE
        }

        end = End(
                6, getString(R.string.end_to_string_arrow_placeholder),
                getString(R.string.end_to_string_arrow_deliminator)
        )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateEndStringAndTotal()

        button_end_inputs__clear.setOnClickListener {
            end.clear()
            updateEndStringAndTotal()
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
