package eywa.projectcodex.prototyping.addArrowCount

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.model.FullShootInfo

data class AddArrowCountState(
        val fullShootInfo: FullShootInfo,
        /**
         * The amount displayed on the counter. The amount that will be added to [fullShootInfo] when 'Add' is pressed.
         */
        val endSize: NumberFieldState<Int> = NumberFieldState(
                NumberValidatorGroup(
                        TypeValidator.IntValidator,
                        NumberValidator.InRange((-3000)..3000),
                )
        ),
)
