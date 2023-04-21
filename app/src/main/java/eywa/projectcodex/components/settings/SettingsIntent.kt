package eywa.projectcodex.components.settings

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent

sealed class SettingsIntent {
    object ToggleUse2023System : SettingsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : SettingsIntent()
}
