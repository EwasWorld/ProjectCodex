package eywa.projectcodex

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eywa.projectcodex.database.entities.ArrowValue

class ScoreListAdapter internal constructor(context: Context?) :
        RecyclerView.Adapter<ScoreListAdapter.ArrowViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var arrowValues = emptyList<ArrowValue>() // Cached copy of words

    inner class ArrowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val arrowValueItemView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArrowViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return ArrowViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArrowViewHolder, position: Int) {
        val current = arrowValues[position]
        holder.arrowValueItemView.text = current.score.toString()
    }

    internal fun setArrows(arrows: List<ArrowValue>) {
        this.arrowValues = arrows
        notifyDataSetChanged()
    }

    override fun getItemCount() = arrowValues.size
}