package eywa.projectcodex

/**
 * The minimum arrow value to be counted as a gold
 */
enum class GoldsType(private val score: Int, private val isX: Boolean, val colHeaderStringId: Int) {
    NINES(9, false, R.string.scorepad_golds_nines_header), TENS(10, false, R.string.scorepad_golds_tens_header),
    XS(10, true, R.string.scorepad_golds_xs_header);

    fun isGold(arrow: Arrow): Boolean {
        return arrow.score >= score && (arrow.isX || !isX)
    }
}