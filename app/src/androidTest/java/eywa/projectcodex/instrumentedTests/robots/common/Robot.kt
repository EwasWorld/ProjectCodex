package eywa.projectcodex.instrumentedTests.robots.common

import eywa.projectcodex.instrumentedTests.dsl.TestActionDslV2
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import kotlin.reflect.KClass

typealias PerformFnV2 = (TestActionDslV2.() -> Unit) -> Unit

interface Robot {
    fun performV2(config: TestActionDslV2.() -> Unit)
    fun <R : BaseRobot> createRobot(clazz: KClass<R>, block: R.() -> Unit)
}
