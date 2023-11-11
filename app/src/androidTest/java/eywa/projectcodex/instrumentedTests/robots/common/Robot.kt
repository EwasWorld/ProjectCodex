package eywa.projectcodex.instrumentedTests.robots.common

import eywa.projectcodex.instrumentedTests.dsl.TestActionDsl
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import kotlin.reflect.KClass

typealias PerformFn = (TestActionDsl.() -> Unit) -> Unit

interface Robot {
    fun perform(config: TestActionDsl.() -> Unit)
    fun <R : BaseRobot> createRobot(clazz: KClass<R>, block: R.() -> Unit)
}
