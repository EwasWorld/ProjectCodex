package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.infoTable.getColumnHeadersForTable
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CalculateColumnHeadersTests {
    private val goldsType = GoldsType.TENS
    private val headerIds = listOf(1, 4, 7, -1, 12)
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resources = mock()
    }

    @Test
    fun testGetStandardHeaders() {
        for (testGoldsType in GoldsType.values()) {
            val resources = mock<Resources>()
            getColumnHeadersForTable(headerIds, resources, testGoldsType)
            argumentCaptor<Int>().apply {
                verify(resources, times(5)).getString(capture())
                for (i in allValues.indices) {
                    if (headerIds[i] == -1) {
                        Assert.assertEquals(testGoldsType.colHeaderStringId, allValues[i])
                    }
                    else {
                        Assert.assertEquals(headerIds[i], allValues[i])
                    }
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

    @Test
    fun testDeleteColumn() {
        // Delete column added
        getColumnHeadersForTable(headerIds, resources, goldsType, true)
        val expectedHeaderIds = headerIds.plus(R.string.table_delete)
        argumentCaptor<Int>().apply {
            verify(resources, times(6)).getString(capture())
            for (i in allValues.indices) {
                if (expectedHeaderIds[i] == -1) {
                    Assert.assertEquals(goldsType.colHeaderStringId, allValues[i])
                }
                else {
                    Assert.assertEquals(expectedHeaderIds[i], allValues[i])
                }
            }
        }
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