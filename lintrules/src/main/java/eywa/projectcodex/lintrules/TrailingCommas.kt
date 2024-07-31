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
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.KtValueArgumentName
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.util.isMethodCall

/**
 */
class TrailingCommas : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        /**
         * Reports an issue if:
         * - Is a method
         * - Has a non-empty, multi-line argument list
         * - EITHER
         *      - Doesn't have a trailing comma and last argument isn't named "modifier"
         *      - Has a trailing comma and last argument is named "modifier"
         */
        override fun visitCallExpression(node: UCallExpression) {
            if (!node.isMethodCall()) return
            val nodePsi = node.sourcePsi ?: return

            val argList = nodePsi.getChildOfType<KtValueArgumentList>() ?: return
            if (!argList.text.contains("\n")) return

            if (argList.arguments.isEmpty()) return
            val hasTrailingComma = argList.trailingComma != null
            val lastArgument = argList.arguments.last() ?: return
            val lastArgIsModifier = lastArgument.getChildOfType<KtValueArgumentName>()?.text == "modifier"

            if (lastArgIsModifier && hasTrailingComma) {
                context.report(
                        issue = ISSUE_FORBIDDEN,
                        scope = node,
                        location = context.getRangeLocation(argList.trailingComma, 0, 1),
                        message = "Trailing comma is forbidden on modifier parameters",
                )
            }
            else if (!lastArgIsModifier && !hasTrailingComma) {
                context.report(
                        issue = ISSUE_REQUIRED,
                        scope = node,
                        location = context.getRangeLocation(lastArgument, lastArgument.text.length - 1, 1),
                        message = "Trailing comma is required on method calls",
                )
            }
        }
    }

    companion object {
        val ISSUE_REQUIRED = Issue.create(
                id = "TrailingCommaRequired",
                briefDescription = "Trailing commas are required",
                explanation = "Trailing commas are required except on a modifier parameter where they are forbidden",
                category = Category.LINT,
                priority = 5,
                severity = Severity.INFORMATIONAL,
                implementation = Implementation(
                        TrailingCommas::class.java,
                        Scope.JAVA_FILE_SCOPE,
                ),
                androidSpecific = true,
        )

        val ISSUE_FORBIDDEN = Issue.create(
                id = "TrailingCommaForbidden",
                briefDescription = "Trailing commas are forbidden on modifier parameters",
                explanation = "Trailing commas are required except on a modifier parameter where they are forbidden",
                category = Category.LINT,
                priority = 5,
                severity = Severity.INFORMATIONAL,
                implementation = Implementation(
                        TrailingCommas::class.java,
                        Scope.JAVA_FILE_SCOPE,
                ),
                androidSpecific = true,
        )
    }
}
