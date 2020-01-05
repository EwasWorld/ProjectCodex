package eywa.projectcodex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.*
import eywa.projectcodex.infoTable.InfoTableViewAdapter
import eywa.projectcodex.infoTable.calculateScorePadTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.infoTable.getScorePadColumnHeaders
import eywa.projectcodex.viewModels.ScorePadViewModel
import eywa.projectcodex.viewModels.ViewModelFactory
import ph.ingenuity.tableview.TableView

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

        val tableAdapter = InfoTableViewAdapter(context!!)
        val tableView = view.findViewById<TableView>(R.id.table_view)
        tableView.adapter = tableAdapter

        scorePadViewModel = ViewModelProvider(this, ViewModelFactory {
            ScorePadViewModel(activity!!.application, args.archerRoundId)
        }).get(ScorePadViewModel::class.java)
        scorePadViewModel.arrowsForRound.observe(viewLifecycleOwner, Observer { arrows ->
            arrows?.let {
                val tableData = calculateScorePadTableData(
                        arrows, args.endSize, goldsType,
                        resources.getString(R.string.end_to_string_arrow_placeholder),
                        resources.getString(R.string.end_to_string_arrow_deliminator)
                )
                tableAdapter.setAllItems(
                        tableData,
                        getScorePadColumnHeaders(resources, goldsType),
                        generateNumberedRowHeaders(tableData.size)
                )
            }
        })
    }
}
