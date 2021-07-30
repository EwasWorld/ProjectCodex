package eywa.projectcodex

import org.junit.runner.RunWith
import org.junit.runners.Suite


@RunWith(Suite::class)
@Suite.SuiteClasses(
        InputEndInstrumentedTest::class, NewRoundInstrumentedTest::class, LargeScaleInstrumentedTest::class,
        ScorePadInstrumentedTest::class, ViewRoundsInstrumentedTest::class, UpdateDefaultRoundsInstrumentedTests::class,
        ArcherRoundStatsInstrumentedTest::class
)
class InstrumentedTestSuite