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
        private const val LOG_TAG = "ViewScoresFragment"
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
        setMultiSelectMode(viewScoresViewModel.isInSelectMode)

        val viewScoresListAdapter = ViewScoresAdapter(viewScoresViewModel)
        recycler_view_scores.adapter = viewScoresListAdapter
        viewScoresViewModel.getViewScoreData().observe(viewLifecycleOwner, {
            CustomLogger.customLogger.i(LOG_TAG, "New list")
            viewScoresListAdapter.submitList(it?.getData())
            // TODO_CURRENT Notify specific items
            viewScoresListAdapter.notifyDataSetChanged()

            if (it == null || it.getData().isNullOrEmpty()) {
                emptyTableDialog.show()
            }
            else if (emptyTableDialog.isShowing) {
                emptyTableDialog.dismiss()
            }
        })

        button_view_scores__start_multi_select.setOnClickListener {
            setMultiSelectMode(true)
        }

        button_view_scores__cancel_selection.setOnClickListener {
            setMultiSelectMode(false)
            val changedItems = viewScoresViewModel.setAllSelected(false)
            changedItems.forEach { itemId ->
                viewScoresListAdapter.notifyItemChanged(
                        viewScoresListAdapter.currentList.indexOfFirst { it.id == itemId }
                )
            }
        }

        /*
         * If all items are selected, deselect all items. Else, select all items
         */
        button_view_scores__select_all_or_none.setOnClickListener {
            val allSelected = viewScoresListAdapter.currentList.all { it.isSelected }
            val changedItems = viewScoresViewModel.setAllSelected(!allSelected)
            changedItems.forEach { itemId ->
                viewScoresListAdapter.notifyItemChanged(
                        viewScoresListAdapter.currentList.indexOfFirst { it.id == itemId }
                )
            }
        }

        button_view_scores__selection_action.setOnClickListener {
            view.findNavController().navigate(R.id.emailFragment)
        }
    }

    private fun setMultiSelectMode(isInSelectMode: Boolean) {
        viewScoresViewModel.isInSelectMode = isInSelectMode
        button_view_scores__start_multi_select.visibility = if (isInSelectMode) View.GONE else View.VISIBLE

        val multiSelectItemsVisibility = if (isInSelectMode) View.VISIBLE else View.GONE
        label_view_scores__multi_select.visibility = multiSelectItemsVisibility
        button_view_scores__cancel_selection.visibility = multiSelectItemsVisibility
        button_view_scores__select_all_or_none.visibility = multiSelectItemsVisibility
        button_view_scores__selection_action.visibility = multiSelectItemsVisibility
    }

    override fun onResume() {
        super.onResume()
        CustomLogger.customLogger.i(LOG_TAG, "Resuming")
        val adapter = recycler_view_scores.adapter as ViewScoresAdapter?
        adapter?.submitList(ViewScoreData.getViewScoreData().getData())
        adapter?.notifyDataSetChanged()
        setMultiSelectMode(viewScoresViewModel.isInSelectMode)
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
        if (!viewScoresViewModel.isInSelectMode) {
            helpShowcases.add(
                    ActionBarHelp.HelpShowcaseItem.Builder()
                            .setViewId(R.id.button_view_scores__start_multi_select)
                            .setHelpTitleId(R.string.help_view_score__start_multi_select_title)
                            .setHelpBodyId(R.string.help_view_score__start_multi_select_body)
                            .build()
            )
        }
        else {
            helpShowcases.addAll(
                    listOf(
                            ActionBarHelp.HelpShowcaseItem.Builder()
                                    .setViewId(R.id.button_view_scores__select_all_or_none)
                                    .setHelpTitleId(R.string.help_view_score__select_all_or_none_title)
                                    .setHelpBodyId(R.string.help_view_score__select_all_or_none_body)
                                    .build(),
                            ActionBarHelp.HelpShowcaseItem.Builder()
                                    .setViewId(R.id.button_view_scores__selection_action)
                                    .setHelpTitleId(R.string.help_view_score__action_multi_select_title)
                                    .setHelpBodyId(R.string.help_view_score__action_multi_select_body)
                                    .build(),
                            ActionBarHelp.HelpShowcaseItem.Builder()
                                    .setViewId(R.id.button_view_scores__cancel_selection)
                                    .setHelpTitleId(R.string.help_view_score__cancel_multi_select_title)
                                    .setHelpBodyId(R.string.help_view_score__cancel_multi_select_body)
                                    .build()
                    )
            )
        }
        return helpShowcases
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
