package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.newScore.helpers.NewScoreRoundFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class NewScoreRoundFilterUnitTest {
    private val indoorMetricRound = RoundPreviewHelper.indoorMetricRoundData.round
    private val outdoorImperialRound = RoundPreviewHelper.outdoorImperialRoundData.round

    @Test
    fun testMetric() {
        assertEquals(true, NewScoreRoundFilter.METRIC.predicate(indoorMetricRound))
        assertEquals(false, NewScoreRoundFilter.METRIC.predicate(outdoorImperialRound))
    }

    @Test
    fun testImperial() {
        assertEquals(true, NewScoreRoundFilter.IMPERIAL.predicate(outdoorImperialRound))
        assertEquals(false, NewScoreRoundFilter.IMPERIAL.predicate(indoorMetricRound))
    }

    @Test
    fun testIndoor() {
        assertEquals(true, NewScoreRoundFilter.INDOOR.predicate(indoorMetricRound))
        assertEquals(false, NewScoreRoundFilter.INDOOR.predicate(outdoorImperialRound))
    }

    @Test
    fun testOutdoor() {
        assertEquals(true, NewScoreRoundFilter.OUTDOOR.predicate(outdoorImperialRound))
        assertEquals(false, NewScoreRoundFilter.OUTDOOR.predicate(indoorMetricRound))
    }
}

