package eywa.projectcodex.components.shootDetails.settings

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class ShootDetailsSettingsIntent {
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ShootDetailsSettingsIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : ShootDetailsSettingsIntent()
    data class AddEndSizeChanged(val endSize: String?) : ShootDetailsSettingsIntent()
    data class ScorePadEndSizeChanged(val endSize: String?) : ShootDetailsSettingsIntent()
}
