package eywa.projectcodex.components.mainActivity

sealed class MainActivityIntent {
    object GoToNextHelpShowcaseItem : MainActivityIntent()
    object CloseHelpShowcase : MainActivityIntent()
}
