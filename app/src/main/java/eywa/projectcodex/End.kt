package eywa.projectcodex

import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.viewModels.InputEndViewModel

class End(val arrowsPerEnd: Int, private val arrowPlaceholder: String, private val arrowDeliminator: String) {
    private var arrows = mutableListOf<Arrow>()

    /**
     * Used if the end is already represented in the database. This stores the arrows as they are in the database so
     * they can be overwritten when the end is pushed to the database
     */
    private var originalEnd: List<ArrowValue>? = null

    /**
     * @param arrowsList size must be <= [arrowsPerEnd] and must all belong to the same archerRound
     */
    constructor(arrowsList: List<ArrowValue>, arrowsPerEnd: Int, arrowPlaceholder: String, arrowDeliminator: String) :
            this(arrowsPerEnd, arrowPlaceholder, arrowDeliminator) {
        require(arrowsList.size <= arrowsPerEnd) { "Too many arrows provided" }
        require(arrowsList.map { it.archerRoundId }.distinct().size == 1) { "Arrows must be from the same round" }
        for (arrow in arrowsList) {
            addArrowToEnd(Arrow(arrow.score, arrow.isX))
        }
        originalEnd = arrowsList
    }

    /**
     * @return the total score for the end
     */
    fun getScore(): Int {
        return arrows.sumBy { it.score }
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
     * Updates the archer-round ID and arrow IDs for all arrows in the end then adds them to the database and clears the
     * end
     *
     * @param archerRoundId the archer-round ID to assign to each arrow. Not required if [originalEnd].size ==
     * [arrowsPerEnd]
     * @param firstArrowId the arrow number to assign to the first arrow in the end, subsequent arrows increment on
     * this. Not required if [originalEnd].size == [arrowsPerEnd]
     * @param inputEndViewModel the database accessor
     * @throws IllegalStateException if the end is not full
     */
    fun addArrowsToDatabase(archerRoundId: Int?, firstArrowId: Int?, inputEndViewModel: InputEndViewModel) {
        check(arrows.size == arrowsPerEnd) { "End not full" }
        check((firstArrowId != null && archerRoundId != null) || originalEnd?.size == arrowsPerEnd) {
            "Must provide archerRoundId and firstArrowId (end was not created sufficiently)"
        }
        val origArcherRoundIds = originalEnd?.map { it.archerRoundId }?.distinct()
        check(archerRoundId == null || (origArcherRoundIds != null && archerRoundId == origArcherRoundIds[0])) {
            "archerRoundId doesn't match those in the database"
        }

        var arrowsToAdd = arrows
        /*
         * Overwrite the scores that were in the originalEnd
         */
        originalEnd?.let { end ->
            if (end.isNotEmpty()) {
                for (i in end.indices) {
                    inputEndViewModel.update(
                            ArrowValue(
                                    end[i].archerRoundId, end[i].arrowNumber, arrowsToAdd[i].score, arrowsToAdd[i].isX
                            )
                    )
                }
                arrowsToAdd = arrowsToAdd.subList(end.size, arrowsToAdd.size)
            }
        }
        if (arrowsToAdd.isNotEmpty()) {
            /*
             * Some or all arrows are to be newly added meaning archerRoundId && firstArrowId != null
             *
             * As adding arrows to the database requires a full end, it should only be possible for *some* arrows to be
             *   added like this if it's the last end being edited and arrowsPerEnd has changed from when the arrows
             *   were originally added to the database. e.g. if arrowsPerEnd is 3s then it's changed to 6s but the
             *   current number of arrows is not divisible by 6, you'll update 3 above and then add 3 here
             */
            var arrowID = firstArrowId!!
            for (arrow in arrowsToAdd) {
                inputEndViewModel.insert(arrow.toArrowValue(archerRoundId!!, arrowID++))
            }
        }
        clear()
    }
}