package eywa.projectcodex.components.archerRoundScore.scorePad

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.utils.*
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.*
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

class ScorePadFragment : Fragment(), ActionBarHelp, ArcherRoundBottomNavigationInfo {
    companion object {
        const val LOG_TAG = "ScorePadFragment"
    }

    private val args: ScorePadFragmentArgs by navArgs()

    private val scorePadViewModel: ArcherRoundScoreViewModel by activityViewModels()
    private var selectedRow = 0
    private var roundName: String? = null
    private val columnHeaderOrder = listOf(
            ScorePadData.ColumnHeader.END_STRING,
            ScorePadData.ColumnHeader.HITS,
            ScorePadData.ColumnHeader.SCORE,
            ScorePadData.ColumnHeader.GOLDS,
            ScorePadData.ColumnHeader.RUNNING_TOTAL
    )

    private val noArrowsErrorDialog by lazy {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.err_table_view__no_data)
        builder.setMessage(R.string.err_score_pad__no_arrows)
        builder.setPositiveButton(R.string.general_ok) { dialogInterface, _ ->
            // Don't skip fragments like InputEnd from the backstack in this case
            //      else it will kick the user out of the score completely when this was probably a mistake
            requireView().findNavController().popBackStack()
            dialogInterface.cancel()
        }
        builder.create()
    }

    // TODO Is there a way to make these setters common?
    private var goldsType = GoldsType.defaultGoldsType
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
    private val endSize = 6

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_score_pad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomLogger.customLogger.d(LOG_TAG, "onViewCreated")
        setFragmentTitle()

        scorePadViewModel.archerRoundIdMutableLiveData.postValue(args.archerRoundId)

        // Get arrow counts and distances
        scorePadViewModel.archerRoundWithInfo.observe(viewLifecycleOwner, Observer { archerRoundInfo ->
            if (archerRoundInfo == null) return@Observer
            roundName = archerRoundInfo.displayName
            setFragmentTitle()
            archerRoundInfo.round?.roundId?.let { roundId ->
                scorePadViewModel.getArrowCountsForRound(roundId).observe(viewLifecycleOwner, {
                    arrowCounts = it
                })
                scorePadViewModel.getDistancesForRound(roundId, archerRoundInfo.archerRound.roundSubTypeId)
                        .observe(viewLifecycleOwner, {
                            distances = it
                        })
            }
        })

        // Get golds type and distance unit
        scorePadViewModel.roundInfo.observe(viewLifecycleOwner, Observer { round ->
            if (round == null) return@Observer
            distanceUnit =
                    getString(if (round.isMetric) R.string.units_meters_short else R.string.units_yards_short)
            goldsType = GoldsType.getGoldsType(round)
        })

        // Get arrows
        scorePadViewModel.arrowsForRound.observe(viewLifecycleOwner, Observer { arrowValues ->
            if (arrowValues == null) return@Observer
            if (arrowValues.isNullOrEmpty() && !noArrowsErrorDialog.isShowing) {
                noArrowsErrorDialog.show()
                return@Observer
            }
            if (noArrowsErrorDialog.isShowing) {
                noArrowsErrorDialog.cancel()
            }
            arrows = arrowValues
        })
    }

    private fun setFragmentTitle() {
        activity?.title = roundName ?: getString(R.string.score_pad__title)
    }

    override fun onResume() {
        super.onResume()
        updateTable()
    }

    /**
     * Does nothing if the requirements to make a valid table are not met
     */
    private fun updateTable() {
        if (arrows.isNullOrEmpty() || arrowCounts.size != distances.size ||
            (distances.isNotEmpty() && distanceUnit.isNullOrBlank())
        ) {
            return
        }
        CustomLogger.customLogger.d(LOG_TAG, "updateTable")

        /*
         * For some unknown reason not creating a new table adapter every time causes major display issues. Chiefly:
         *  - On deletion of a row, column sizes go crazy and column headers no longer horizontally align with data
         *  - On setAllItems it will randomly bold some rows and headers and truncate data in random places
         */
        val tableAdapter = InfoTableViewAdapter(requireContext())
        val tableView = requireView().findViewById<TableView>(R.id.table_view_score_pad)
        tableView.adapter = tableAdapter
        tableView.tableViewListener = ScorePadTableViewListener(tableView)
        registerForContextMenu(tableView.cellRecyclerView)

        val tableData = ScorePadData(arrows, endSize, goldsType, resources, arrowCounts, distances, distanceUnit)
        if (tableData.isNullOrEmpty() && !noArrowsErrorDialog.isShowing) {
            noArrowsErrorDialog.show()
            return
        }
        if (noArrowsErrorDialog.isShowing) {
            noArrowsErrorDialog.cancel()
        }

        tableAdapter.setAllItems(
                ScorePadData.getColumnHeadersForTable(columnHeaderOrder, resources, goldsType),
                tableData.generateRowHeaders(
                        resources.getString(R.string.score_pad__distance_total_row_header),
                        resources.getString(R.string.score_pad__grand_total_row_header),
                ),
                tableData.getAsTableCells(columnHeaderOrder)
        )
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.score_pad_item_menu, menu)

        // Don't show insert option if all arrows have been inputted
        val allowInsert = arrowCounts.isEmpty() || arrowCounts.sumOf { it.arrowCount } > arrows.count()
        menu.findItem(R.id.button_score_pad_menu__insert).isVisible = allowInsert
        menu.findItem(R.id.button_score_pad_menu__insert).isEnabled = allowInsert
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Note arrows are counted from 1
        val firstArrowId = selectedRow * endSize + 1

        return when (item.itemId) {
            R.id.button_score_pad_menu__edit -> {
                val action = ScorePadFragmentDirections.actionScorePadFragmentToEditEndFragment(
                        endSize, args.archerRoundId, firstArrowId
                )
                view?.findNavController()?.navigate(action)
                true
            }
            R.id.button_score_pad_menu__insert -> {
                val action = ScorePadFragmentDirections.actionScorePadFragmentToInsertEndFragment(
                        endSize, args.archerRoundId, firstArrowId
                )
                view?.findNavController()?.navigate(action)
                true
            }
            R.id.button_score_pad_menu__delete -> {
                scorePadViewModel.deleteArrows(firstArrowId, endSize)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    inner class ScorePadTableViewListener(private val tableView: TableView) : ITableViewListener {
        override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
            if ((cellView as InfoTableViewAdapter.InfoTableViewHolder).isTotalCell) {
                return
            }

            selectedRow = row
            showContextMenuOnCentreOfView(cellView.itemView)
        }

        override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
            onCellClicked(cellView, column, row)
        }

        override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

        override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

        override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        return listOf(
                ActionBarHelp.HelpShowcaseItem.Builder()
                        .setHelpTitleId(R.string.help_score_pad__open_menu_title)
                        .setHelpBodyId(R.string.help_table_open_menu_body)
                        .setShape(ActionBarHelp.ShowcaseShape.NO_SHAPE)
                        .build()
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }

    override fun getArcherRoundId(): Int {
        return args.archerRoundId
    }

    override fun isRoundComplete(): Boolean {
        // Arrow counts will be empty if no round is being tracked
        if (arrowCounts.isEmpty()) {
            return false
        }
        return arrowCounts.sumOf { it.arrowCount } <= arrows.size
    }
}
