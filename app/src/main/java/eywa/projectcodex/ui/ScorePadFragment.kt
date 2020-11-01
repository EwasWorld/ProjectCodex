package eywa.projectcodex.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.infoTable.*
import eywa.projectcodex.logic.GoldsType
import eywa.projectcodex.logic.getGoldsType
import eywa.projectcodex.viewModels.ScorePadViewModel
import eywa.projectcodex.viewModels.ViewModelFactory
import kotlin.math.ceil

class ScorePadFragment : Fragment() {
    private val args: ScorePadFragmentArgs by navArgs()
    private lateinit var scorePadViewModel: ScorePadViewModel
    private var dialog: AlertDialog? = null

    // TODO Is there a way to make these setters common?
    private var goldsType = GoldsType.TENS
        set(value) {
            if (value == field) return
            field = value
            updateTable()
        }
    private var arrowCounts = listOf<RoundArrowCount>()
        set(value) {
            if (value.isNullOrEmpty() || value == field) return
            field = value
            updateTable()
        }
    private var distances = listOf<RoundDistance>()
        set(value) {
            if (value.isNullOrEmpty() || value == field) return
            field = value
            updateTable()
        }
    private var distanceUnit: String? = null
        set(value) {
            if (value.isNullOrEmpty() || value == field) return
            field = value
            updateTable()
        }
    private var arrows = listOf<ArrowValue>()
        set(value) {
            if (value.isNullOrEmpty() || value == field) return
            field = value
            updateTable()
        }
    private var endSize: Int? = null
        set(value) {
            if (value == null || value < 1 || value == field) return
            field = value
            updateTable()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_score_pad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.score_pad__title)

        if (endSize == null) endSize = args.endSize

        scorePadViewModel = ViewModelProvider(this, ViewModelFactory {
            ScorePadViewModel(activity!!.application, args.archerRoundId)
        }).get(ScorePadViewModel::class.java)

        // Get arrow counts and distances
        scorePadViewModel.archerRound.observe(viewLifecycleOwner, Observer { archerRound ->
            if (archerRound == null) return@Observer
            archerRound.roundId?.let { roundId ->
                scorePadViewModel.getArrowCountsForRound(roundId).observe(viewLifecycleOwner, Observer {
                    arrowCounts = it
                })
                scorePadViewModel.getDistancesForRound(roundId, archerRound.roundSubTypeId)
                        .observe(viewLifecycleOwner, Observer {
                            distances = it
                        })
            }
        })

        // Get golds type and distance unit
        scorePadViewModel.roundInfo.observe(viewLifecycleOwner, Observer { round ->
            if (round == null) return@Observer
            distanceUnit =
                    getString(if (round.isMetric) R.string.units_meters_short else R.string.units_yards_short)
            goldsType = getGoldsType(round.isOutdoor, round.isMetric)
        })

        // Get arrows
        scorePadViewModel.arrowsForRound.observe(viewLifecycleOwner, Observer { arrowValues ->
            if (arrowValues == null) return@Observer
            if (arrowValues.isNullOrEmpty()) {
                displayError()
                return@Observer
            }
            arrows = arrowValues
        })
    }

    /**
     * Does nothing if the requirements to make a valid table are not met
     */
    private fun updateTable() {
        if (endSize == null || arrows.isNullOrEmpty() || arrowCounts.size != distances.size ||
            (distances.isNotEmpty() && distanceUnit.isNullOrBlank())) {
            return
        }

        try {
            /*
             * For some unknown reason not creating a new table adapter every time causes major display issues. Chiefly:
             *  - On deletion of a row, column sizes go crazy and column headers no longer horizontally align with data
             *  - On setAllItems it will randomly bold some rows and headers and truncate data in random places
             */
            val tableAdapter = InfoTableViewAdapter(context!!)
            val tableView = view!!.findViewById<TableView>(R.id.table_view_score_pad)
            tableView.adapter = tableAdapter
            tableView.tableViewListener = ScorePadTableViewListener(tableView)

            val tableData = calculateScorePadTableData(
                    arrows, endSize!!, goldsType, resources, arrowCounts, distances, distanceUnit
            )
            val arrowRowsShot = ceil(arrows.size / endSize!!.toDouble()).toInt()
            tableAdapter.setAllItems(
                    getColumnHeadersForTable(scorePadColumnHeaderIds, resources, goldsType),
                    generateNumberedRowHeaders(
                            if (arrowCounts.isNotEmpty()) {
                                arrowCounts.map { ceil(it.arrowCount / endSize!!.toDouble()).toInt() }
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
            displayError()
        }
    }

    private fun displayError() {
        if (dialog != null) {
            return
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.err_table_view__no_data)
        builder.setMessage(R.string.err_score_pad__no_arrows)
        builder.setPositiveButton(R.string.err_button__ok) { dialogInterface, _ ->
            activity?.onBackPressed()
            dialogInterface.cancel()
            dialog = null
        }
        dialog = builder.create()
        dialog!!.show()
    }

    inner class ScorePadTableViewListener(private val tableView: TableView) : ITableViewListener {
        override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
            // Note arrows are counted from 1
            val firstArrowId = row * args.endSize + 1

            if ((tableView.adapter!!.getCellItem(column, row) as InfoTableCell).id.contains(DELETE_CELL_ID_PREFIX)) {
                // -1 because this is an index not an ID
                scorePadViewModel.deleteArrows(firstArrowId - 1, args.endSize)
            }
            else {
                val action = ScorePadFragmentDirections.actionScorePadFragmentToEditEndFragment(
                        args.endSize, args.archerRoundId, firstArrowId
                )
                view?.findNavController()?.navigate(action)
            }
        }

        override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

        override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

        override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
    }
}
