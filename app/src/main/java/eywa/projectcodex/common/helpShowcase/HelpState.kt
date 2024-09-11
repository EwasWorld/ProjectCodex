package eywa.projectcodex.common.helpShowcase

data class HelpState(
        val helpListener: (HelpShowcaseIntent) -> Unit,
        val helpShowcaseItem: HelpShowcaseItem,
) {
    fun add() = helpListener(HelpShowcaseIntent.Add(helpShowcaseItem))
}

fun HelpShowcaseItem.asHelpState(helpListener: (HelpShowcaseIntent) -> Unit) = HelpState(helpListener, this)
