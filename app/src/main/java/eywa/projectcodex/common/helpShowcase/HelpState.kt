package eywa.projectcodex.common.helpShowcase

data class HelpState(
        val helpListener: (HelpShowcaseIntent) -> Unit,
        val helpShowcaseItem: HelpShowcaseItem,
) {
    constructor(
            helpListener: (HelpShowcaseIntent) -> Unit,
            helpTitle: String,
            helpBody: String,
    ) : this(helpListener, HelpShowcaseItem(helpTitle, helpBody))

    fun add() = helpListener(HelpShowcaseIntent.Add(helpShowcaseItem))
}
