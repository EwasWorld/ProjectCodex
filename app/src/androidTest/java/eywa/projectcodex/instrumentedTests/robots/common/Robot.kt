package eywa.projectcodex.instrumentedTests.robots.common

import eywa.projectcodex.instrumentedTests.dsl.TestActionDslRoot
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import kotlin.reflect.KClass

typealias PerformFn = (TestActionDslRoot.() -> Unit) -> Unit

interface Robot {
    fun perform(config: TestActionDslRoot.() -> Unit)
    fun <R : BaseRobot> createRobot(clazz: KClass<R>, block: R.() -> Unit)
}
