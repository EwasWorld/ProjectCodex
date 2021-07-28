package eywa.projectcodex.components.viewRounds

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.ToastSpamPrevention
import eywa.projectcodex.components.commonUtils.resourceStringReplace
import eywa.projectcodex.components.commonUtils.showContextMenuOnCentreOfView
import eywa.projectcodex.components.infoTable.*
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

class ViewRoundsFragment : Fragment(), ActionBarHelp {
    companion object {
        private const val LOG_TAG = "ViewRoundsFrag"
    }

    private lateinit var viewRoundsViewModel: ViewRoundsViewModel

    /*
     * All data from certain tables in the database
     */
    private var allArrows: List<ArrowValue> = listOf()
    private var allArcherRoundsWithNames: List<ArcherRoundWithRoundInfoAndName> = listOf()
    private var allArrowCounts: List<RoundArrowCount> = listOf()
    private var allDistances: List<RoundDistance> = listOf()

    /**
     * How to calculate the golds column
     */
    private val goldsType = GoldsType.TENS

    /**
     * Currently selected row, set when any row is pressed or long pressed
     */
    private var selectedArcherRoundId = -1

    /**
     * Currently selected item in the [convertDialog]
     */
    private var selectedConversionType: ConvertScore? = null

    /**
     * Displayed when there's no information to display in the table
     */
    private val emptyTableDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.err_table_view__no_data)
        builder.setMessage(R.string.err_view_round__no_rounds)
        builder.setPositiveButton(R.string.general_ok) { _, _ ->
            requireView().findNavController().popBackStack()
        }
        builder.create()
    }

    /**
     * Displayed if a user tries to continue a round that's already completed
     */
    private val roundCompleteDialog by lazy {
        val okListener = DialogInterface.OnClickListener { _, _ ->
            val action = ViewRoundsFragmentDirections.actionViewRoundsFragmentToInputEndFragment(
                    selectedArcherRoundId
            )
            view?.findNavController()?.navigate(action)
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.err_view_round__round_already_complete_title)
        builder.setMessage(R.string.err_view_round__round_already_complete)
        builder.setPositiveButton(R.string.general_continue, okListener)
        builder.setNegativeButton(R.string.general_cancel) { _, _ -> }
        builder.create()
    }

    /**
     * Displayed when the user selects the 'convert' option from the menu
     */
    private val convertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.view_round__convert_score_dialog_title)
        val menuItems = listOf(
                R.string.view_rounds__convert_xs_to_tens to ConvertScore.XS_TO_TENS,
                R.string.view_rounds__convert_to_five_zone to ConvertScore.TO_FIVE_ZONE
        )

        @SuppressLint("InflateParams")
        val content = layoutInflater.inflate(R.layout.list_msg_dialog, null)
        content.findViewById<TextView>(R.id.text_list_msg_dialog__message).text =
                resources.getString(R.string.view_round__convert_score_dialog_body)

        val radioGroup = content.findViewById<RadioGroup>(R.id.radios_list_msg_dialog__items)

        menuItems.forEachIndexed { i, menuItem ->
            @SuppressLint("InflateParams")
            val layout = layoutInflater.inflate(R.layout.radio_button, null, false)
            val radioButton = layout.findViewById<RadioButton>(R.id.radio_button)
            radioButton.text = resources.getString(menuItem.first)
            radioButton.setOnClickListener {
                selectedConversionType = menuItem.second
            }
            layout.id = i
            radioGroup.addView(radioButton)
        }
        radioGroup.check(0)

        builder.setView(content)
        builder.setPositiveButton(R.string.general_ok) { dialog, _ ->
            val archerRoundId = selectedArcherRoundId
            ToastSpamPrevention.displayToast(
                    requireContext(), resources.getString(R.string.view_round__convert_score_started_message)
            )
            val completedMessage = resources.getString(R.string.view_round__convert_score_completed_message)
            selectedConversionType!!.convertScore(
                    allArrows.filter { it.archerRoundId == archerRoundId }, viewRoundsViewModel
            )?.invokeOnCompletion {
                ToastSpamPrevention.displayToast(requireContext(), completedMessage)
            } ?: ToastSpamPrevention.displayToast(requireContext(), completedMessage)
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.general_cancel) { _, _ -> }
        builder.create()
    }
    private val archerRoundIdColumn = viewRoundsColumnHeaderIds.indexOf(R.string.view_round__id_header)

    /**
     * Column indexes as per [viewRoundsColumnHeaderIds] which should be hidden
     */
    private val hiddenColumnIndexes = listOf(archerRoundIdColumn).sorted()

    /**
     * Data removed from the table so that the column is 'hidden'
     */
    private lateinit var hiddenColumnData: MutableList<MutableList<InfoTableCell>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_rounds, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.view_round__title)

        val tableAdapter = InfoTableViewAdapter(requireContext())
        val tableView = view.findViewById<TableView>(R.id.table_view_view_rounds)
        tableView.adapter = tableAdapter
        tableView.rowHeaderWidth = 0
        tableView.tableViewListener = ViewRoundsTableViewListener()
        registerForContextMenu(tableView.cellRecyclerView)

        viewRoundsViewModel = ViewModelProvider(this).get(ViewRoundsViewModel::class.java)
        viewRoundsViewModel.allArrows.observe(viewLifecycleOwner, { arrows ->
            arrows?.let {
                allArrows = arrows
                populateTable(tableAdapter)
            }
        })
        viewRoundsViewModel.allArcherRounds.observe(viewLifecycleOwner, { archerRounds ->
            archerRounds?.let {
                allArcherRoundsWithNames = archerRounds
                populateTable(tableAdapter)
            }
        })
        viewRoundsViewModel.allArrowCounts.observe(viewLifecycleOwner, { arrowCounts ->
            arrowCounts?.let {
                allArrowCounts = arrowCounts
                populateTable(tableAdapter)
            }
        })
        viewRoundsViewModel.allDistances.observe(viewLifecycleOwner, { distances ->
            distances?.let {
                allDistances = distances
                populateTable(tableAdapter)
            }
        })
    }

    private fun populateTable(tableAdapter: InfoTableViewAdapter) {
        val tableData = calculateViewRoundsTableData(
                allArcherRoundsWithNames,
                allArrows,
                goldsType,
                allArrowCounts,
                allDistances
        )

        if (!tableData.isNullOrEmpty()) {
            // Remove columns to be hidden
            val displayTableData = mutableListOf<MutableList<InfoTableCell>>()
            hiddenColumnData = mutableListOf()
            for (row in tableData) {
                hiddenColumnData.add(row.filterIndexed { i, _ -> hiddenColumnIndexes.contains(i) }.toMutableList())
                displayTableData.add(row.filterIndexed { i, _ -> !hiddenColumnIndexes.contains(i) }.toMutableList())
            }

            val colHeaders = getColumnHeadersForTable(viewRoundsColumnHeaderIds, resources, goldsType)
            tableAdapter.setAllItems(
                    colHeaders.filterIndexed { i, _ -> !hiddenColumnIndexes.contains(i) },
                    generateNumberedRowHeaders(tableData.size),
                    displayTableData
            )
            if (emptyTableDialog.isShowing) {
                emptyTableDialog.dismiss()
            }
        }
        else {
            if (!emptyTableDialog.isShowing) {
                emptyTableDialog.show()
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.view_rounds_item_menu, menu)

        check(selectedArcherRoundId != -1) { "No round id selected" }
        val selectedArcherRoundInfo =
                allArcherRoundsWithNames.find { it.archerRound.archerRoundId == selectedArcherRoundId }
        if (selectedArcherRoundInfo == null) {
            CustomLogger.customLogger.w(LOG_TAG, "No archer round info for selected round")
            return
        }

        val selectedRoundId = selectedArcherRoundInfo.round?.roundId ?: return
        val roundArrowCount = allArrowCounts.filter { it.roundId == selectedRoundId }.sumOf { it.arrowCount }
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
                            .sumOf { it.arrowCount }
                    if (arrowsShot >= arrowsInRound) {
                        // Warn the user they're about to add arrows to a completed round
                        if (!roundCompleteDialog.isShowing) {
                            roundCompleteDialog.show()
                        }
                        return true
                    }
                }
                val action =
                        ViewRoundsFragmentDirections.actionViewRoundsFragmentToInputEndFragment(selectedArcherRoundId)
                view?.findNavController()?.navigate(action)
                true
            }
            R.id.button_view_rounds_menu__delete -> {
                viewRoundsViewModel.deleteRound(selectedArcherRoundId)
                true
            }
            R.id.button_view_rounds_menu__convert -> {
                convertDialog.show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun openScorePad() {
        val action = ViewRoundsFragmentDirections.actionViewRoundsFragmentToScorePadFragment(
                selectedArcherRoundId
        )
        view?.findNavController()?.navigate(action)
    }

    private fun getArcherRoundId(row: Int): Int {
        return hiddenColumnData[row][hiddenColumnIndexes.indexOf(archerRoundIdColumn)].content as Int
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
