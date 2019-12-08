package eywa.projectcodex

import eywa.projectcodex.database.ScoresViewModel
import eywa.projectcodex.database.entities.ArrowValue

class End(private val arrowsPerEnd: Int, private val arrowPlaceholder: String, private val arrowDeliminator: String) {
    private var arrows: MutableList<Arrow> = mutableListOf()

    /**
     * @return the total score for the end
     */
    fun getEndScore(): Int {
        var total = 0
        for (arrow in arrows) {
            total += arrow.score
        }
        return total
    }

    /**
     * @param arrow the arrow value to add to the end
     * @throws NullPointerException if the end is full
     */
    fun addArrowToEnd(arrow: Arrow) {
        if (arrows.size == arrowsPerEnd) {
            throw NullPointerException("End full")
        }
        arrows.add(arrow)
    }

    /**
     * @param arrow the arrow value to add to the end
     * @throws NullPointerException if the end is full
     */
    fun addArrowToEnd(arrow: String) {
        addArrowToEnd(Arrow(arrow))
    }

    /**
     * @throws NullPointerException if the end is empty
     */
    fun removeLastArrowFromEnd() {
        if (arrows.isEmpty()) {
            throw NullPointerException("End empty")
        }
        arrows.removeAt(arrows.size - 1)
    }

    /**
     * Sorts the end's arrows into ascending order
     */
    fun reorderScores() {
        arrows.sortWith(Comparator { a, b ->
            when {
                a.score > b.score -> 1
                a.score < b.score -> -1
                a.score != 10 -> 0
                a.isX -> 1
                else -> -1
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
     * @param archerRoundsID the archer-round ID to assign to each arrow
     * @param firstArrowID the arrow number to assign to the first arrow in the end, subsequent arrows increment on this
     * @param scoresViewModel the database accessor
     */
    fun addArrowsToDatabase(archerRoundsID: Int, firstArrowID: Int, scoresViewModel: ScoresViewModel) {
        if (arrows.size != arrowsPerEnd) {
            throw NullPointerException("End not full")
        }
        var arrowID = firstArrowID
        for (arrow in arrows) {
            scoresViewModel.insert(ArrowValue(archerRoundsID, arrowID++, arrow.score, arrow.isX))
        }
        clear()
    }
}