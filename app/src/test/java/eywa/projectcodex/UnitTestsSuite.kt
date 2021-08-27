package eywa.projectcodex

import eywa.projectcodex.infoTableDataCalculations.InfoTableDataCalculationsSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        InfoTableDataCalculationsSuite::class, ArrowUnitTest::class, EndUnitTest::class, HandicapUnitTest::class,
        GoldsTypeUnitTest::class, RoundSelectionUnitTest::class, RemainingArrowUnitTest::class,
        ViewScoresUnitTest::class
)
class UnitTestsSuite