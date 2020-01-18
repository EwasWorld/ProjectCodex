package eywa.projectcodex.infoTable

import com.evrencoskun.tableview.sort.ISortableModel

class InfoTableCell(content: Any?, id: String) : ISortableModel {
    private var cellContent = content
    private var cellId = id

    override fun getContent(): Any? {
        return cellContent
    }

    override fun getId(): String {
        return cellId
    }

    fun setContent(value: Any?) {
        cellContent = value
    }

    fun setId(value: String) {
        cellId = value
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
}