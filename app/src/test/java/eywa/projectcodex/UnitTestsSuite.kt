package eywa.projectcodex

import eywa.projectcodex.infoTableDataCalculations.InfoTableDataCalculationsSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        InfoTableDataCalculationsSuite::class, ArrowUnitTest::class, EndUnitTest::class,
        GoldsTypeUnitTest::class, DefaultRoundInfoUnitTest::class, RoundSelectionUnitTest::class,
        RemainingArrowCalculationsUnitTest::class
)
class UnitTestsSuite