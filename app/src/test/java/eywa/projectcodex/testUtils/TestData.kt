package eywa.projectcodex.testUtils

import eywa.projectcodex.common.archeryObjects.Arrow

class TestData {
    companion object {
        const val ARROW_PLACEHOLDER = "."
        const val ARROW_DELIMINATOR = "-"

        val ARROWS = arrayOf(
                Arrow(0, false),
                Arrow(1, false),
                Arrow(2, false),
                Arrow(3, false),
                Arrow(4, false),
                Arrow(5, false),
                Arrow(6, false),
                Arrow(7, false),
                Arrow(8, false),
                Arrow(9, false),
                Arrow(10, false),
                Arrow(10, true)
        )
    }
}
