package eywa.projectcodex.lintrules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import kotlin.reflect.KVisibility
import kotlin.reflect.full.primaryConstructor

class CopyUsedOnDataClassWithPrivateConstructor : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("copy")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        super.visitMethodCall(context, node, method)

        val clazz = node.javaClass.kotlin
        if (!clazz.isData) return

        val primaryConstructor = clazz.primaryConstructor ?: return
        if (primaryConstructor.visibility == KVisibility.PUBLIC) return

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

    companion object {
        val ISSUE = Issue.create(
                id = "KotlinDataClassPrivateCopyDetector",
                briefDescription = "Unsafe call to copy",
                explanation = "Using the copy constructor of a data class with a private constructor will expose the private constructor",
                category = Category.CORRECTNESS,
                priority = 9,
                severity = Severity.WARNING,
                implementation = Implementation(
                        CopyUsedOnDataClassWithPrivateConstructor::class.java,
                        Scope.JAVA_FILE_SCOPE,
                ),
        )
    }
}
