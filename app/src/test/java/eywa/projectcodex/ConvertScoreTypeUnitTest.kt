package eywa.projectcodex

import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.testUtils.TestData
import org.junit.Assert
import org.junit.Test

class ConvertScoreTypeUnitTest {
    @Test
    fun testConvertXsToTens() {
        Assert.assertEquals(
                // Only the X will become a 10
                listOf(TestData.ARROWS[10].toArrowValue(1, 11)),
                ConvertScoreType.XS_TO_TENS.convertScore(TestData.ARROWS.toList().toArrowValues())
        )
    }

    @Test
    fun testConvertToFiveZoneScore() {
        val expectedArrows = listOf(
                TestData.ARROWS[1].toArrowValue(1, 2), // Was 2
                TestData.ARROWS[3].toArrowValue(1, 4), // Was 4
                TestData.ARROWS[5].toArrowValue(1, 6), // Was 6
                TestData.ARROWS[7].toArrowValue(1, 8), // Was 8
                TestData.ARROWS[9].toArrowValue(1, 10), // Was 10
                TestData.ARROWS[9].toArrowValue(1, 11), // Was X
        )

        Assert.assertEquals(
                expectedArrows,
                ConvertScoreType.TO_FIVE_ZONE.convertScore(TestData.ARROWS.toList().toArrowValues())
        )
    }

    @Test
    fun testConvertXsToTensNoChanges() {
        Assert.assertEquals(
                listOf<ArrowValue>(),
                ConvertScoreType.XS_TO_TENS.convertScore(TestData.ARROWS.dropLast(1).toList().toArrowValues())
        )
    }

    @Test
    fun testConvertToFiveZoneScoreNoChanges() {
        val arrows = listOf(
                TestData.ARROWS[0],
                TestData.ARROWS[1],
                TestData.ARROWS[3],
                TestData.ARROWS[5],
                TestData.ARROWS[7],
                TestData.ARROWS[9]
        ).toArrowValues()

        Assert.assertEquals(listOf<ArrowValue>(), ConvertScoreType.TO_FIVE_ZONE.convertScore(arrows))
    }

    /**
     * Calls [Arrow.toArrowValue] on every item in the list, setting archerRoundId to 1 and arrowNumber incrementally
     * starting from 0
     */
    private fun List<Arrow>.toArrowValues(): List<ArrowValue> =
            this.mapIndexed { i, arrow -> arrow.toArrowValue(1, i) }
}