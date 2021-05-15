package eywa.projectcodex.components.inputEnd.subFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import eywa.projectcodex.R
import eywa.projectcodex.database.arrowValue.ArrowValue
import kotlinx.android.synthetic.main.frag_score_indicator.*


class ScoreIndicatorFragment : Fragment() {
    var onClickListener: View.OnClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_score_indicator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (onClickListener != null) {
            table_layout_scores_indicator.setOnClickListener(onClickListener)
        }
    }

    /**
     * @param arrows the arrows currently added to the round
     */
    fun update(arrows: List<ArrowValue>) {
        view?.let { view ->
            view.findViewById<TextView>(R.id.text_scores_indicator__table_score_1).text =
                    arrows.sumBy { it.score }.toString()
            view.findViewById<TextView>(R.id.text_scores_indicator__table_arrow_count_1).text = arrows.size.toString()
        }
    }
}
