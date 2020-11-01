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
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import eywa.projectcodex.logic.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.*
import eywa.projectcodex.viewModels.ViewRoundsViewModel

class ViewRoundsFragment : Fragment() {
    private lateinit var viewRoundsViewModel: ViewRoundsViewModel
    private var allArrows: List<ArrowValue> = listOf()
    private var allArcherRoundsWithNames: List<ArcherRoundWithRoundInfoAndName> = listOf()
    private val goldsType = GoldsType.TENS
    private var dialog: AlertDialog? = null
    private val archerRoundIdRow = viewRoundsColumnHeaderIds.indexOf(R.string.view_round__id_header)
    private val countsToHcRow = viewRoundsColumnHeaderIds.indexOf(R.string.view_round__counts_to_hc_header)
    private val hiddenColumnIndexes = listOf(archerRoundIdRow, countsToHcRow).sorted()
    private lateinit var hiddenColumns: MutableList<MutableList<InfoTableCell>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_rounds, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.view_round__title)

        val tableAdapter = InfoTableViewAdapter(context!!)
        val tableView = view.findViewById<TableView>(R.id.table_view_view_rounds)
        tableView.adapter = tableAdapter
        tableView.rowHeaderWidth = 0
        tableView.tableViewListener = ViewRoundsTableViewListener(tableView)

        viewRoundsViewModel = ViewModelProvider(this).get(ViewRoundsViewModel::class.java)
        viewRoundsViewModel.allArrows.observe(viewLifecycleOwner, Observer { arrows ->
            arrows?.let {
                allArrows = arrows
                populateTable(tableAdapter)
            }
        })
        viewRoundsViewModel.allArcherRounds.observe(viewLifecycleOwner, Observer { archerRounds ->
            archerRounds?.let {
                allArcherRoundsWithNames = archerRounds
                populateTable(tableAdapter)
            }
        })
    }

    private fun populateTable(tableAdapter: InfoTableViewAdapter) {
        try {
            val tableData = calculateViewRoundsTableData(
                    allArcherRoundsWithNames,
                    allArrows,
                    goldsType,
                    resources
            )

            // Remove columns to be hidden
            val displayTableData = mutableListOf<MutableList<InfoTableCell>>()
            hiddenColumns = mutableListOf()
            for (row in tableData) {
                hiddenColumns.add(row.filterIndexed { i, _ -> hiddenColumnIndexes.contains(i) }.toMutableList())
                displayTableData.add(row.filterIndexed { i, _ -> !hiddenColumnIndexes.contains(i) }.toMutableList())
            }

            val colHeaders = getColumnHeadersForTable(viewRoundsColumnHeaderIds, resources, goldsType)
            tableAdapter.setAllItems(
                    colHeaders.filterIndexed { i, _ -> !hiddenColumnIndexes.contains(i) || i == colHeaders.size - 1 },
                    generateNumberedRowHeaders(tableData.size),
                    displayTableData
            )
            if (dialog?.isShowing == true) {
                dialog!!.dismiss()
            }
        }
        catch (e: IllegalArgumentException) {
            if (dialog == null) {
                val builder = AlertDialog.Builder(activity)
                builder.setTitle(R.string.err_table_view__no_data)
                builder.setMessage(R.string.err_view_round__no_rounds)
                builder.setPositiveButton(R.string.err_button__ok) { _, _ ->
                    activity?.onBackPressed()
                }
                dialog = builder.create()
            }
            if (!dialog!!.isShowing) {
                dialog!!.show()
            }
        }
    }

    inner class ViewRoundsTableViewListener(private val tableView: TableView) : ITableViewListener {
        override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
            val roundId = hiddenColumns[row][hiddenColumnIndexes.indexOf(archerRoundIdRow)].content as Int
            if ((tableView.adapter!!.getCellItem(column, row) as InfoTableCell).id.contains("delete")) {
                viewRoundsViewModel.deleteRound(roundId)
            }
            else {
                val action = ViewRoundsFragmentDirections.actionViewRoundsFragmentToScorePadFragment(6, roundId)
                view?.findNavController()?.navigate(action)
            }
        }

        override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

        override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {
            tableView.remeasureColumnWidth(column)
        }

        override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

        override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
    }
}
