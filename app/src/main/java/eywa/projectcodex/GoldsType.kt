package eywa.projectcodex

import eywa.projectcodex.database.entities.ArrowValue

/**
 * The minimum arrow value to be counted as a gold
 */
enum class GoldsType(private val score: Int, private val isX: Boolean, val colHeaderStringId: Int) {
    NINES(9, false, R.string.table_golds_nines_header), TENS(10, false, R.string.table_golds_tens_header),
    XS(10, true, R.string.table_golds_xs_header);

    fun isGold(arrow: Arrow): Boolean {
        return isGold(arrow.score, arrow.isX)
    }

    fun isGold(arrow: ArrowValue): Boolean {
        return isGold(arrow.score, arrow.isX)
    }

    fun isGold(arrowScore: Int, arrowIsX: Boolean): Boolean {
        return arrowScore >= score && (arrowIsX || !isX)
    }
}