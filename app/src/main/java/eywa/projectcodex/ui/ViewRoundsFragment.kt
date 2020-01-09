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
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.*
import eywa.projectcodex.viewModels.ViewRoundsViewModel
import ph.ingenuity.tableview.TableView
import ph.ingenuity.tableview.listener.ITableViewListener

class ViewRoundsFragment : Fragment() {
    private lateinit var viewRoundsViewModel: ViewRoundsViewModel
    private var allArrows: List<ArrowValue> = listOf()
    private var allArcherRounds: List<ArcherRound> = listOf()
    // TODO pull this from the database when rounds are properly implemented
    private val goldsType = GoldsType.TENS
    private var dialog: AlertDialog? = null
    private val archerRoundIdRowId = 5

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_rounds, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.view_round__title)

        val tableAdapter = InfoTableViewAdapter(context!!)
        val tableView = view.findViewById<TableView>(R.id.table_view)
        tableView.adapter = tableAdapter
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
                allArcherRounds = archerRounds
                populateTable(tableAdapter)
            }
        })
    }

    private fun populateTable(tableAdapter: InfoTableViewAdapter) {
        try {
            val tableData = calculateViewRoundsTableData(
                    allArcherRounds,
                    allArrows,
                    goldsType,
                    getString(R.string.short_boolean_true),
                    getString(R.string.short_boolean_false)
            )
            tableAdapter.setAllItems(
                    tableData,
                    getViewRoundsColumnHeaders(resources, goldsType),
                    generateNumberedRowHeaders(tableData.size)
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
            tableView.adapter?.cellItems?.let { cellItems ->
                // TODO set up preferred end size
                val action = ViewRoundsFragmentDirections.actionViewRoundsFragmentToScorePadFragment(
                        6,
                        (cellItems[row][archerRoundIdRowId] as InfoTableCell).content as Int
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
