package eywa.projectcodex.lintrules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Severity
import org.junit.jupiter.api.Test

class ModifierIsLastParamTest : LintDetectorTest() {
    private fun createTestFile(args: String) = kotlin(
            """
            package eywa.projectcodex.lintrules

            @Composable
            fun testCopy() {
                MyComposable($args)
            }

            @Composable
            fun MyComposable(test: String) {
            }
        """
    ).indented()

    @Test
    fun shouldDetect() {
        val testFile = createTestFile(
                """
                    |
                    |        test = "test",
                    |        modifier = "test",
                    |        test = "test",
                    |
                """.trimMargin()
        )
        val lintResult = lint()
                .skipTestModes(TestMode.REORDER_ARGUMENTS)
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult
                .expectCount(1, Severity.INFORMATIONAL)
                .expect(
                        """src/eywa/projectcodex/lintrules/test.kt:7: Information: Modifier should be the last parameter [ModifierIsLastParam]
        modifier = "test",
        ~~~~~~~~~~~~~~~~~
0 errors, 0 warnings""".trimIndent()
                )
    }

    @Test
    fun shouldAllow() {
        val testFile = createTestFile(
                """
                    |
                    |        test = "test",
                    |        test = "test",
                    |        modifier = "test",
                    |
                """.trimMargin()
        )
        val lintResult = lint()
                .skipTestModes(TestMode.REORDER_ARGUMENTS)
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult.expectClean()
    }

    override fun getDetector(): Detector = ModifierIsLastParam()

    override fun getIssues(): MutableList<Issue> = mutableListOf(ModifierIsLastParam.ISSUE)
}
