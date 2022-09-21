package eywa.projectcodex

import eywa.projectcodex.common.TestData
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
import eywa.projectcodex.database.arrowValue.ArrowValue
import kotlinx.coroutines.Job
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.mock

class ConvertScoreTypeUnitTest {
    @Captor
    private lateinit var arrowValueCaptor: ArgumentCaptor<ArrowValue>
    private lateinit var convertScoreTypeViewModel: ConvertScoreType.ConvertScoreViewModel
    private var job: Job? = null

    @Before
    fun setup() {
        convertScoreTypeViewModel = mock(ConvertScoreType.ConvertScoreViewModel::class.java)
        job = mock(Job::class.java)
        Mockito.`when`(convertScoreTypeViewModel.updateArrowValues(TestUtils.anyMatcher())).thenReturn(job)
        arrowValueCaptor = ArgumentCaptor.forClass(ArrowValue::class.java)
    }

    @Test
    fun testConvertXsToTens() {
        // Only the X will become a 10
        val expectedArrows = listOf(TestData.ARROWS[10].toArrowValue(1, 11))
        val returnedJob =
                ConvertScoreType.XS_TO_TENS.convertScore(
                        TestData.ARROWS.toList().toArrowValues(),
                        convertScoreTypeViewModel
                )

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
                ConvertScoreType.TO_FIVE_ZONE.convertScore(
                        TestData.ARROWS.toList().toArrowValues(),
                        convertScoreTypeViewModel
                )

        Assert.assertEquals(job, returnedJob)
        setUpArgumentCaptor(true)
        Assert.assertEquals(expectedArrows, arrowValueCaptor.allValues)
    }

    @Test
    fun testConvertXsToTensNoChanges() {
        val returnedJob = ConvertScoreType.XS_TO_TENS.convertScore(
                TestData.ARROWS.dropLast(1).toList().toArrowValues(), convertScoreTypeViewModel
        )
        Assert.assertEquals(null, returnedJob)
        setUpArgumentCaptor(false)
    }

    @Test
    fun testConvertToFiveZoneScoreNoChanges() {
        val returnedJob = ConvertScoreType.TO_FIVE_ZONE.convertScore(
                listOf(
                        TestData.ARROWS[0],
                        TestData.ARROWS[1],
                        TestData.ARROWS[3],
                        TestData.ARROWS[5],
                        TestData.ARROWS[7],
                        TestData.ARROWS[9]
                ).toArrowValues(),
                convertScoreTypeViewModel
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
     * Verifies the number of times [ConvertScoreType.ConvertScoreViewModel.updateArrowValues] is called and captures all
     * parameters passed to it in [arrowValueCaptor]
     * @param isCalled true if the [ConvertScoreType.ConvertScoreViewModel.updateArrowValues] should be called once
     */
    private fun setUpArgumentCaptor(isCalled: Boolean) {
        val times = if (isCalled) 1 else 0
        Mockito.verify(convertScoreTypeViewModel, Mockito.times(times))
                .updateArrowValues(TestUtils.capture(arrowValueCaptor))
    }
}