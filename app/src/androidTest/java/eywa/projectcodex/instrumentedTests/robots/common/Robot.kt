package eywa.projectcodex.instrumentedTests.robots.common

import eywa.projectcodex.instrumentedTests.dsl.TestActionDsl
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import kotlin.reflect.KClass

interface Robot {
    fun perform(config: TestActionDsl.() -> Unit)
    fun <R : BaseRobot> createRobot(clazz: KClass<R>, block: R.() -> Unit)
}
