package eywa.projectcodex.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.getGoldsType
import eywa.projectcodex.infoTable.*
import eywa.projectcodex.viewModels.ScorePadViewModel
import eywa.projectcodex.viewModels.ViewModelFactory
import kotlin.math.ceil

class ScorePadFragment : Fragment() {
    private val args: ScorePadFragmentArgs by navArgs()
    private lateinit var scorePadViewModel: ScorePadViewModel
    private var goldsType = GoldsType.TENS
    private var arrowCounts = listOf<RoundArrowCount>()
    private var distances = listOf<RoundDistance>()
    private var distanceUnit: String? = null
    private var arrows = listOf<ArrowValue>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_score_pad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.score_pad__title)

        val tableAdapter = InfoTableViewAdapter(context!!)
        val tableView = view.findViewById<TableView>(R.id.table_view_score_pad)
        tableView.adapter = tableAdapter
        tableView.tableViewListener = ScorePadTableViewListener(tableView)

        scorePadViewModel = ViewModelProvider(this, ViewModelFactory {
            ScorePadViewModel(activity!!.application, args.archerRoundId)
        }).get(ScorePadViewModel::class.java)

        // Get arrow counts and distances
        scorePadViewModel.archerRound.observe(viewLifecycleOwner, Observer { ar ->
            ar?.let { archerRound ->
                archerRound.roundId?.let { roundId ->
                    scorePadViewModel.getArrowCountsForRound(roundId).observe(viewLifecycleOwner, Observer {
                        arrowCounts = it
                        updateTable(tableAdapter)
                    })
                    scorePadViewModel.getDistancesForRound(roundId, archerRound.roundSubTypeId)
                            .observe(viewLifecycleOwner, Observer {
                                distances = it
                                updateTable(tableAdapter)
                            })
                }
            }
        })

        // Get golds type and distance unit
        scorePadViewModel.roundInfo.observe(viewLifecycleOwner, Observer {
            it?.let { round ->
                goldsType = getGoldsType(round.isOutdoor, round.isMetric)
                distanceUnit =
                        getString(if (round.isMetric) R.string.units_meters_short else R.string.units_yards_short)
            }
            updateTable(tableAdapter)
        })

        // Get arrows
        scorePadViewModel.arrowsForRound.observe(viewLifecycleOwner, Observer {
            if (it != null) arrows = it
            updateTable(tableAdapter)
        })
    }

    /**
     * Does nothing if the requirements to make a valid table are not met
     */
    private fun updateTable(tableAdapter: InfoTableViewAdapter) {
        if (arrows.isNullOrEmpty() || arrowCounts.size != distances.size
            || (arrowCounts.isNotEmpty() && distanceUnit == null)) {
            return
        }

        try {
            val tableData = calculateScorePadTableData(
                    arrows, args.endSize, goldsType, resources, arrowCounts, distances, distanceUnit
            )
            val arrowRowsShot = ceil(arrows.size / args.endSize.toDouble()).toInt()
            tableAdapter.setAllItems(
                    getColumnHeadersForTable(scorePadColumnHeaderIds, resources, goldsType),
                    generateNumberedRowHeaders(
                            if (arrowCounts.isNotEmpty()) {
                                arrowCounts.map { ceil(it.arrowCount / args.endSize.toDouble()).toInt() }
                            }
                            else {
                                listOf(arrowRowsShot)
                            },
                            arrowRowsShot,
                            resources,
                            true
                    ),
                    tableData
            )
        }
        catch (e: IllegalArgumentException) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.err_table_view__no_data)
            builder.setMessage(R.string.err_score_pad__no_arrows)
            builder.setPositiveButton(R.string.err_button__ok) { _, _ ->
                activity?.onBackPressed()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    inner class ScorePadTableViewListener(private val tableView: TableView) : ITableViewListener {
        override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

        override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

        override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

        override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
    }
}
