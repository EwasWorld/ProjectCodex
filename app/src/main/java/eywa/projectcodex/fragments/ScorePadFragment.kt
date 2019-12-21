package eywa.projectcodex.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.End
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.database.ScoresViewModel
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.scorepadTable.ScorePadCell
import eywa.projectcodex.scorepadTable.ScorePadTableViewAdapter
import ph.ingenuity.tableview.TableView
import kotlin.math.min

class ScorePadFragment : Fragment() {

    private val args: ScorePadFragmentArgs by navArgs()
    private lateinit var scoresViewModel: ScoresViewModel
    // TODO pull this from the database when rounds are properly implemented
    private val goldsType = GoldsType.TENS

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scorepad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
         * Setup table
         */
        val tableAdapter = ScorePadTableViewAdapter(context!!)
        val tableView = view.findViewById<TableView>(R.id.table_view)
        tableView.adapter = tableAdapter

        /*
         * Create column headers
         */
        var col = 0
        val columnHeadersList = listOf(
                resources.getString(R.string.scorepad_end_string_header),
                resources.getString(R.string.scorepad_hits_header),
                resources.getString(R.string.scorepad_score_header),
                resources.getString(goldsType.colHeaderStringID),
                resources.getString(R.string.scorepad_running_total_header)
        ).map { ScorePadCell(it, "col" + col++.toString()) }

        /*
         * Setup database and its callback
         */
        scoresViewModel = ViewModelProvider(this).get(ScoresViewModel::class.java)
        scoresViewModel.allArrows.observe(viewLifecycleOwner, Observer { arrows ->
            arrows?.let {
                val arrows2DArray = create2DArrowArray(arrows)
                var row = 0
                val rowHeaders = IntRange(1, arrows2DArray.size).map { ScorePadCell(it, "row" + row++.toString()) }
                tableAdapter.setAllItems(arrows2DArray, columnHeadersList, rowHeaders)

                for (i in tableAdapter.columnHeaderItems?.indices ?: columnHeadersList.indices) {
                    tableView.remeasureColumnWidth(i)
                }
            }
        })
    }

    /**
     * @return allArrows as a 2D array ready for the table
     */
    private fun create2DArrowArray(allArrows: List<ArrowValue>): List<List<ScorePadCell>> {
        val tableData = mutableListOf<List<ScorePadCell>>()
        for (index in allArrows.indices step (args.endSize)) {
            val rowData = mutableListOf<String>()
            val end = End(
                    allArrows.subList(index, min(index + args.endSize, allArrows.size)),
                    6,
                    resources.getString(R.string.arrow_placeholder),
                    resources.getString(R.string.arrow_deliminator)
            )
            end.reorderScores()
            rowData.add(end.toString())
            rowData.add(end.getHits().toString())
            rowData.add(end.getScore().toString())
            rowData.add(end.getGolds(goldsType).toString())
            rowData.add(allArrows.subList(0, index + args.endSize).sumBy { arrow -> arrow.score }.toString())

            var col = 0
            tableData.add(rowData.map { ScorePadCell(it, "cell" + (tableData.size).toString() + col++.toString()) })
        }
        return tableData
    }
}
