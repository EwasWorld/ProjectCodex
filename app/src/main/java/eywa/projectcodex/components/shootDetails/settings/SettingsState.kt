package eywa.projectcodex.components.shootDetails.settings

import eywa.projectcodex.components.shootDetails.DEFAULT_END_SIZE
import eywa.projectcodex.components.shootDetails.ShootDetailsState

class SettingsState(
        main: ShootDetailsState,
        extras: SettingsExtras,
) {
    val addEndSize = main.addEndSize
    val scorePadEndSize = main.scorePadEndSize
    val addEndSizePartial = extras.addEndSizePartial
    val scorePadEndSizePartial = extras.scorePadEndSizePartial
}

data class SettingsExtras(
        val addEndSizePartial: Int? = DEFAULT_END_SIZE,
        val scorePadEndSizePartial: Int? = DEFAULT_END_SIZE,
)
