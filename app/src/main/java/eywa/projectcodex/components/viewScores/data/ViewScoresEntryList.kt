package eywa.projectcodex.components.viewScores.data

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.viewScores.screenUi.getDisplayName
import eywa.projectcodex.model.GoldsType
import eywa.projectcodex.model.PbType
import eywa.projectcodex.model.roundHandicap

data class ViewScoresEntryList(
        private val entries: List<ViewScoresEntry>,
        private val multiplePbType: PbType? = null
) {
    constructor(entry: ViewScoresEntry) : this(listOf(entry))

    init {
        check(entries.isNotEmpty()) { "Cannot create an empty list" }
        check(
                entries.distinctBy { it.info.use2023HandicapSystem }.size == 1,
        ) { "Must all use the same scoring system" }
    }

    val isMulti
        get() = entries.size > 1

    val size
        get() = entries.size

    val dateShot
        get() = entries.first().info.shoot.dateShot

    val arrowsShotWithoutSighters
        get() = entries.sumOf { it.info.arrowsShot }

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
        get() = entries.sumOf { it.golds(goldsType) }
    val goldsType
        get() = entries.first().info.goldsTypes[0]

    private val handicapFloat
        get() = entries
                .mapNotNull { it.info.handicapFloat }
                .takeIf { it.size == entries.size }
                ?.average()

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
                .map { it.info.shootRound to (it.info.h2h == null) }
                .distinctBy { (round, isH2h) -> round?.roundId to (round?.roundSubTypeId ?: 1) to isH2h }
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
                        semanticDisplayName = entry.info.semanticDisplayName,
                )
            }
            return ViewScoresRoundNameInfo(
                    displayName = entry.info.displayName,
                    strikethrough = entry.info.round != null && !entry.isRoundComplete(),
                    semanticDisplayName = entry.info.semanticDisplayName,
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
                    semanticDisplayName = entry.info.semanticDisplayName,
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

    val nameSemantics
        get() = listOfNotNull(
                firstDisplayName.takeIf { it.displayName != null }?.let { getDisplayName(it, useSemanticName = true) },
                secondDisplayName?.let { getDisplayName(it, useSemanticName = true) },
                totalUndisplayedNamesCount
                        ?.let { ResOrActual.StringResource(R.string.view_score__multiple_ellipses, listOf(it)) },
        )

    val hsgSemantics
        get() =
            if (hitsScoreGolds == null) {
                listOf(ResOrActual.StringResource(R.string.view_score__hsg_placeholder_semantics))
            }
            else {
                listOfNotNull(
                        ResOrActual.StringResource(R.string.view_score__score_semantics, listOf(score)),
                        ResOrActual.StringResource(
                                R.string.view_score__golds_semantics,
                                listOf(ResOrActual.StringResource(goldsType.longStringId), golds),
                        ),
                        ResOrActual.StringResource(R.string.view_score__hits_semantics, listOf(hits)),
                )
            }

    val h2hWinsLossesOther
        get() = entries
                .flatMap { it.info.h2h!!.matches }
                .map { it.result }
                .let { resultsList ->
                    val grouped = resultsList.groupBy { it }.mapValues { it.value.size }
                    val wins = (grouped[HeadToHeadResult.WIN] ?: 0)
                    val losses = (grouped[HeadToHeadResult.LOSS] ?: 0)

                    listOf(wins, losses, (resultsList.size - wins - losses))
                }
}
