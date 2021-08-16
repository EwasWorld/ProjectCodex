package eywa.projectcodex

import org.junit.runner.RunWith
import org.junit.runners.Suite


@RunWith(Suite::class)
@Suite.SuiteClasses(

        ScorePadInstrumentedTest::class, ViewRoundsInstrumentedTest::class, LargeScaleInstrumentedTest::class
)
class InstrumentedTestSuite