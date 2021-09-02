package eywa.projectcodex.instrumentedTests

import org.junit.runner.RunWith
import org.junit.runners.Suite


@RunWith(Suite::class)
@Suite.SuiteClasses(
        ViewScoresInstrumentedTest::class, LargeScaleInstrumentedTest::class
)
class InstrumentedTestSuite