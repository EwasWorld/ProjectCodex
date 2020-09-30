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


class EndInputsFragment : Fragment(), ArrowInputsFragment10ZoneWithX.ScoreButtonPressedListener {
    private lateinit var end: End

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_end_inputs, container, false)
        if (!view.isInEditMode) {
            view.findViewById<View>(R.id.fragment_arrow_inputs_preview).visibility = View.GONE
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.input_end__title)

        end = End(
                6, getString(R.string.end_to_string_arrow_placeholder),
                getString(R.string.end_to_string_arrow_deliminator)
        )
    }

    override fun onScoreButtonPressed(score: String) {
        try {
            end.addArrowToEnd(score)
            view?.let { view ->
                view.findViewById<TextView>(R.id.text_input_end__inputted_arrows).text = end.toString()
                view.findViewById<TextView>(R.id.text_input_end__end_total).text = end.getScore().toString()
            }
        }
        catch (e: IllegalStateException) {
            Toast.makeText(context, getString(R.string.err_input_end__end_full), Toast.LENGTH_SHORT).show()
        }
    }
}
