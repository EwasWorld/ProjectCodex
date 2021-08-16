package eywa.projectcodex.instrumentedTests

import org.junit.runner.RunWith
import org.junit.runners.Suite


@RunWith(Suite::class)
@Suite.SuiteClasses(
        InputEndInstrumentedTest::class, NewRoundInstrumentedTest::class, ScorePadInstrumentedTest::class,
        ViewRoundsInstrumentedTest::class, UpdateDefaultRoundsInstrumentedTests::class,
        ArcherRoundStatsInstrumentedTest::class, LargeScaleInstrumentedTest::class
)
class InstrumentedTestSuite