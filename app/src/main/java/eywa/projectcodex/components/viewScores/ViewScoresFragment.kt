package eywa.projectcodex.components.viewScores

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ActionBarHelp
import eywa.projectcodex.components.viewScores.data.ViewScoreData
import eywa.projectcodex.components.viewScores.listAdapter.ViewScoresAdapter
import eywa.projectcodex.components.viewScores.listAdapter.ViewScoresEntryViewHolder
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlinx.android.synthetic.main.fragment_view_scores.*
import javax.inject.Inject

open class ViewScoresFragment : Fragment(), ActionBarHelp {
    companion object {
        private const val LOG_TAG = "ViewScoresFrag"
    }

    @Inject
    lateinit var viewScoresViewModel: ViewScoresViewModel

    /*
     * All data from certain tables in the database
     */
    private var allArrows: List<ArrowValue>? = null
    private var allArcherRoundsWithNames: List<ArcherRoundWithRoundInfoAndName>? = null
    private var allArrowCounts: List<RoundArrowCount>? = null
    private var allDistances: List<RoundDistance>? = null

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

        val viewScoreData = ViewScoreData.getViewScoreData()
        val viewScoresListAdapter = ViewScoresAdapter(viewScoresViewModel)
        recycler_view_scores.adapter = viewScoresListAdapter
        viewScoresViewModel.allArrows.observe(viewLifecycleOwner, { arrows ->
            arrows?.let {
                allArrows = arrows
                viewScoreData.updateArrows(arrows)
            }
        })
        viewScoresViewModel.allArcherRounds.observe(viewLifecycleOwner, { archerRounds ->
            archerRounds?.let {
                allArcherRoundsWithNames = archerRounds
                val updateList = viewScoreData.updateArcherRounds(archerRounds)

                // Ensure that if other fields were populated before this one, they're updated too
                if (allArrows != null) {
                    viewScoreData.updateArrows(allArrows!!)
                }
                if (allArrowCounts != null) {
                    viewScoreData.updateArrowCounts(allArrowCounts!!)
                }
                if (allDistances != null) {
                    viewScoreData.updateDistances(allDistances!!)
                }

                if (updateList) {
                    CustomLogger.customLogger.i(LOG_TAG, "New list")
                    viewScoresListAdapter.submitList(viewScoreData.getData())
                }
                if (viewScoreData.getData().isEmpty()) {
                    emptyTableDialog.show()
                }
                else if (emptyTableDialog.isShowing) {
                    emptyTableDialog.dismiss()
                }
            }
        })
        viewScoresViewModel.allArrowCounts.observe(viewLifecycleOwner, { arrowCounts ->
            arrowCounts?.let {
                allArrowCounts = arrowCounts
                viewScoreData.updateArrowCounts(arrowCounts)
            }
        })
        viewScoresViewModel.allDistances.observe(viewLifecycleOwner, { distances ->
            distances?.let {
                allDistances = distances
                viewScoreData.updateDistances(distances)
            }
        })
    }

    override fun onAttach(context: Context) {
        injectMembers()
        super.onAttach(context)
    }

    protected open fun injectMembers() = AndroidSupportInjection.inject(this)

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
