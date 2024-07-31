package eywa.projectcodex.lintrules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class IssueRegistry : IssueRegistry() {
    override val api: Int
        get() = CURRENT_API

    override val issues: List<Issue>
        get() = listOf(
                DataClassCopyCallWithPrivateConstructor.ISSUE,
                TrailingCommas.ISSUE_REQUIRED,
                TrailingCommas.ISSUE_FORBIDDEN,
                ModifierIsLastParam.ISSUE,
        )
}
