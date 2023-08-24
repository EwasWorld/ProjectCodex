package eywa.projectcodex.components.shootDetails.settings

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class SettingsIntent {
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : SettingsIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : SettingsIntent()
    data class AddEndSizeChanged(val endSize: String?) : SettingsIntent()
    data class ScorePadEndSizeChanged(val endSize: String?) : SettingsIntent()
}
