package eywa.projectcodex.infoTable

import android.content.Context
import android.graphics.Typeface
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import eywa.projectcodex.R
import java.util.*


class InfoTableViewAdapter(private val context: Context) :
        AbstractTableAdapter<InfoTableCell, InfoTableCell, InfoTableCell>() {
    class InfoTableCellViewHolder(itemView: View) : AbstractViewHolder(itemView), View.OnCreateContextMenuListener {
        val cellTextView: TextView
            get() = itemView.findViewById(R.id.text_info_table_cell_data)

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {}
    }

    class InfoTableColumnHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cellTextView: TextView
            get() = itemView.findViewById(R.id.text_info_table_column_header)
    }

    class InfoTableHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cellTextView: TextView
            get() = itemView.findViewById(R.id.text_info_table_row_header)
    }

    override fun getCellItemViewType(column: Int): Int = 0

    override fun getColumnHeaderItemViewType(column: Int): Int = 0

    override fun getRowHeaderItemViewType(row: Int): Int = 0

    override fun onBindCellViewHolder(holder: AbstractViewHolder, cell: InfoTableCell?, column: Int, row: Int) {
        val cellViewHolder = holder as InfoTableCellViewHolder
        cellViewHolder.cellTextView.text = cell?.content.toString()
        if (cell?.id != null) {
            setBoldIfTotal(cell.id, cellViewHolder.cellTextView)
        }
    }

    private fun setBoldIfTotal(cellId: String, cellTextView: TextView) {
        if (cellId.toLowerCase(Locale.ROOT).contains(TOTAL_CELL_ID.toLowerCase(Locale.ROOT))) {
            cellTextView.setTypeface(cellTextView.typeface, Typeface.BOLD)
        }
    }

    override fun onBindColumnHeaderViewHolder(holder: AbstractViewHolder, columnHeaderCell: InfoTableCell?, column: Int) {
        val columnHeaderViewHolder = holder as InfoTableColumnHeaderViewHolder
        columnHeaderViewHolder.cellTextView.text = columnHeaderCell?.content.toString()
    }

    override fun onBindRowHeaderViewHolder(holder: AbstractViewHolder, rowHeaderCell: InfoTableCell?, row: Int) {
        val rowHeaderViewHolder = holder as InfoTableHeaderViewHolder
        rowHeaderViewHolder.cellTextView.text = rowHeaderCell?.content.toString()
        if (rowHeaderCell?.id != null) {
            setBoldIfTotal(rowHeaderCell.id, rowHeaderViewHolder.cellTextView)
        }
    }

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val cellView = LayoutInflater.from(context).inflate(
                R.layout.table_cell_data,
                parent,
                false
        )
        return InfoTableCellViewHolder(cellView)
    }

    override fun onCreateColumnHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val columnHeaderView = LayoutInflater.from(context).inflate(
                R.layout.table_column_header_data,
                parent,
                false
        )
        return InfoTableColumnHeaderViewHolder(columnHeaderView)
    }

    override fun onCreateCornerView(parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.table_corner_view, parent, false)
    }

    override fun onCreateRowHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val rowHeaderView = LayoutInflater.from(context).inflate(
                R.layout.table_row_header_data,
                parent,
                false
        )
        return InfoTableHeaderViewHolder(rowHeaderView)
    }
}