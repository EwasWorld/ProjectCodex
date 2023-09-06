package eywa.projectcodex.lintrules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.fabiocarballo.rules.AndroidLogDetector

class CodexIssueRegistry : IssueRegistry() {
    override val api: Int
        get() = CURRENT_API

    override val issues: List<Issue>
        get() = listOf(AndroidLogDetector.ISSUE)
}
