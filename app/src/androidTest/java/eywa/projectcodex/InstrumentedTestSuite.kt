package eywa.projectcodex

import org.junit.runner.RunWith
import org.junit.runners.Suite


@RunWith(Suite::class)
@Suite.SuiteClasses(
        InputEndInstrumentedTest::class, NewRoundInstrumentedTest::class,
        ScorePadInstrumentedTest::class, ViewRoundsInstrumentedTest::class, UpdateDefaultRoundsInstrumentedTests::class
)
class InstrumentedTestSuite