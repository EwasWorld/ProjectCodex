package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectRoundFilterUnitTest {
    private val indoorMetricRound = RoundPreviewHelper.indoorMetricRoundData.round
    private val outdoorImperialRound = RoundPreviewHelper.outdoorImperialRoundData.round

    @Test
    fun testMetric() {
        assertEquals(true, SelectRoundFilter.METRIC.predicate(indoorMetricRound))
        assertEquals(false, SelectRoundFilter.METRIC.predicate(outdoorImperialRound))
    }

    @Test
    fun testImperial() {
        assertEquals(true, SelectRoundFilter.IMPERIAL.predicate(outdoorImperialRound))
        assertEquals(false, SelectRoundFilter.IMPERIAL.predicate(indoorMetricRound))
    }

    @Test
    fun testIndoor() {
        assertEquals(true, SelectRoundFilter.INDOOR.predicate(indoorMetricRound))
        assertEquals(false, SelectRoundFilter.INDOOR.predicate(outdoorImperialRound))
    }

    @Test
    fun testOutdoor() {
        assertEquals(true, SelectRoundFilter.OUTDOOR.predicate(outdoorImperialRound))
        assertEquals(false, SelectRoundFilter.OUTDOOR.predicate(indoorMetricRound))
    }
}

