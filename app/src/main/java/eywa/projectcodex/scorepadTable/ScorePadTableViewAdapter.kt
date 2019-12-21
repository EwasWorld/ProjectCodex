package eywa.projectcodex.scorepadTable

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eywa.projectcodex.R
import ph.ingenuity.tableview.adapter.AbstractTableAdapter
import ph.ingenuity.tableview.adapter.recyclerview.holder.AbstractViewHolder


class ScorePadTableViewAdapter(private val context: Context) : AbstractTableAdapter(context) {
    class ScorePadCellViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cellTextView: TextView
            get() = itemView.findViewById(R.id.text_scorepad_cell_data)
    }

    class ScorePadColumnHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cellTextView: TextView
            get() = itemView.findViewById(R.id.text_scorepad_column_header)
    }

    class ScorePadHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cellTextView: TextView
            get() = itemView.findViewById(R.id.text_scorepad_row_header)
    }

    override fun getCellItemViewType(column: Int): Int = 0

    override fun getColumnHeaderItemViewType(column: Int): Int = 0

    override fun getRowHeaderItemViewType(row: Int): Int = 0

    override fun onBindCellViewHolder(holder: AbstractViewHolder, cellItem: Any, column: Int, row: Int) {
        val cell = cellItem as ScorePadCell
        val cellViewHolder = holder as ScorePadCellViewHolder
        cellViewHolder.cellTextView.text = cell.content.toString()
    }

    override fun onBindColumnHeaderViewHolder(holder: AbstractViewHolder, columnHeaderItem: Any, column: Int) {
        val columnHeaderCell = columnHeaderItem as ScorePadCell
        val columnHeaderViewHolder = holder as ScorePadColumnHeaderViewHolder
        columnHeaderViewHolder.cellTextView.text = columnHeaderCell.content.toString()
    }

    override fun onBindRowHeaderViewHolder(holder: AbstractViewHolder, rowHeaderItem: Any, row: Int) {
        val rowHeaderCell = rowHeaderItem as ScorePadCell
        val rowHeaderViewHolder = holder as ScorePadHeaderViewHolder
        rowHeaderViewHolder.cellTextView.text = rowHeaderCell.content.toString()
    }

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(context).inflate(
                R.layout.table_cell_data,
                parent,
                false
        )
        return ScorePadCellViewHolder(cellView)
    }

    override fun onCreateColumnHeaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val columnHeaderView = LayoutInflater.from(context).inflate(
                R.layout.table_column_header_data,
                parent,
                false
        )
        return ScorePadColumnHeaderViewHolder(columnHeaderView)
    }

    override fun onCreateCornerView(): View? {
        return LayoutInflater.from(context).inflate(R.layout.table_corner_view, null)
    }

    override fun onCreateRowHeaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rowHeaderView = LayoutInflater.from(context).inflate(
                R.layout.table_row_header_data,
                parent,
                false
        )
        return ScorePadHeaderViewHolder(rowHeaderView)
    }
}