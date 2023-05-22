package eywa.projectcodex.common.utils

interface CodexTestTag {
    val screenName: String

    fun getElement(): String

    fun getTestTag() = "${screenName}_${getElement()}"
}
