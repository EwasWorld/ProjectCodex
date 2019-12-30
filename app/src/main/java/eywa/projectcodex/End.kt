package eywa.projectcodex

import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.viewModels.InputEndViewModel

class End(val arrowsPerEnd: Int, private val arrowPlaceholder: String, private val arrowDeliminator: String) {
    private var arrows: MutableList<Arrow> = mutableListOf()

    constructor(arrows: List<ArrowValue>, arrowsPerEnd: Int, arrowPlaceholder: String, arrowDeliminator: String) :
            this(arrowsPerEnd, arrowPlaceholder, arrowDeliminator) {
        require(arrows.size <= arrowsPerEnd) { "Too many arrows provided" }
        for (arrow in arrows) {
            addArrowToEnd(Arrow(arrow.score, arrow.isX))
        }
    }

    /**
     * @return the total score for the end
     */
    fun getScore(): Int {
        var total = 0
        for (arrow in arrows) {
            total += arrow.score
        }
        return total
    }

    /**
     * @return the number of hits in the end
     */
    fun getHits(): Int {
        return arrows.count { arrow -> arrow.score != 0 }
    }

    /**
     * @param goldsType what is the minimum value to be counted as a gold
     * @return the number of golds in the end
     */
    fun getGolds(goldsType: GoldsType): Int {
        return arrows.count { arrow -> goldsType.isGold(arrow) }
    }

    /**
     * @param arrow the arrow value to add to the end
     * @throws IllegalStateException if the end is full
     */
    fun addArrowToEnd(arrow: Arrow) {
        check(arrows.size != arrowsPerEnd) { "End full" }
        arrows.add(arrow)
    }

    /**
     * @param arrow the arrow value to add to the end
     * @throws IllegalStateException if the end is full
     */
    fun addArrowToEnd(arrow: String) {
        addArrowToEnd(Arrow(arrow))
    }

    /**
     * @throws IllegalStateException if the end is empty
     */
    fun removeLastArrowFromEnd() {
        check(arrows.isNotEmpty()) { "End empty" }
        arrows.removeAt(arrows.size - 1)
    }

    /**
     * Sorts the end's arrows into descending order
     */
    fun reorderScores() {
        arrows.sortWith(Comparator { a, b ->
            when {
                a.score > b.score -> -1
                a.score < b.score -> 1
                a.score != 10 -> 0
                a.isX -> -1
                else -> 1
            }
        })
    }

    /**
     * @return the end as a string to display
     */
    override fun toString(): String {
        val arrowStrings = mutableListOf<String>()
        // Arrows to strings
        for (arrow in arrows) {
            arrowStrings.add(arrow.toString())
        }
        // Fill end
        while (arrowStrings.size < arrowsPerEnd) {
            arrowStrings.add(arrowPlaceholder)
        }
        return arrowStrings.joinToString(arrowDeliminator)
    }

    fun clear() {
        arrows = mutableListOf()
    }

    /**
     * Updates the archer-round ID and arrow IDs for all arrows in the end then adds them to the database and clears the end
     *
     * @param archerRoundId the archer-round ID to assign to each arrow
     * @param firstArrowId the arrow number to assign to the first arrow in the end, subsequent arrows increment on this
     * @param scoresViewModel the database accessor
     * @throws IllegalStateException if the end is not full
     */
    fun addArrowsToDatabase(archerRoundId: Int, firstArrowId: Int, scoresViewModel: InputEndViewModel) {
        check(arrows.size == arrowsPerEnd) { "End not full" }
        var arrowID = firstArrowId
        for (arrow in arrows) {
            scoresViewModel.insert(ArrowValue(archerRoundId, arrowID++, arrow.score, arrow.isX))
        }
        clear()
    }
}