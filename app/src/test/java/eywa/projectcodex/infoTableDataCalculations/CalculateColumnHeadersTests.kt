package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import eywa.projectcodex.infoTable.getColumnHeadersForTable
import eywa.projectcodex.logic.GoldsType
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class CalculateColumnHeadersTests {
    private val goldsType = GoldsType.TENS
    private val headerIds = listOf(1, 4, 7, -1, 12)
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resources = mock(Resources::class.java)
    }

    @Test
    fun testGetStandardHeaders() {
        for (testGoldsType in GoldsType.values()) {
            reset(resources)
            getColumnHeadersForTable(headerIds, resources, testGoldsType)
            val captor = ArgumentCaptor.forClass(Int::class.java)
            verify(resources, times(5)).getString(captor.capture())
            for (i in captor.allValues.indices) {
                if (headerIds[i] == -1) {
                    Assert.assertEquals(testGoldsType.colHeaderStringId, captor.allValues[i])
                }
                else {
                    Assert.assertEquals(headerIds[i], captor.allValues[i])
                }
            }
        }
    }

    /**
     * Shouldn't throw an exception
     */
    @Test
    fun testGetStandardHeadersWithResourceParameter() {
        getColumnHeadersForTable(listOf(1), resources)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNoData() {
        getColumnHeadersForTable(listOf(), resources, goldsType)
        Assert.fail("Create column headers with no data")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPlaceholderButNoGoldsType() {
        getColumnHeadersForTable(listOf(-1), resources)
        Assert.fail("Golds placeholder and no goldsType given")
    }
}