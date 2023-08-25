package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs

import eywa.projectcodex.common.utils.CodexTestTag

enum class ArrowInputsTestTag : CodexTestTag {
    ARROW_SCORE_BUTTON,

    SUBMIT_BUTTON,
    CANCEL_BUTTON,

    RESET_BUTTON,
    CLEAR_BUTTON,
    BACKSPACE_BUTTON,

    END_ARROWS_TEXT,
    END_TOTAL_TEXT,

    CONTENT_TEXT,
    ;

    override val screenName: String
        get() = "ARROW_INPUTS"

    override fun getElement(): String = name
}
