package eywa.projectcodex.instrumentedTests.robots.common

import eywa.projectcodex.instrumentedTests.dsl.TestActionDsl
import eywa.projectcodex.instrumentedTests.dsl.TestActionDslV2
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import kotlin.reflect.KClass

typealias PerformFn = (TestActionDsl.() -> Unit) -> Unit

interface Robot {
    @Deprecated(
            "There's a shiny new version",
            ReplaceWith("performV2", " eywa.projectcodex.instrumentedTests.dsl.TestActionDslV2"),
    )
    fun perform(config: TestActionDsl.() -> Unit)
    fun performV2(config: TestActionDslV2.() -> Unit)
    fun <R : BaseRobot> createRobot(clazz: KClass<R>, block: R.() -> Unit)
}
