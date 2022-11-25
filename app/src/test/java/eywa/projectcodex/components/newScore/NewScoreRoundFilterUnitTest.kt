package eywa.projectcodex.components.newScore

import eywa.projectcodex.components.newScore.helpers.NewScoreRoundFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class NewScoreRoundFilterUnitTest {
    private val paramProvider = NewScoreStatePreviewProvider()
    private val indoorMetricRound = with(paramProvider) { indoorMetricRoundData.getOnlyRound() }
    private val outdoorImperialRound = with(paramProvider) { outdoorImperialRoundData.getOnlyRound() }

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

