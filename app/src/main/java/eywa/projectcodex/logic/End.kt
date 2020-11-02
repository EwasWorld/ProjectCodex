package eywa.projectcodex.logic

import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.exceptions.UserException
import eywa.projectcodex.viewModels.InputEndViewModel
import kotlin.math.min

class End(arrowsPerEnd: Int, private val arrowPlaceholder: String, private val arrowDeliminator: String) {
    private var arrows = mutableListOf<Arrow>()
    var endSize = arrowsPerEnd

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
     * @param deleteContents if true, will truncate [arrows] if necessary before updating [endSize]. If false, will
     * throw an IllegalArgumentException
     * @throws UserException if [originalEnd] != null
     * @throws IllegalArgumentException if [value] < [arrows].size (unless [deleteContents] is true)
     */
    fun updateEndSize(value: Int, deleteContents: Boolean) {
        if (originalEnd != null) {
            throw UserException(R.string.err_input_end__cannot_edit_end_size)
        }
        if (deleteContents) {
            endSize = value
            if (arrows.isNotEmpty()) {
                arrows = arrows.subList(0, min(arrows.size, endSize))
            }
            return
        }

        if (arrows.size >= value) {
            endSize = value
        }
        else {
            throw IllegalArgumentException("New end size is too small for arrows currently added to the end")
        }
    }

    fun isEditEnd(): Boolean {
        return originalEnd != null
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
        check(arrows.size != endSize) { "End full" }
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
        while (arrowStrings.size < endSize) {
            arrowStrings.add(arrowPlaceholder)
        }
        return arrowStrings.joinToString(arrowDeliminator)
    }

    fun clear() {
        arrows = mutableListOf()
    }

    fun reset() {
        check(originalEnd != null) { "No original end state was provided" }
        arrows = originalEnd!!.map { Arrow(it.score, it.isX) }.toMutableList()
    }

    /**
     * Updates the archer-round ID and arrow IDs for all arrows in the end then adds them to the database and clears the
     * end
     *
     * @param archerRoundId the archer-round ID to assign to each arrow. Not required if [originalEnd].size ==
     * [endSize]
     * @param firstArrowId the arrow number to assign to the first arrow in the end, subsequent arrows increment on
     * this. Not required if [originalEnd].size == [endSize]
     * @param inputEndViewModel the database accessor
     * @throws UserException if the end is not full
     * @throws IllegalStateException if [archerRoundId] or [firstArrowId] is invalid
     */
    fun addArrowsToDatabase(archerRoundId: Int?, firstArrowId: Int?, inputEndViewModel: InputEndViewModel) {
        val origArcherRoundIds = originalEnd?.map { it.archerRoundId }?.distinct()
        val finalArcherRoundId = origArcherRoundIds?.get(0) ?: archerRoundId
        check(finalArcherRoundId != null) { "Must provide archerRoundId" }

        // TODO Make sure these requirements are thoroughly unit tested
        if (arrows.size != endSize) throw UserException(
                R.string.err_input_end__end_not_full
        )
        if (originalEnd == null) {
            check(firstArrowId != null) { "Must provide firstArrowId" }
        }
        else {
            val originalEnd = originalEnd!!
            check(origArcherRoundIds!!.size == 1) { "originalEnd contains arrow values from multiple rounds" }
            check(archerRoundId == finalArcherRoundId) { "archerRoundId doesn't match those in the database" }

            check(originalEnd.size == arrows.size || firstArrowId != null) {
                "Must provide firstArrowId or match the original end size"
            }
        }

        /*
         * Overwrite the scores that were in the originalEnd
         */
        if (!originalEnd.isNullOrEmpty()) {
            val originalEnd = originalEnd!!
            for (i in originalEnd.indices) {
                inputEndViewModel.update(
                        ArrowValue(
                                originalEnd[i].archerRoundId, originalEnd[i].arrowNumber, arrows[i].score, arrows[i].isX
                        )
                )
            }
        }
        /*
         * Else add the new arrows
         */
        else {
            var arrowID = firstArrowId!!
            for (arrow in arrows) {
                inputEndViewModel.insert(arrow.toArrowValue(finalArcherRoundId, arrowID++))
            }
        }
        clear()
    }
}