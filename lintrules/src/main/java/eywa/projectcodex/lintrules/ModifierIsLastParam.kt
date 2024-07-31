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
class ModifierIsLastParam : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        /**
         * Reports an issue if:
         * - Is a method
         * - Has a non-empty argument list
         * - "modifier" is not the last argument
         */
        override fun visitCallExpression(node: UCallExpression) {
            if (!node.isMethodCall()) return
            val nodePsi = node.sourcePsi ?: return

            val argList = nodePsi.getChildOfType<KtValueArgumentList>() ?: return
            val modifierArg = argList.arguments.find { it.getChildOfType<KtValueArgumentName>()?.text == "modifier" }
                    ?: return

            val lastArg = argList.arguments.last() ?: return
            val lastArgIsModifier = lastArg.getChildOfType<KtValueArgumentName>()?.text == "modifier"

            if (!lastArgIsModifier) {
                context.report(
                        issue = ISSUE,
                        scope = node,
                        location = context.getLocation(modifierArg),
                        message = "Modifier should be the last parameter",
                )
            }
        }
    }

    companion object {
        val ISSUE = Issue.create(
                id = "ModifierIsLastParam",
                briefDescription = "Modifier should be the last parameter",
                explanation = "Modifier should be the last parameter",
                category = Category.LINT,
                priority = 5,
                severity = Severity.INFORMATIONAL,
                implementation = Implementation(
                        ModifierIsLastParam::class.java,
                        Scope.JAVA_FILE_SCOPE,
                ),
                androidSpecific = true,
        )
    }
}
