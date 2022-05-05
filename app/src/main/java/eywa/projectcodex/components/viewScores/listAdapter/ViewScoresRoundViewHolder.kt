package eywa.projectcodex.components.viewScores.listAdapter

import android.view.View
import android.widget.TextView
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ActionBarHelp
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.ViewScoresFragmentDirections
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import kotlin.math.roundToInt

/**
 * Container to display a [ViewScoresEntry] of type [ViewScoresAdapter.ViewScoresEntryType.ROUND] in a recycler view
 */
class ViewScoresRoundViewHolder(view: View) : ViewScoresEntryViewHolder(view) {
    private val dateView = view.findViewById<TextView>(R.id.text_vs_round_item__date)
    private val roundView = view.findViewById<TextView>(R.id.text_vs_round_item__round)
    private val hsgView = view.findViewById<TextView>(R.id.text_vs_round_item__hsg)
    private val hsgLabel = view.findViewById<TextView>(R.id.label_vs_round_item__hsg)
    private val handicapView = view.findViewById<TextView>(R.id.text_vs_round_item__handicap)

    init {
        view.setOnCreateContextMenuListener(this)
        handicapView.minWidth = handicapView.paddingLeft + handicapView.paddingRight +
                handicapView.paint.measureText("00").roundToInt()
    }

    override fun bind(viewScoresEntry: ViewScoresEntry) {
        super.bind(viewScoresEntry)

        val listener = object : ViewScoresEntry.UpdatedListener {
            override fun onUpdate() {
                dateView.text = DateTimeFormat.SHORT_DATE_TIME_FORMAT.format(viewScoresEntry.archerRound.dateShot)
                roundView.text =
                        viewScoresEntry.displayName ?: itemView.resources.getString(R.string.create_round__no_round)
                hsgView.text = viewScoresEntry.hitsScoreGolds
                hsgLabel.text = "%s/%s/%s".format(
                        itemView.resources.getString(R.string.table_hits_header),
                        itemView.resources.getString(R.string.table_score_header),
                        itemView.resources.getString(viewScoresEntry.goldsType.shortStringId),
                )
                handicapView.text = viewScoresEntry.handicap?.toString() ?: "-"
                itemView.isSelected = viewScoresEntry.isSelected
            }
        }
        viewScoresEntry.updatedListener = listener
        listener.onUpdate()
    }

    override fun onClick(): Boolean {
        if (super.onClick()) return true

        itemView.findNavController().navigate(
                ViewScoresFragmentDirections.actionViewScoresFragmentToScorePadFragment(entry!!.id)
        )
        return true
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        return listOf(
                ActionBarHelp.HelpShowcaseItem.Builder()
                        .setView(hsgView)
                        .setHelpTitleId(R.string.help_view_score__hsg_title)
                        .setHelpBodyId(R.string.help_view_score__hsg_body)
                        .build(),
                ActionBarHelp.HelpShowcaseItem.Builder()
                        .setView(handicapView)
                        .setHelpTitleId(R.string.help_view_score__handicap_title)
                        .setHelpBodyId(R.string.help_view_score__handicap_body)
                        .setShapePadding(40)
                        .build()
        )
    }
}