package eywa.projectcodex.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.*
import eywa.projectcodex.logic.GoldsType
import eywa.projectcodex.viewModels.ViewRoundsViewModel

class ViewRoundsFragment : Fragment() {
    private lateinit var viewRoundsViewModel: ViewRoundsViewModel
    private var allArrows: List<ArrowValue> = listOf()
    private var allArcherRoundsWithNames: List<ArcherRoundWithRoundInfoAndName> = listOf()
    private val goldsType = GoldsType.TENS
    private var selectedArcherRoundId = -1
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
        tableView.tableViewListener = ViewRoundsTableViewListener()
        registerForContextMenu(tableView.cellRecyclerView)

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
                builder.setPositiveButton(R.string.button_ok) { _, _ ->
                    activity?.onBackPressed()
                }
                dialog = builder.create()
            }
            if (!dialog!!.isShowing) {
                dialog!!.show()
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity!!.menuInflater.inflate(R.menu.view_rounds_item_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        check(selectedArcherRoundId != -1) { "No round id selected" }

        return when (item.itemId) {
            R.id.button_view_rounds_menu__score_pad -> {
                openScorePad()
                true
            }
            R.id.button_view_rounds_menu__delete -> {
                viewRoundsViewModel.deleteRound(selectedArcherRoundId)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun openScorePad() {
        val action = ViewRoundsFragmentDirections.actionViewRoundsFragmentToScorePadFragment(
                6, selectedArcherRoundId
        )
        view?.findNavController()?.navigate(action)
    }

    private fun getArcherRoundId(row: Int): Int {
        return hiddenColumns[row][hiddenColumnIndexes.indexOf(archerRoundIdRow)].content as Int
    }

    inner class ViewRoundsTableViewListener : ITableViewListener {
        override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
            selectedArcherRoundId = getArcherRoundId(row)
            openScorePad()
        }

        override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
            // Set the round id for the context menu to use
            selectedArcherRoundId = getArcherRoundId(row)
        }

        override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

        override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
    }
}
