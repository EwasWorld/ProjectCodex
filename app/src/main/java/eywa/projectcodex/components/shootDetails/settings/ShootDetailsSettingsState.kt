package eywa.projectcodex.components.shootDetails.settings

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.components.shootDetails.DEFAULT_END_SIZE
import eywa.projectcodex.components.shootDetails.ShootDetailsState

class ShootDetailsSettingsState(
        main: ShootDetailsState,
        extras: ShootDetailsSettingsExtras,
) {
    val addEndSize = main.addEndSize
    val scorePadEndSize = main.scorePadEndSize
    val addEndSizePartial = extras.addEndSizePartial
    val scorePadEndSizePartial = extras.scorePadEndSizePartial
}

data class ShootDetailsSettingsExtras(
        val addEndSizePartial: NumberFieldState<Int> =
                NumberFieldState(
                        DEFAULT_END_SIZE.toString(),
                        NumberValidatorGroup(TypeValidator.IntValidator, NumberValidator.InRange(1..12)),
                ),
        val scorePadEndSizePartial: NumberFieldState<Int> =
                NumberFieldState(
                        DEFAULT_END_SIZE.toString(),
                        NumberValidatorGroup(TypeValidator.IntValidator, NumberValidator.InRange(1..12)),
                ),
)
