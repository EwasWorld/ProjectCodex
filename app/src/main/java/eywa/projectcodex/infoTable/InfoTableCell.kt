package eywa.projectcodex.infoTable

import ph.ingenuity.tableview.feature.sort.Sortable

class InfoTableCell(override var content: Any, override var id: String) : Sortable {
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
}