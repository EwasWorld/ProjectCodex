package eywa.projectcodex

import eywa.projectcodex.components.viewScores.dialogs.convertScoreDialog.ConvertScoreType
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.testUtils.TestData
import org.junit.Assert
import org.junit.Test

class ConvertScoreTypeUnitTest {
    @Test
    fun testConvertXsToTens() {
        Assert.assertEquals(
                // Only the X will become a 10
                listOf(TestData.ARROWS[10].asArrowScore(1, 11)),
                ConvertScoreType.XS_TO_TENS.convertScore(TestData.ARROWS.toList().toArrowScores())
        )
    }

    @Test
    fun testConvertToFiveZoneScore() {
        val expectedArrows = listOf(
                TestData.ARROWS[1].asArrowScore(1, 2), // Was 2
                TestData.ARROWS[3].asArrowScore(1, 4), // Was 4
                TestData.ARROWS[5].asArrowScore(1, 6), // Was 6
                TestData.ARROWS[7].asArrowScore(1, 8), // Was 8
                TestData.ARROWS[9].asArrowScore(1, 10), // Was 10
                TestData.ARROWS[9].asArrowScore(1, 11), // Was X
        )

        Assert.assertEquals(
                expectedArrows,
                ConvertScoreType.TO_FIVE_ZONE.convertScore(TestData.ARROWS.toList().toArrowScores())
        )
    }

    @Test
    fun testConvertXsToTensNoChanges() {
        Assert.assertEquals(
                listOf<DatabaseArrowScore>(),
                ConvertScoreType.XS_TO_TENS.convertScore(TestData.ARROWS.dropLast(1).toList().toArrowScores())
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
        ).toArrowScores()

        Assert.assertEquals(listOf<DatabaseArrowScore>(), ConvertScoreType.TO_FIVE_ZONE.convertScore(arrows))
    }

    /**
     * Calls [Arrow.asArrowScore] on every item in the list, setting shootId to 1 and arrowNumber incrementally
     * starting from 0
     */
    private fun List<Arrow>.toArrowScores(): List<DatabaseArrowScore> =
            this.mapIndexed { i, arrow -> arrow.asArrowScore(1, i) }
}
