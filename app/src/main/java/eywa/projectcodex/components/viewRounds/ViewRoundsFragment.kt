package eywa.projectcodex.components.viewRounds

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.resourceStringReplace
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlinx.android.synthetic.main.fragment_view_scores.*

class ViewRoundsFragment : Fragment(), ActionBarHelp {
    companion object {
        private const val LOG_TAG = "ViewRoundsFrag"
    }

    private lateinit var viewRoundsViewModel: ViewRoundsViewModel

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
        builder.setMessage(R.string.err_view_round__no_rounds)
        builder.setPositiveButton(R.string.general_ok) { _, _ ->
            requireView().findNavController().popBackStack()
        }
        builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_scores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.view_round__title)
        viewRoundsViewModel = ViewModelProvider(this).get(ViewRoundsViewModel::class.java)

        val viewScoreData = ViewScoreData.getViewScoreData()
        val viewScoresListAdapter = ViewScoresAdapter(viewRoundsViewModel)
        recycler_view_scores.adapter = viewScoresListAdapter
        registerForContextMenu(view.findViewById(R.id.recycler_view_scores))
        viewRoundsViewModel.allArrows.observe(viewLifecycleOwner, { arrows ->
            arrows?.let {
                allArrows = arrows
                viewScoreData.updateArrows(arrows)
            }
        })
        viewRoundsViewModel.allArcherRounds.observe(viewLifecycleOwner, { archerRounds ->
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
                // TODO Recognise no entries
            }
        })
        viewRoundsViewModel.allArrowCounts.observe(viewLifecycleOwner, { arrowCounts ->
            arrowCounts?.let {
                allArrowCounts = arrowCounts
                viewScoreData.updateArrowCounts(arrowCounts)
            }
        })
        viewRoundsViewModel.allDistances.observe(viewLifecycleOwner, { distances ->
            distances?.let {
                allDistances = distances
                viewScoreData.updateDistances(distances)
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
        return listOf(
                ActionBarHelp.HelpShowcaseItem(
                        null,
                        getString(R.string.help_view_round__main_title),
                        resourceStringReplace(
                                getString(R.string.help_view_round__main_body),
                                mapOf(Pair("edit help", getString(R.string.help_table_open_menu_body)))
                        )
                )
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
