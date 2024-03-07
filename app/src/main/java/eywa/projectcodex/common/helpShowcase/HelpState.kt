package eywa.projectcodex.common.helpShowcase

data class HelpState(
        val helpListener: (HelpShowcaseIntent) -> Unit,
        val helpShowcaseItem: HelpShowcaseItem,
) {
    @Deprecated("Use HelpShowcaseItem.asHelpState")
    constructor(
            helpListener: (HelpShowcaseIntent) -> Unit,
            helpTitle: String,
            helpBody: String,
    ) : this(helpListener, HelpShowcaseItem(helpTitle, helpBody))

    fun add() = helpListener(HelpShowcaseIntent.Add(helpShowcaseItem))
}

fun HelpShowcaseItem.asHelpState(helpListener: (HelpShowcaseIntent) -> Unit) = HelpState(helpListener, this)