package eywa.projectcodex.common.helpShowcase

data class HelpState(
        val helpListener: (HelpShowcaseIntent) -> Unit,
        val helpTitle: String,
        val helpBody: String,
) {
    fun add() = helpListener(HelpShowcaseIntent.Add(HelpShowcaseItem(helpTitle, helpBody)))
}
