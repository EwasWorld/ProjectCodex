package eywa.projectcodex.lintrules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Severity
import org.junit.jupiter.api.Test

class TrailingCommasTest : LintDetectorTest() {
    private fun createTestFile(args: String) = kotlin(
            "src/eywa/projectcodex/lintrules/test.kt",
            """
            package eywa.projectcodex.lintrules

            @Composable
            fun testCopy() {
                MyComposable($args)
            }

            @Composable
            fun MyComposable(test: String) {
            }

            annotation class Composable
        """
    ).indented()

    @Test
    fun shouldDetect_Required() {
        val testFile = createTestFile(
                """
                    |
                    |        test = "test"
                    |
                """.trimMargin()
        )
        val lintResult = lint()
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult
                .expectCount(1, Severity.INFORMATIONAL)
                .expect(
                        """src/eywa/projectcodex/lintrules/test.kt:6: Information: Trailing comma is required on method calls [TrailingCommaRequired]
        test = "test"
                    ~
0 errors, 0 warnings""".trimIndent()
                )
    }

    @Test
    fun shouldAllow_Required() {
        val testFile = createTestFile(
                """
                    |
                    |        test = "test",
                    |
                """.trimMargin()
        )
        val lintResult = lint()
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult.expectClean()
    }

    @Test
    fun shouldDetect_Forbidden() {
        val testFile = createTestFile(
                """
                    |
                    |        modifier = "test",
                    |
                """.trimMargin()
        )
        val lintResult = lint()
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult
                .expectCount(1, Severity.INFORMATIONAL)
                .expect(
                        """src/eywa/projectcodex/lintrules/test.kt:6: Information: Trailing comma is forbidden on modifier parameters [TrailingCommaForbidden]
        modifier = "test",
                         ~
0 errors, 0 warnings""".trimIndent()
                )
    }

    @Test
    fun shouldAllow_Forbidden() {
        val testFile = createTestFile(
                """
                    |
                    |        modifier = "test"
                    |
                """.trimMargin()
        )
        val lintResult = lint()
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult.expectClean()
    }

    @Test
    fun shouldAllowSingleLine_Forbidden() {
        val testFile = createTestFile(
                """modifier = "test"""",
        )
        val lintResult = lint()
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult.expectClean()
    }

    @Test
    fun shouldAllowSingleLine_Required() {
        val testFile = createTestFile(
                """test = "test"""",
        )
        val lintResult = lint()
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult.expectClean()
    }

    @Test
    fun shouldAllowLambda() {
        val testFile = kotlin(
                "src/eywa/projectcodex/lintrules/test.kt",
                """
            package eywa.projectcodex.lintrules

            @Composable
            fun testCopy() {
                MyComposable("test") {
                    
                }
            }

            @Composable
            fun MyComposable(test: String) {
            }

            annotation class Composable
        """
        ).indented()
        val lintResult = lint()
                .skipTestModes(TestMode.REORDER_ARGUMENTS)
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult.expectClean()
    }

    @Test
    fun shouldAllowStringNewLine_Required() {
        val testFile = createTestFile(
                "test = \"\\n\"",
        )
        val lintResult = lint()
                .files(testFile)
                .allowMissingSdk()
                .run()

        lintResult.expectClean()
    }

    override fun getDetector(): Detector = TrailingCommas()

    override fun getIssues(): MutableList<Issue> = mutableListOf(
            TrailingCommas.ISSUE_REQUIRED,
            TrailingCommas.ISSUE_FORBIDDEN,
    )
}
