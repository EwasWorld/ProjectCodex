package eywa.projectcodex

import eywa.projectcodex.databaseTests.DatabaseSuite
import eywa.projectcodex.instrumentedTests.InstrumentedTestSuite
import eywa.projectcodex.unitStyleTests.DefaultRoundInfoUnitTest
import org.junit.runner.RunWith
import org.junit.runners.Suite


@RunWith(Suite::class)
@Suite.SuiteClasses(
        DatabaseSuite::class, DefaultRoundInfoUnitTest::class, InstrumentedTestSuite::class
)
class MasterTestSuite