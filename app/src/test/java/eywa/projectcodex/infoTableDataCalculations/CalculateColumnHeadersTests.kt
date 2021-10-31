package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadHeader
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.getColumnHeadersForTable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class CalculateColumnHeadersTests {
    private val goldsType = GoldsType.TENS
    private lateinit var resources: Resources

    private val headerIds = listOf(
            ScorePadHeader.END_STRING,
            ScorePadHeader.HITS,
            ScorePadHeader.SCORE,
            ScorePadHeader.GOLDS,
            ScorePadHeader.RUNNING_TOTAL
    )

    @Before
    fun setUp() {
        resources = mock(Resources::class.java)
        `when`(resources.getString(anyInt())).thenReturn("")
    }

    @Test
    fun testGetStandardHeaders() {
        for (testGoldsType in GoldsType.values()) {
            reset(resources)
            `when`(resources.getString(anyInt())).thenReturn("")
            getColumnHeadersForTable(headerIds, resources, testGoldsType)
            val captor = ArgumentCaptor.forClass(Int::class.java)
            verify(resources, times(headerIds.size)).getString(captor.capture())
            for (i in captor.allValues.indices) {
                if (headerIds[i] == ScorePadHeader.GOLDS) {
                    Assert.assertEquals(testGoldsType.shortStringId, captor.allValues[i])
                }
                else {
                    Assert.assertEquals(headerIds[i].resourceId, captor.allValues[i])
                }
            }
        }
    }

    /**
     * Shouldn't throw an exception
     */
    @Test
    fun testGetStandardHeadersWithResourceParameter() {
        getColumnHeadersForTable(listOf(ScorePadHeader.END_STRING), resources)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNoData() {
        getColumnHeadersForTable(listOf(), resources, goldsType)
        Assert.fail("Create column headers with no data")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPlaceholderButNoGoldsType() {
        getColumnHeadersForTable(listOf(ScorePadHeader.GOLDS), resources)
        Assert.fail("Golds placeholder and no goldsType given")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHeaderWithNullResId() {
        getColumnHeadersForTable(listOf(ScorePadHeader.ROW_TYPE), resources)
        Assert.fail("Row type used as header")
    }
}