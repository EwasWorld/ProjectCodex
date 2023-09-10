package eywa.projectcodex.lintrules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClassType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.util.isMethodCall

/**
 * Google sample: https://github.com/googlesamples/android-custom-lint-rules
 * Create rules tutorial: https://proandroiddev.com/implementing-your-first-android-lint-rule-6e572383b292
 * Video: https://www.youtube.com/watch?v=jCmJWOkjbM0
 *
 * Rules docs: https://googlesamples.github.io/android-custom-lint-rules/index.html
 * Api docs: https://googlesamples.github.io/android-custom-lint-rules/api-guide.html
 *
 * Built-in lint checks source https://android.googlesource.com/platform/tools/base/+/refs/heads/studio-master-dev/lint/libs/lint-checks/src/main/java/com/android/tools/lint/checks
 */
class DataClassCopyCallWithPrivateConstructor : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        /**
         * Reports an issue if:
         * - Is a method
         * - Name is 'copy'
         * - Class is a data class (file contains the string 'data class <className>')
         * - Primary constructor is private ('private' keyword detected between class name primary constructor)
         */
        override fun visitCallExpression(node: UCallExpression) {
            if (!node.isMethodCall()) return
            if (node.methodName != "copy") return

            val clazzType = (node.receiverType as? PsiClassType) ?: return
            val clazz = clazzType.resolve() ?: return

            // Find the text between the class name and the start of the primary constructor
            val clazzFileText = clazz.containingFile?.text ?: return
            val endOfNameIndex = Regex("data\\s+class\\s+${clazzType.name}").find(clazzFileText)?.range?.last ?: return
            val startOfParamsIndex = clazzFileText.indexOf("(", endOfNameIndex).takeIf { it != -1 }
                    ?: return

            val primaryConstructorModifiers = clazzFileText.substring(endOfNameIndex, startOfParamsIndex)
            if (
                !primaryConstructorModifiers.contains("private")
                && !primaryConstructorModifiers.contains("protected")
                && !primaryConstructorModifiers.contains("internal")
            ) return

            context.report(
                    issue = ISSUE,
                    scope = node,
                    location = context.getCallLocation(
                            call = node,
                            includeReceiver = true,
                            includeArguments = true,
                    ),
                    message = "Using the copy method of a data class with a private primary constructor is forbidden",
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
                id = "DataClassPrivateCopyDetector",
                briefDescription = "Unsafe call to copy",
                explanation = "Using the copy constructor of a data class with a private constructor will expose the private constructor",
                category = Category.CORRECTNESS,
                priority = 9,
                severity = Severity.ERROR,
                implementation = Implementation(
                        DataClassCopyCallWithPrivateConstructor::class.java,
                        Scope.JAVA_FILE_SCOPE,
                ),
                androidSpecific = true,
        )
    }
}
