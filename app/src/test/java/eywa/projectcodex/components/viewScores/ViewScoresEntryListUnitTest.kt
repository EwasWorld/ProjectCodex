package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.archeryObjects.PbType
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.data.ViewScoresEntryList
import eywa.projectcodex.components.viewScores.data.ViewScoresRoundNameInfo
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.clearArrows
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setPersonalBests
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setTiedPersonalBests
import eywa.projectcodex.database.rounds.RoundArrowCount
import org.junit.Assert.*
import org.junit.Test

class ViewScoresEntryListUnitTest {
    @Test
    fun testIsMulti() {
        assertTrue(ViewScoresEntryList(ViewScoresEntryPreviewProvider.generateEntries(2)).isMulti)
        assertFalse(ViewScoresEntryList(ViewScoresEntryPreviewProvider.generateEntries(1)).isMulti)
    }

    @Test
    fun testHitsScoreGolds() {
        // Single
        assertEquals(
                "36/217/12",
                ViewScoresEntryList(ViewScoresEntryPreviewProvider.generateEntries(1)).hitsScoreGolds
        )
        // Multi
        assertEquals(
                "72/502/17",
                ViewScoresEntryList(ViewScoresEntryPreviewProvider.generateEntries(3)).hitsScoreGolds
        )
        // Single no arrows
        assertEquals(
                null,
                ViewScoresEntryList(
                        ViewScoresEntryPreviewProvider.generateEntries(1).clearArrows(),
                ).hitsScoreGolds
        )
        // Multi no arrows
        assertEquals(
                null,
                ViewScoresEntryList(
                        ViewScoresEntryPreviewProvider.generateEntries(3).clearArrows(),
                ).hitsScoreGolds
        )
    }

    @Test
    fun testHandicap() {
        // TODO_CURRENT
    }

    @Test
    fun testPbTypes() {
        val entries = ViewScoresEntryPreviewProvider.generateEntries(3)

        assertEquals(null, ViewScoresEntryList(entries).allPbTypes)
        assertEquals(
                listOf(PbType.SINGLE),
                ViewScoresEntryList(entries.setPersonalBests(listOf(0))).allPbTypes
        )
        assertEquals(
                listOf(PbType.SINGLE_TIED),
                ViewScoresEntryList(entries.setPersonalBests(listOf(0)).setTiedPersonalBests(listOf(0))).allPbTypes
        )

        assertEquals(listOf(PbType.MULTI), ViewScoresEntryList(entries, PbType.MULTI).allPbTypes)
        assertEquals(listOf(PbType.MULTI_TIED), ViewScoresEntryList(entries, PbType.MULTI_TIED).allPbTypes)

        assertEquals(
                listOf(PbType.MULTI, PbType.SINGLE_TIED),
                ViewScoresEntryList(
                        entries.setPersonalBests(listOf(0)).setTiedPersonalBests(listOf(0)),
                        PbType.MULTI,
                ).allPbTypes
        )
        assertEquals(
                listOf(PbType.MULTI, PbType.SINGLE),
                ViewScoresEntryList(
                        entries.setPersonalBests(listOf(0, 1)).setTiedPersonalBests(listOf(0)),
                        PbType.MULTI,
                ).allPbTypes
        )
    }

    @Test
    fun testAllRoundsIdentical() {
        val entries = ViewScoresEntryPreviewProvider.generateEntries(6)

        assertFalse(ViewScoresEntryList(entries).allRoundsIdentical)
        assertTrue(ViewScoresEntryList(entries.setAllRoundsIdentical()).allRoundsIdentical)
    }

    @Test
    fun testFirstDisplayName() {
        val entries = ViewScoresEntryPreviewProvider.generateEntries(4)
        val incomplete = ViewScoresEntryPreviewProvider.generateIncompleteRound()

        // Single
        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = "Metric II / Cadet Ladies 1440",
                        strikethrough = false,
                        identicalCount = 1,
                        prefixWithAmpersand = false,
                ),
                ViewScoresEntryList(entries.take(1)).firstDisplayName,
        )
        // Multiple mismatched
        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = "Metric II / Cadet Ladies 1440",
                        strikethrough = false,
                        identicalCount = 1,
                        prefixWithAmpersand = false,
                ),
                ViewScoresEntryList(entries).firstDisplayName,
        )
        // Unfinished
        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = "Short Junior National",
                        strikethrough = true,
                        identicalCount = 1,
                        prefixWithAmpersand = false,
                ),
                ViewScoresEntryList(listOf(incomplete)).firstDisplayName,
        )
        // Multiple matched
        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = "Metric II / Cadet Ladies 1440",
                        strikethrough = false,
                        identicalCount = 3,
                        prefixWithAmpersand = false,
                ),
                ViewScoresEntryList(List(3) { entries.first() }).firstDisplayName,
        )
        // Multiple matched unfinished
        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = "York",
                        strikethrough = true,
                        identicalCount = 3,
                        prefixWithAmpersand = false,
                ),
                ViewScoresEntryList(
                        List(3) { if (it == 2) incomplete else entries.first() }
                                .setAllRoundsIdentical()
                ).firstDisplayName,
        )
        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = "York",
                        strikethrough = true,
                        identicalCount = 3,
                        prefixWithAmpersand = false,
                ),
                ViewScoresEntryList(
                        List(3) { if (it == 1) incomplete else entries.first() }
                                .setAllRoundsIdentical()
                ).firstDisplayName,
        )
    }

    @Test
    fun testSecondDisplayName() {
        val entries = ViewScoresEntryPreviewProvider.generateEntries(3)

        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = null,
                        strikethrough = false,
                        identicalCount = 1,
                        prefixWithAmpersand = true,
                ),
                ViewScoresEntryList(entries).secondDisplayName,
        )
        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = "Hereford",
                        strikethrough = false,
                        identicalCount = 1,
                        prefixWithAmpersand = true,
                ),
                ViewScoresEntryList(entries.drop(1)).secondDisplayName,
        )
        assertEquals(
                ViewScoresRoundNameInfo(
                        displayName = "Short Junior National",
                        strikethrough = true,
                        identicalCount = 1,
                        prefixWithAmpersand = true,
                ),
                ViewScoresEntryList(
                        listOf(entries.first(), ViewScoresEntryPreviewProvider.generateIncompleteRound())
                ).secondDisplayName,
        )

        // Identical entries
        assertEquals(
                null,
                ViewScoresEntryList(entries.setAllRoundsIdentical()).secondDisplayName,
        )

        // Too few
        assertEquals(
                null,
                ViewScoresEntryList(ViewScoresEntryPreviewProvider.generateEntries(1)).secondDisplayName,
        )
    }

    @Test
    fun testTotalUndisplayedNamesCount() {
        val entries = ViewScoresEntryPreviewProvider.generateEntries(6)

        assertEquals(4, ViewScoresEntryList(entries).totalUndisplayedNamesCount)

        // Identical entries
        assertEquals(
                null,
                ViewScoresEntryList(entries.setAllRoundsIdentical()).totalUndisplayedNamesCount,
        )

        // Too few
        assertEquals(
                null,
                ViewScoresEntryList(ViewScoresEntryPreviewProvider.generateEntries(1)).totalUndisplayedNamesCount,
        )
        assertEquals(
                null,
                ViewScoresEntryList(ViewScoresEntryPreviewProvider.generateEntries(2)).totalUndisplayedNamesCount,
        )
    }

    // TODO Why is null round causing test to not respond?
    private fun List<ViewScoresEntry>.setAllRoundsIdentical() = filter { it.info.round != null }
            .map {
                it.copy(
                        info = it.info.addRound(
                                RoundPreviewHelper.outdoorImperialRoundData.copy(
                                        roundArrowCounts = listOf(
                                                RoundArrowCount(1, 1, 120f, 72),
                                                RoundArrowCount(1, 2, 120f, 72),
                                        )
                                )
                        )
                )
            }
}
