package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundFilter.*
import eywa.projectcodex.database.rounds.Round
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectRoundEnabledFiltersUnitTest {
    private val indoorMetricRound = RoundPreviewHelper.indoorMetricRoundData.round
    private val outdoorImperialRound = RoundPreviewHelper.outdoorImperialRoundData.round
    private val rounds = listOf(
            indoorMetricRound,
            outdoorImperialRound,
    )

    @Test
    fun testAdd() {
        // Single
        assertEquals(
                listOf(indoorMetricRound),
                SelectRoundEnabledFilters().plus(METRIC).filter(rounds),
        )

        // Multiple with match
        assertEquals(
                listOf(indoorMetricRound),
                SelectRoundEnabledFilters().plus(setOf(METRIC, INDOOR)).filter(rounds),
        )

        // Multiple with no match
        assertEquals(
                listOf<Round>(),
                SelectRoundEnabledFilters().plus(setOf(METRIC, OUTDOOR)).filter(rounds),
        )

        // Sequential mutually exclusive
        assertEquals(
                listOf(outdoorImperialRound),
                SelectRoundEnabledFilters().plus(INDOOR).plus(OUTDOOR).filter(rounds),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAdd_MutuallyExclusive() {
        SelectRoundEnabledFilters().plus(setOf(INDOOR, OUTDOOR))
    }

    @Test
    fun testToggleAndContains() {
        var filters = SelectRoundEnabledFilters().plus(METRIC)
        assertEquals(listOf(indoorMetricRound), filters.filter(rounds))
        assertEquals(true, filters.contains(METRIC))
        assertEquals(false, filters.contains(OUTDOOR))

        filters = filters.toggle(OUTDOOR)
        assertEquals(listOf<Round>(), filters.filter(rounds))
        assertEquals(true, filters.contains(METRIC))
        assertEquals(true, filters.contains(OUTDOOR))

        filters = filters.toggle(OUTDOOR)
        assertEquals(listOf(indoorMetricRound), filters.filter(rounds))
        assertEquals(true, filters.contains(METRIC))
        assertEquals(false, filters.contains(OUTDOOR))
    }

    @Test
    fun testMinus() {
        var filters = SelectRoundEnabledFilters().plus(OUTDOOR)
        assertEquals(true, filters.contains(OUTDOOR))

        filters = filters.minus(OUTDOOR)
        assertEquals(false, filters.contains(OUTDOOR))
    }
}
