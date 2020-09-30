package eywa.projectcodex.ui.inputEnd

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import eywa.projectcodex.R
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_input_end.*


class ArrowInputsFragment10ZoneWithX : Fragment() {
    private var listener: ScoreButtonPressedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_arrow_inputs_10_zone_with_x, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO better way to do this? Tags?
        val scoreButtons = arrayOf(
                button_input_end__score_0,
                button_input_end__score_1,
                button_input_end__score_2,
                button_input_end__score_3,
                button_input_end__score_4,
                button_input_end__score_5,
                button_input_end__score_6,
                button_input_end__score_7,
                button_input_end__score_8,
                button_input_end__score_9,
                button_input_end__score_10,
                button_input_end__score_x
        )
        for (button in scoreButtons) {
            button.setOnClickListener {
                listener?.onScoreButtonPressed(view.findViewById<Button>(button.id).text.toString())
            }
        }
    }

    interface ScoreButtonPressedListener {
        fun onScoreButtonPressed(score: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        for (fragment in (context as FragmentActivity).nav_host_fragment.childFragmentManager.fragments) {
            if (fragment is ScoreButtonPressedListener) {
                listener = fragment
                return
            }
        }
        throw ClassCastException("$context must implement ScoreButtonPressedListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}