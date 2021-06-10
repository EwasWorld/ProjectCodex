package eywa.projectcodex.components.viewRounds

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.resourceStringReplace
import eywa.projectcodex.components.commonUtils.showContextMenuOnCentreOfView
import eywa.projectcodex.components.infoTable.*
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount

class ViewRoundsFragment : Fragment(), ActionBarHelp {
    companion object {
        const val LOG_TAG = "ViewRoundsFrag"
    }

    private lateinit var viewRoundsViewModel: ViewRoundsViewModel
    private var allArrows: List<ArrowValue> = listOf()
    private var allArcherRoundsWithNames: List<ArcherRoundWithRoundInfoAndName> = listOf()
    private var allArrowCounts: List<RoundArrowCount> = listOf()
    private val goldsType = GoldsType.TENS
    private var selectedArcherRoundId = -1
    private var emptyDialog: AlertDialog? = null
    private var roundCompleteDialog: AlertDialog? = null
    private val archerRoundIdColumn = viewRoundsColumnHeaderIds.indexOf(R.string.view_round__id_header)
    private val countsToHcColumn = viewRoundsColumnHeaderIds.indexOf(R.string.view_round__counts_to_hc_header)
    private val hiddenColumnIndexes = listOf(archerRoundIdColumn, countsToHcColumn).sorted()
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
        viewRoundsViewModel.allArrowCounts.observe(viewLifecycleOwner, Observer { arrowCounts ->
            arrowCounts?.let { allArrowCounts = arrowCounts }
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
                    colHeaders.filterIndexed { i, _ -> !hiddenColumnIndexes.contains(i) },
                    generateNumberedRowHeaders(tableData.size),
                    displayTableData
            )
            if (emptyDialog?.isShowing == true) {
                emptyDialog!!.dismiss()
            }
        }
        catch (e: IllegalArgumentException) {
            if (emptyDialog == null) {
                val builder = AlertDialog.Builder(activity)
                builder.setTitle(R.string.err_table_view__no_data)
                builder.setMessage(R.string.err_view_round__no_rounds)
                builder.setPositiveButton(R.string.general_ok) { _, _ ->
                    activity?.onBackPressed()
                }
                emptyDialog = builder.create()
            }
            if (!emptyDialog!!.isShowing) {
                emptyDialog!!.show()
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity!!.menuInflater.inflate(R.menu.view_rounds_item_menu, menu)

        check(selectedArcherRoundId != -1) { "No round id selected" }
        val selectedArcherRoundInfo =
                allArcherRoundsWithNames.find { it.archerRound.archerRoundId == selectedArcherRoundId }
        if (selectedArcherRoundInfo == null) {
            CustomLogger.customLogger.w(LOG_TAG, "No archer round info for selected round")
            return
        }

        val selectedRoundId = selectedArcherRoundInfo.round?.roundId ?: return
        val roundArrowCount = allArrowCounts.filter { it.roundId == selectedRoundId }.sumBy { it.arrowCount }
        val currentArrowCount = allArrows.count { it.archerRoundId == selectedArcherRoundId }

        val showContinue = roundArrowCount > currentArrowCount
        menu.findItem(R.id.button_view_rounds_menu__continue).isVisible = showContinue
        menu.findItem(R.id.button_view_rounds_menu__continue).isEnabled = showContinue
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        check(selectedArcherRoundId != -1) { "No round id selected" }

        return when (item.itemId) {
            R.id.button_view_rounds_menu__score_pad -> {
                openScorePad()
                true
            }
            R.id.button_view_rounds_menu__continue -> {
                val selectedArcherRound =
                        allArcherRoundsWithNames.find { it.archerRound.archerRoundId == selectedArcherRoundId }
                val hasRound = selectedArcherRound?.round != null
                if (hasRound) {
                    /*
                     * Check whether the round is completed (full with arrows)
                     */
                    val arrowsShot = allArrows.filter { it.archerRoundId == selectedArcherRoundId }.count()
                    val arrowsInRound = allArrowCounts.filter { it.roundId == selectedArcherRound?.round?.roundId }
                            .sumBy { it.arrowCount }
                    if (arrowsShot >= arrowsInRound) {
                        /*
                         * Warn the user they're about to add arrows to a completed round
                         */
                        if (roundCompleteDialog == null) {
                            val okListener = DialogInterface.OnClickListener { _, _ ->
                                val action = ViewRoundsFragmentDirections.actionViewRoundsFragmentToInputEndFragment(
                                        selectedArcherRoundId, false
                                )
                                view?.findNavController()?.navigate(action)
                            }
                            val builder = AlertDialog.Builder(activity)
                            builder.setTitle(R.string.err_view_round__round_already_complete_title)
                            builder.setMessage(R.string.err_view_round__round_already_complete)
                            builder.setPositiveButton(R.string.general_continue, okListener)
                            builder.setNegativeButton(R.string.general_cancel) { _, _ -> }
                            roundCompleteDialog = builder.create()
                        }
                        if (!roundCompleteDialog!!.isShowing) {
                            roundCompleteDialog!!.show()
                        }
                        return true
                    }
                }
                val action = ViewRoundsFragmentDirections.actionViewRoundsFragmentToInputEndFragment(
                        selectedArcherRoundId, hasRound
                )
                view?.findNavController()?.navigate(action)
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
        return hiddenColumns[row][hiddenColumnIndexes.indexOf(archerRoundIdColumn)].content as Int
    }

    inner class ViewRoundsTableViewListener : ITableViewListener {
        override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
            selectedArcherRoundId = getArcherRoundId(row)
            openScorePad()
        }

        override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
            // Set the round id for the context menu to use
            selectedArcherRoundId = getArcherRoundId(row)
            showContextMenuOnCentreOfView(cellView.itemView)
        }

        override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

        override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
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
