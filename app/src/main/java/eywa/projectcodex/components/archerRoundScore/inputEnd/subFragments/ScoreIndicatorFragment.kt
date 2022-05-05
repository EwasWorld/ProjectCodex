package eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ViewHelpShowcaseItem
import eywa.projectcodex.database.arrowValue.ArrowValue
import kotlinx.android.synthetic.main.frag_score_indicator.*


class ScoreIndicatorFragment : Fragment(), ActionBarHelp {
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
                    arrows.sumOf { it.score }.toString()
            view.findViewById<TextView>(R.id.text_scores_indicator__table_arrow_count_1).text = arrows.size.toString()
        }
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        return listOf(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.table_layout_scores_indicator)
                        .setHelpTitleId(R.string.help_input_end__indicator_title)
                        .setHelpBodyId(R.string.help_input_end__indicator_body)
                        .build()
        )
    }

    override fun getHelpPriority(): Int {
        return -100
    }
}
