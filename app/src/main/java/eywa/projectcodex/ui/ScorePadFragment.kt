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
import eywa.projectcodex.infoTable.*
import eywa.projectcodex.viewModels.ScorePadViewModel
import eywa.projectcodex.viewModels.ViewModelFactory

class ScorePadFragment : Fragment() {
    private val args: ScorePadFragmentArgs by navArgs()
    private lateinit var scorePadViewModel: ScorePadViewModel
    // TODO pull this from the database when rounds are properly implemented
    private val goldsType = GoldsType.TENS

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scorepad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.score_pad__title)

        val tableAdapter = InfoTableViewAdapter(context!!)
        val tableView = view.findViewById<TableView>(R.id.score_pad__table_view)
        tableView.adapter = tableAdapter
        tableView.tableViewListener = ScorePadTableViewListener(tableView)

        scorePadViewModel = ViewModelProvider(this, ViewModelFactory {
            ScorePadViewModel(activity!!.application, args.archerRoundId)
        }).get(ScorePadViewModel::class.java)
        scorePadViewModel.arrowsForRound.observe(viewLifecycleOwner, Observer { arrows ->
            arrows?.let {
                try {
                    val tableData = calculateScorePadTableData(
                            arrows, args.endSize, goldsType,
                            getString(R.string.end_to_string_arrow_placeholder),
                            getString(R.string.end_to_string_arrow_deliminator)
                    )
                    tableAdapter.setAllItems(
                            getColumnHeadersForTable(scorePadColumnHeaderIds, resources, goldsType),
                            generateNumberedRowHeaders(tableData.size),
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
        })
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
