package eywa.projectcodex.lintrules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.jupiter.api.Test

class DataClassCopyCallWithPrivateConstructorTest : LintDetectorTest() {
    private val testFile = kotlin(
            """
            package eywa.projectcodex.lintrules

            fun testCopy() {
                Dog().copy()
            }
        """
    ).indented()

    @Test
    fun shouldDetect_Standard() {
        val dataClassFile = kotlin(
                """
            package eywa.projectcodex.lintrules

            data class Dog private constructor(private val breed: String) {
                constructor() : this("Poodle")
            }
        """
        ).indented()

        val lintResult = lint()
                .files(testFile, dataClassFile)
                .allowMissingSdk()
                .run()

        lintResult
                .expectErrorCount(1)
                .expect(
                        """
             src/eywa/projectcodex/lintrules/test.kt:4: Error: Using the copy method of a data class with a private primary constructor is forbidden [DataClassPrivateCopyDetector]
                 Dog().copy()
                 ~~~~~~~~~~~~
             1 errors, 0 warnings
         """.trimIndent()
                )
    }

    @Test
    fun shouldDetect_OddLineBreaks() {
        val dataClassFile = kotlin(
                """
            package eywa.projectcodex.lintrules

            data 
            class Dog private
            constructor(private val breed: String) {
                constructor() : this("Poodle")
            }
        """
        ).indented()

        val lintResult = lint()
                .files(testFile, dataClassFile)
                .allowMissingSdk()
                .run()

        lintResult
                .expectErrorCount(1)
                .expect(
                        """
             src/eywa/projectcodex/lintrules/test.kt:4: Error: Using the copy method of a data class with a private primary constructor is forbidden [DataClassPrivateCopyDetector]
                 Dog().copy()
                 ~~~~~~~~~~~~
             1 errors, 0 warnings
         """.trimIndent()
                )
    }

    @Test
    fun shouldNotDetect() {
        val dataClassFile = kotlin(
                """
            package eywa.projectcodex.lintrules

            data class Dog(private val breed: String) {
                constructor() : this("Poodle")
            }
        """
        ).indented()

        val lintResult = lint()
                .files(testFile, dataClassFile)
                .allowMissingSdk()
                .run()


        lintResult.expectClean()
    }

    override fun getDetector(): Detector = DataClassCopyCallWithPrivateConstructor()

    override fun getIssues(): MutableList<Issue> = mutableListOf(DataClassCopyCallWithPrivateConstructor.ISSUE)
}
