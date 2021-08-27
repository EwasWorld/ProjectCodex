package eywa.projectcodex.components.viewScores.listAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import eywa.projectcodex.R
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.components.viewScores.ViewScoresViewModel
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Used to display data in [ViewScoresFragment]'s recycler view
 */
class ViewScoresAdapter(private val viewModel: ViewScoresViewModel) :
        ListAdapter<ViewScoresEntry, ViewScoresEntryViewHolder>(
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
        val holder = ViewScoresEntryType.values()[viewType].onCreateViewHolder(parent)
        holder.viewModel = viewModel
        return holder
    }

    override fun onBindViewHolder(holder: ViewScoresEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getType().ordinal
    }

    enum class ViewScoresEntryType(
            private val layoutId: Int,
            private val type: KClass<out ViewScoresEntryViewHolder>
    ) {
        ROUND(R.layout.view_scores_round_item, ViewScoresRoundViewHolder::class);

        fun onCreateViewHolder(parent: ViewGroup): ViewScoresEntryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return type.primaryConstructor!!.call(view)
        }
    }
}