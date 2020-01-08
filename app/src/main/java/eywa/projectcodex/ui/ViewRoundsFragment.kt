package eywa.projectcodex.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.InfoTableViewAdapter
import eywa.projectcodex.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.infoTable.getViewRoundsColumnHeaders
import eywa.projectcodex.viewModels.ViewRoundsViewModel
import ph.ingenuity.tableview.TableView

class ViewRoundsFragment : Fragment() {
    private lateinit var viewRoundsViewModel: ViewRoundsViewModel
    private var allArrows: List<ArrowValue> = listOf()
    private var allArcherRounds: List<ArcherRound> = listOf()
    // TODO pull this from the database when rounds are properly implemented
    private val goldsType = GoldsType.TENS
    private var dialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_rounds, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.view_round__title)

        val tableAdapter = InfoTableViewAdapter(context!!)
        val tableView = view.findViewById<TableView>(R.id.table_view)
        tableView.adapter = tableAdapter

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
            if (dialog!!.isShowing) {
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
}
