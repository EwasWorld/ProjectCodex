package eywa.projectcodex.components.viewRounds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eywa.projectcodex.R
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ViewScoresAdapter : ListAdapter<ViewScoresEntry, ViewScoresAdapter.ViewScoresEntryViewHolder>(
        object : DiffUtil.ItemCallback<ViewScoresEntry>() {
            override fun areItemsTheSame(oldItem: ViewScoresEntry, newItem: ViewScoresEntry): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ViewScoresEntry, newItem: ViewScoresEntry): Boolean {
                return oldItem == newItem
            }
        }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewScoresEntryViewHolder {
        return ViewScoresEntryType.values()[viewType].onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewScoresEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getType().ordinal
    }

    abstract class ViewScoresEntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(viewScoresEntry: ViewScoresEntry)
    }

    enum class ViewScoresEntryType(private val layoutId: Int, private val type: KClass<out ViewScoresEntryViewHolder>) {
        ROUND(R.layout.view_scores_round_item, ViewScoresRoundViewHolder::class);

        fun onCreateViewHolder(parent: ViewGroup): ViewScoresEntryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return type.primaryConstructor!!.call(view)
        }
    }
}