package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import eywa.projectcodex.R
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class CalculateRowHeadersTest {
    private val totalRowHeader = "T"
    private val grandTotalRowHeader = "GT"
    private val size = listOf(24)
    private lateinit var resources: Resources
    private var rowId = 0

    @Before
    fun setUp() {
        resources = mock()
        Mockito.`when`(resources.getString(any())).thenAnswer { invocation ->
            when (invocation.getArgument<Int>(0)) {
                R.string.score_pad__grand_total_row_header -> grandTotalRowHeader
                R.string.score_pad__total_row_header -> totalRowHeader
                else -> Assert.fail("Bad string passed to resources")
            }
        }
        rowId = 0
    }

    @Test
    fun testNormalHeaders() {
        for (testSize in listOf(1, 6, 20)) {
            val generated = generateNumberedRowHeaders(listOf(testSize))
            Assert.assertEquals(testSize, generated.size)
            rowId = 0
            for (cell in generated) {
                Assert.assertEquals((rowId + 1).toString(), cell.content)
                Assert.assertEquals("row$rowId", cell.id)
                rowId++
            }
        }
    }

    @Test
    fun testHeadersWithGrandTotal() {
        val generated = generateNumberedRowHeaders(size, resources, true)
        Assert.assertEquals(size[0] + 1, generated.size)
        val grandTotalHeader = generated[generated.size - 1]
        for (cell in generated.minus(grandTotalHeader)) {
            Assert.assertEquals((rowId + 1).toString(), cell.content)
            Assert.assertEquals("row$rowId", cell.id)
            rowId++
        }
        Assert.assertEquals(grandTotalRowHeader, grandTotalHeader.content)
        Assert.assertEquals("grandTotalHeader", grandTotalHeader.id)
    }

    @Test
    fun testHeadersWithDistanceTotal() {
        for (distanceList in listOf(listOf(24), listOf(8, 8, 8), listOf(12, 8, 4), listOf(8, 8))) {
            val generated = generateNumberedRowHeaders(distanceList, resources, true)
            var expectedSize = distanceList.sum() + 1
            if (distanceList.size > 1) {
                expectedSize += distanceList.size
            }
            Assert.assertEquals(expectedSize, generated.size)
            val grandTotal = generated[generated.size - 1]
            var totalId = 0
            rowId = 0
            for (cell in generated.minus(grandTotal)) {
                if (!cell.id.contains("total")) {
                    Assert.assertEquals((rowId + 1).toString(), cell.content)
                    Assert.assertEquals("row$rowId", cell.id)
                    rowId++
                }
                else {
                    Assert.assertEquals(totalRowHeader, cell.content)
                    Assert.assertEquals("totalRow$totalId", cell.id)
                    totalId++
                }
            }
            Assert.assertEquals(grandTotalRowHeader, grandTotal.content)
            Assert.assertEquals("grandTotalHeader", grandTotal.id)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNoData() {
        generateNumberedRowHeaders(listOf(0))
        Assert.fail("Generate no header rows")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeRowCount() {
        generateNumberedRowHeaders(listOf(-1))
        Assert.fail("Negative row count for distance")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGrandTotalNoResource() {
        generateNumberedRowHeaders(listOf(24), grandTotal = true)
        Assert.fail("Resources required, grand total")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDistanceTotalNoResource() {
        generateNumberedRowHeaders(listOf(1, 4))
        Assert.fail("Resources required, multiple distances")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBothTotalNoResource() {
        generateNumberedRowHeaders(listOf(1, 4), grandTotal = true)
        Assert.fail("Resources required, grand total and multiple distances")
    }
}