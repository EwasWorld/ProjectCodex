package eywa.projectcodex.components.viewScores.data

import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.archeryObjects.PbType
import eywa.projectcodex.common.archeryObjects.roundHandicap

data class ViewScoresEntryList(
        private val entries: List<ViewScoresEntry>,
        private val multiplePbType: PbType? = null
) {
    constructor(entry: ViewScoresEntry) : this(listOf(entry))

    init {
        check(entries.isNotEmpty()) { "Cannot create an empty list" }
        check(
                entries.distinctBy { it.info.use2023HandicapSystem }.size == 1
        ) { "Must all use the same scoring system" }
    }

    val isMulti
        get() = entries.size > 1

    val size
        get() = entries.size

    val dateShot
        get() = entries.first().info.archerRound.dateShot

    /**
     * Note golds will use the [GoldsType] of the first [entries]
     */
    val hitsScoreGolds
        get() = listOf(hits, score, golds)
                .takeIf { entries.any { it.info.arrowsShot > 0 } }
                ?.joinToString("/")

    val hits
        get() = entries.sumOf { it.info.hits }
    val score
        get() = entries.sumOf { it.info.score }
    val golds
        get() = entries.sumOf { it.golds(entries.first().info.goldsType) }

    val handicapFloat
        get() = entries
                .mapNotNull { it.info.handicapFloat }
                .takeIf { it.size == entries.size }
                ?.average()
                ?.toDouble()

    val handicap
        get() = handicapFloat?.roundHandicap()

    val use2023System
        get() = entries.first().info.use2023HandicapSystem

    private val singlePbType
        get() = entries
                .mapNotNull { it.info.pbType }
                .takeIf { it.isNotEmpty() }
                ?.distinct()
                ?.let {
                    val final = if (it.contains(PbType.SINGLE)) it.minus(PbType.SINGLE_TIED) else it
                    check(final.size == 1) { "Multiple PbTypes detected" }
                    final.first()
                }

    /**
     * In order of most important
     */
    val allPbTypes = listOfNotNull(multiplePbType, singlePbType).takeIf { it.isNotEmpty() }

    val allRoundsIdentical
        get() = entries
                .map { it.info.archerRound }
                .distinctBy { it.roundId to (it.roundSubTypeId ?: 1) }
                .size == 1

    private fun allHaveRounds() = entries.all { it.info.round != null }
    private fun allRoundFinished() = entries.all { it.isRoundComplete() }

    val firstDisplayName: ViewScoresRoundNameInfo
        get() {
            val entry = entries.first()

            if (allRoundsIdentical && entries.size > 1) {
                return ViewScoresRoundNameInfo(
                        displayName = entry.info.displayName,
                        strikethrough = allHaveRounds() && !allRoundFinished(),
                        identicalCount = entries.size,
                )
            }
            return ViewScoresRoundNameInfo(
                    displayName = entry.info.displayName,
                    strikethrough = entry.info.round != null && !entry.isRoundComplete(),
            )
        }

    val secondDisplayName: ViewScoresRoundNameInfo?
        get() {
            if (allRoundsIdentical) return null
            val entry = entries.getOrNull(1) ?: return null
            return ViewScoresRoundNameInfo(
                    displayName = entry.info.displayName,
                    strikethrough = entry.info.round != null && !entry.isRoundComplete(),
                    prefixWithAmpersand = true,
            )
        }

    /**
     * The number of rounds that aren't covered by [firstDisplayName] and [secondDisplayName].
     *
     * Will be 0 if all rounds are identical and therefore covered by [firstDisplayName].
     * Will be 0 if [entries].size <= 2.
     */
    val totalUndisplayedNamesCount
        get() = (entries.size - 2).takeIf { !allRoundsIdentical && it > 0 }
}
