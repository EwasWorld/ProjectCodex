package eywa.projectcodex.components.archerRoundScore.scorePad.infoTable

import com.evrencoskun.tableview.sort.ISortableModel

class InfoTableCell(private val cellContent: Any?, private val cellId: String, val style: CellStyle? = null) :
        ISortableModel {
    override fun getContent(): Any? {
        return cellContent
    }

    override fun getId(): String {
        return cellId
    }

    override fun equals(other: Any?): Boolean {
        if (other !is InfoTableCell) {
            return false
        }
        return other.content == content && other.id == id
    }

    /**
     * Must be overridden because if two objects are equal, they must return the same hashcode
     */
    override fun hashCode(): Int {
        var result = content.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return cellId + ":" + cellContent.toString()
    }

    enum class CellStyle { BOLD }
}