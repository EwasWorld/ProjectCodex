package eywa.projectcodex

import eywa.projectcodex.common.TestData
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.components.viewScores.ConvertScore
import eywa.projectcodex.database.arrowValue.ArrowValue
import kotlinx.coroutines.Job
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.mock

class ConvertScoreUnitTest {
    @Captor
    private lateinit var arrowValueCaptor: ArgumentCaptor<ArrowValue>
    private lateinit var convertScoreViewModel: ConvertScore.ConvertScoreViewModel
    private var job: Job? = null

    @Before
    fun setup() {
        convertScoreViewModel = mock(ConvertScore.ConvertScoreViewModel::class.java)
        job = mock(Job::class.java)
        Mockito.`when`(convertScoreViewModel.updateArrowValues(TestUtils.anyMatcher())).thenReturn(job)
        arrowValueCaptor = ArgumentCaptor.forClass(ArrowValue::class.java)
    }

    @Test
    fun testConvertXsToTens() {
        // Only the X will become a 10
        val expectedArrows = listOf(TestData.ARROWS[10].toArrowValue(1, 11))
        val returnedJob =
                ConvertScore.XS_TO_TENS.convertScore(TestData.ARROWS.toList().toArrowValues(), convertScoreViewModel)

        Assert.assertEquals(job, returnedJob)
        setUpArgumentCaptor(true)
        Assert.assertEquals(expectedArrows, arrowValueCaptor.allValues)
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
        val returnedJob =
                ConvertScore.TO_FIVE_ZONE.convertScore(TestData.ARROWS.toList().toArrowValues(), convertScoreViewModel)

        Assert.assertEquals(job, returnedJob)
        setUpArgumentCaptor(true)
        Assert.assertEquals(expectedArrows, arrowValueCaptor.allValues)
    }

    @Test
    fun testConvertXsToTensNoChanges() {
        val returnedJob = ConvertScore.XS_TO_TENS.convertScore(
                TestData.ARROWS.dropLast(1).toList().toArrowValues(), convertScoreViewModel
        )
        Assert.assertEquals(null, returnedJob)
        setUpArgumentCaptor(false)
    }

    @Test
    fun testConvertToFiveZoneScoreNoChanges() {
        val returnedJob = ConvertScore.TO_FIVE_ZONE.convertScore(
                listOf(
                        TestData.ARROWS[0],
                        TestData.ARROWS[1],
                        TestData.ARROWS[3],
                        TestData.ARROWS[5],
                        TestData.ARROWS[7],
                        TestData.ARROWS[9]
                ).toArrowValues(),
                convertScoreViewModel
        )
        Assert.assertEquals(null, returnedJob)
        setUpArgumentCaptor(false)
    }

    /**
     * Calls [Arrow.toArrowValue] on every item in the list, setting archerRoundId to 1 and arrowNumber incrementally
     * starting from 0
     */
    private fun List<Arrow>.toArrowValues(): List<ArrowValue> =
            this.mapIndexed { i, arrow -> arrow.toArrowValue(1, i) }

    /**
     * Verifies the number of times [ConvertScore.ConvertScoreViewModel.updateArrowValues] is called and captures all
     * parameters passed to it in [arrowValueCaptor]
     * @param isCalled true if the [ConvertScore.ConvertScoreViewModel.updateArrowValues] should be called once
     */
    private fun setUpArgumentCaptor(isCalled: Boolean) {
        val times = if (isCalled) 1 else 0
        Mockito.verify(convertScoreViewModel, Mockito.times(times))
                .updateArrowValues(TestUtils.capture(arrowValueCaptor))
    }
}