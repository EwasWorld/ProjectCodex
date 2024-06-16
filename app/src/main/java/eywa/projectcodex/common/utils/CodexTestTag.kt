package eywa.projectcodex.common.utils

interface CodexTestTag {
    val screenName: String

    fun getElement(): String

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(message = "Use Modifier.testTag(CodexTestTag)")
    fun getTestTag() = "${screenName}_${getElement()}"
}
