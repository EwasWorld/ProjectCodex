package eywa.projectcodex.common.sharedUi.selectRoundFaceDialog

import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round

data class SelectRoundFaceDialogState(
        val isShown: Boolean = false,
        /**
         * True: use the same face for all distances (if multiple distances)
         */
        val isSingleMode: Boolean = true,

        /**
         * The index in [distances] (sorted descending) that the dropdown is expanded for.
         * Null if no dropdown expanded
         */
        val dropdownExpandedFor: Int? = null,
        val selectedFaces: List<RoundFace>? = null,
        val distances: List<Int>? = null,
        val round: Round? = null,
) {
    init {
        check(
                (round == null && distances == null) || (round != null && !distances.isNullOrEmpty())
        ) { "If a round is given, distances must also be given, and vice versa" }
        check(
                selectedFaces.orEmpty().size <= 1 || selectedFaces.orEmpty().size == distances.orEmpty().size,
        ) { "selectedFaces size must be 0, 1, or distances.size" }
    }

    /**
     * Tidy up and validate [selectedFaces] for storage in the database
     */
    val finalFaces
        get() = when {
            selectedFaces.isNullOrEmpty() || selectedFaces.all { it == RoundFace.FULL } -> null
            selectedFaces.distinctBy { it }.size == 1 -> listOf(selectedFaces.first())
            selectedFaces.size != distances?.size -> throw IllegalStateException("Invalid faces size")
            else -> selectedFaces
        }

    val firstFaceAsSingleton
        get() = selectedFaces?.take(1)?.takeIf { it.isNotEmpty() }
}
