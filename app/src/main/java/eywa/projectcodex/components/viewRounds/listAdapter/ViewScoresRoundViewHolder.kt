package eywa.projectcodex.components.viewRounds.listAdapter

import android.view.View
import android.widget.TextView
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.DateTimeFormat
import eywa.projectcodex.components.viewRounds.data.ViewScoresEntry
import kotlin.math.roundToInt

class ViewScoresRoundViewHolder(view: View) : ViewScoresEntryViewHolder(view) {
    private val dateView = view.findViewById<TextView>(R.id.text_vs_round_item__date)
    private val roundView = view.findViewById<TextView>(R.id.text_vs_round_item__round)
    private val hsgView = view.findViewById<TextView>(R.id.text_vs_round_item__hsg)
    private val handicapView = view.findViewById<TextView>(R.id.text_vs_round_item__handicap)

    init {
        view.setOnCreateContextMenuListener(this)
        handicapView.minWidth = handicapView.paddingLeft + handicapView.paddingRight +
                handicapView.paint.measureText("00").roundToInt()
    }

    override fun bind(viewScoresEntry: ViewScoresEntry) {
        entry = viewScoresEntry
        val listener = object : ViewScoresEntry.UpdatedListener {
            override fun onUpdate() {
                dateView.text = DateTimeFormat.SHORT_DATE_TIME_FORMAT.format(viewScoresEntry.archerRound.dateShot)
                roundView.text =
                        viewScoresEntry.displayName ?: itemView.resources.getString(R.string.create_round__no_round)
                hsgView.text = viewScoresEntry.hitsScoreGolds ?: "-/-/-"
                handicapView.text = viewScoresEntry.handicap?.toString() ?: "-"
            }
        }
        viewScoresEntry.updatedListener = listener
        listener.onUpdate()
    }
}