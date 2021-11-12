package eywa.projectcodex.components.viewScores

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ActionBarHelp
import eywa.projectcodex.components.viewScores.data.ViewScoreData
import eywa.projectcodex.components.viewScores.listAdapter.ViewScoresAdapter
import eywa.projectcodex.components.viewScores.listAdapter.ViewScoresEntryViewHolder
import kotlinx.android.synthetic.main.fragment_view_scores.*

class ViewScoresFragment : Fragment(), ActionBarHelp {
    companion object {
        private const val LOG_TAG = "ViewScoresFrag"
    }

    private val viewScoresViewModel: ViewScoresViewModel by activityViewModels()

    /**
     * Displayed when there's no information to display in the table
     */
    private val emptyTableDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.err_table_view__no_data)
        builder.setMessage(R.string.err_view_score__no_rounds)
        builder.setPositiveButton(R.string.general_ok) { _, _ ->
            requireView().findNavController().popBackStack()
        }
        builder.setCancelable(false)
        builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_scores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.view_score__title)

        val viewScoresListAdapter = ViewScoresAdapter(viewScoresViewModel)
        recycler_view_scores.adapter = viewScoresListAdapter
        viewScoresViewModel.viewScoresData.observe(viewLifecycleOwner, { it ->
            CustomLogger.customLogger.i(LOG_TAG, "New list")
            viewScoresListAdapter.submitList(it?.getData())
            viewScoresListAdapter.notifyDataSetChanged()

            if (it == null || it.getData().isNullOrEmpty()) {
                emptyTableDialog.show()
            }
            else if (emptyTableDialog.isShowing) {
                emptyTableDialog.dismiss()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        CustomLogger.customLogger.i(LOG_TAG, "Resuming")
        val adapter = recycler_view_scores.adapter as ViewScoresAdapter?
        adapter?.submitList(ViewScoreData.getViewScoreData().getData())
        adapter?.notifyDataSetChanged()
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        val helpShowcases = mutableListOf<ActionBarHelp.HelpShowcaseItem>()
        val recyclerViewLayoutManager = recycler_view_scores.layoutManager as LinearLayoutManager
        val seenItemTypes = mutableSetOf<Int>()
        var priorityOffset = 0
        for (position in recyclerViewLayoutManager.findFirstCompletelyVisibleItemPosition() until recyclerViewLayoutManager.findLastCompletelyVisibleItemPosition()) {
            if (!seenItemTypes.add(recycler_view_scores.adapter!!.getItemViewType(position))) {
                continue
            }
            val showcases =
                    (recycler_view_scores.findViewHolderForAdapterPosition(position) as ViewScoresEntryViewHolder)
                            .getHelpShowcases()
            showcases.forEach {
                if (it.priority == null) {
                    it.priority = priorityOffset
                }
                else {
                    it.priority = it.priority!! + priorityOffset
                }
            }
            helpShowcases.addAll(showcases)
            priorityOffset = showcases.maxOf { it.priority ?: priorityOffset } + 1
        }
        return helpShowcases
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
