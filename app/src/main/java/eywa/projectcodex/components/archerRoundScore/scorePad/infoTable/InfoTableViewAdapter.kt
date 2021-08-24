package eywa.projectcodex.components.archerRoundScore.scorePad.infoTable

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import eywa.projectcodex.R
import java.util.*


class InfoTableViewAdapter(private val context: Context) :
        AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>() {
    override fun getCellItemViewType(column: Int): Int = ItemViewType.CELL.ordinal
    override fun getColumnHeaderItemViewType(column: Int): Int = ItemViewType.COLUMN_HEADER.ordinal
    override fun getRowHeaderItemViewType(row: Int): Int = ItemViewType.ROW_HEADER.ordinal

    override fun onBindCellViewHolder(holder: AbstractViewHolder, cell: InfoTableCell?, column: Int, row: Int) {
        (holder as InfoTableViewHolder).setCell(cell)
    }

    override fun onBindColumnHeaderViewHolder(holder: AbstractViewHolder, colHeaderCell: InfoTableCell?, column: Int) {
        (holder as InfoTableViewHolder).setCell(colHeaderCell)
    }

    override fun onBindRowHeaderViewHolder(holder: AbstractViewHolder, rowHeaderCell: InfoTableCell?, row: Int) {
        (holder as InfoTableViewHolder).setCell(rowHeaderCell)
    }

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val cellView = LayoutInflater.from(context).inflate(
                R.layout.table_cell_data,
                parent,
                false
        )
        return InfoTableViewHolder(cellView, viewType)
    }

    override fun onCreateColumnHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val columnHeaderView = LayoutInflater.from(context).inflate(
                R.layout.table_column_header_data,
                parent,
                false
        )
        return InfoTableViewHolder(columnHeaderView, viewType)
    }

    override fun onCreateRowHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val rowHeaderView = LayoutInflater.from(context).inflate(
                R.layout.table_row_header_data,
                parent,
                false
        )
        return InfoTableViewHolder(rowHeaderView, viewType)
    }

    override fun onCreateCornerView(parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.table_corner_view, parent, false)
    }

    private enum class ItemViewType(val textViewId: Int, val containerId: Int) {
        CELL(R.id.text_info_table_cell_data, R.id.layout_info_table_cell_container),
        COLUMN_HEADER(R.id.text_info_table_column_header_data, R.id.layout_info_table_column_header_container),
        ROW_HEADER(R.id.text_info_table_row_header_data, R.id.layout_info_table_row_header_container)
    }

    open class InfoTableViewHolder(itemView: View, private val viewType: Int) : AbstractViewHolder(itemView) {
        private val cellContainer: LinearLayout
        private val cellTextView: TextView
        var isTotalCell = false
            private set

        init {
            val itemViewType = ItemViewType.values()[viewType]
            cellContainer = itemView.findViewById(itemViewType.containerId)
            cellTextView = itemView.findViewById(itemViewType.textViewId)
        }

        fun setCell(cell: InfoTableCell?) {
            cellTextView.text = cell?.content.toString()

            if (viewType != ItemViewType.COLUMN_HEADER.ordinal && cell?.id != null) {
                if (cell.id.lowercase(Locale.ROOT).contains(TOTAL_CELL_ID.lowercase(Locale.ROOT))) {
                    isTotalCell = true
                    cellTextView.setTypeface(cellTextView.typeface, Typeface.BOLD)
                }
            }

            if (viewType != ItemViewType.ROW_HEADER.ordinal) {
                // Required if the TableView uses resizing (see comment in samples given below)
                // https://github.com/evrencoskun/TableView/blob/master/app/src/main/java/com/evrencoskun/tableviewsample/tableview/holder
                cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                cellTextView.requestLayout()
            }
        }
    }
}