package eywa.projectcodex.components.scorePad

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import eywa.projectcodex.R
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.archeryObjects.getGoldsType
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.ArcherRoundBottomNavigationInfo
import eywa.projectcodex.components.commonUtils.ViewModelFactory
import eywa.projectcodex.components.commonUtils.showContextMenuOnCentreOfView
import eywa.projectcodex.components.infoTable.*
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlin.math.ceil

class ScorePadFragment : Fragment(), ActionBarHelp, ArcherRoundBottomNavigationInfo {
    private val args: ScorePadFragmentArgs by navArgs()
    private lateinit var scorePadViewModel: ScorePadViewModel
    private var dialog: AlertDialog? = null
    private var selectedRow = 0

    // TODO Is there a way to make these setters common?
    private var goldsType = GoldsType.TENS
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
        activity?.title = getString(R.string.score_pad__title)

        scorePadViewModel = ViewModelProvider(this, ViewModelFactory {
            ScorePadViewModel(requireActivity().application, args.archerRoundId)
        }).get(ScorePadViewModel::class.java)

        // Get arrow counts and distances
        scorePadViewModel.archerRound.observe(viewLifecycleOwner, Observer { archerRound ->
            if (archerRound == null) return@Observer
            archerRound.roundId?.let { roundId ->
                scorePadViewModel.getArrowCountsForRound(roundId).observe(viewLifecycleOwner, {
                    arrowCounts = it
                })
                scorePadViewModel.getDistancesForRound(roundId, archerRound.roundSubTypeId)
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
            goldsType = getGoldsType(round.isOutdoor, round.isMetric)
        })

        // Get arrows
        scorePadViewModel.arrowsForRound.observe(viewLifecycleOwner, Observer { arrowValues ->
            if (arrowValues == null) return@Observer
            if (arrowValues.isNullOrEmpty()) {
                displayError()
                return@Observer
            }
            arrows = arrowValues
        })
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

        try {
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

            val tableData = calculateScorePadTableData(
                    arrows, endSize, goldsType, resources, arrowCounts, distances, distanceUnit
            )
            val arrowRowsShot = ceil(arrows.size / endSize.toDouble()).toInt()
            tableAdapter.setAllItems(
                    getColumnHeadersForTable(scorePadColumnHeaderIds, resources, goldsType),
                    generateNumberedRowHeaders(
                            if (arrowCounts.isNotEmpty()) {
                                arrowCounts.map { ceil(it.arrowCount / endSize.toDouble()).toInt() }
                            }
                            else {
                                listOf(arrowRowsShot)
                            },
                            arrowRowsShot,
                            resources,
                            true
                    ),
                    tableData
            )
        }
        catch (e: IllegalArgumentException) {
            displayError()
        }
    }

    private fun displayError() {
        if (dialog != null) {
            return
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.err_table_view__no_data)
        builder.setMessage(R.string.err_score_pad__no_arrows)
        builder.setPositiveButton(R.string.general_ok) { dialogInterface, _ ->
            activity?.onBackPressed()
            dialogInterface.cancel()
            dialog = null
        }
        dialog = builder.create()
        dialog!!.show()
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
                ActionBarHelp.HelpShowcaseItem(
                        null,
                        getString(R.string.help_score_pad__open_menu_title),
                        getString(R.string.help_table_open_menu_body)
                )
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
