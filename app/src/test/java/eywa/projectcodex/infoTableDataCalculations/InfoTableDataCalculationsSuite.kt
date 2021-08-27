package eywa.projectcodex.infoTableDataCalculations

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        CalculateColumnHeadersTests::class, CalculateRowHeadersTest::class, CalculateScorePadDataTest::class
)
class InfoTableDataCalculationsSuite