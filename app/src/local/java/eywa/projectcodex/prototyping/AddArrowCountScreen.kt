package eywa.projectcodex.prototyping

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberField
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberFieldErrorText
import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.addEnd.RemainingArrowsIndicator
import eywa.projectcodex.components.shootDetails.stats.NewScoreSection
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.prototyping.AddArrowCountIntent.ClickDecrease
import eywa.projectcodex.prototyping.AddArrowCountIntent.ClickEditShootInfo
import eywa.projectcodex.prototyping.AddArrowCountIntent.ClickIncrease
import eywa.projectcodex.prototyping.AddArrowCountIntent.ClickSubmit
import eywa.projectcodex.prototyping.AddArrowCountIntent.HelpShowcaseAction
import eywa.projectcodex.prototyping.AddArrowCountIntent.OnValueChanged

data class AddArrowCountState(
        val fullShootInfo: FullShootInfo,
        /**
         * The amount displayed on the counter. The amount that will be added to [fullShootInfo] when 'Add' is pressed.
         */
        val endSize: NumberFieldState<Int> = NumberFieldState(NumberValidatorGroup(TypeValidator.IntValidator)),
)

sealed class AddArrowCountIntent {
    object ClickIncrease : AddArrowCountIntent()
    object ClickDecrease : AddArrowCountIntent()
    object ClickSubmit : AddArrowCountIntent()
    data class OnValueChanged(val value: String?) : AddArrowCountIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : AddArrowCountIntent()
    object ClickEditShootInfo : AddArrowCountIntent()
}

@Composable
fun AddArrowCountScreen(
        state: AddArrowCountState,
        listener: (AddArrowCountIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(CodexTheme.dimens.screenPadding)
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            NewScoreSection(
                    fullShootInfo = state.fullShootInfo,
                    editClickedListener = { listener(ClickEditShootInfo) },
                    helpListener = { listener(HelpShowcaseAction(it)) },

                    )

            RemainingArrowsIndicator(state.fullShootInfo)

            Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                            .padding(vertical = 30.dp)
                            .border(2.dp, color = CodexTheme.colors.onAppBackground)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                        text = "Shot:",
                        style = CodexTypography.LARGE,
                        color = CodexTheme.colors.onAppBackground,
                )
                Text(
                        text = state.fullShootInfo.arrowsShot.toString(),
                        style = CodexTypography.X_LARGE,
                        color = CodexTheme.colors.onAppBackground,
                )
            }

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.Remove),
                            onClick = { listener(ClickIncrease) },
                    )
                    CodexNumberField(
                            contentDescription = "End size",
                            currentValue = state.endSize.text,
                            testTag = AddArrowCountTestTag.ADD_COUNT_INPUT,
                            placeholder = "6",
                            onValueChanged = { listener(OnValueChanged(it)) },
                            modifier = Modifier.semantics {
                                customActions = listOf(
                                        CustomAccessibilityAction(
                                                label = "Increase by one",
                                                action = { listener(ClickIncrease); true },
                                        ),
                                        CustomAccessibilityAction(
                                                label = "Decrease by one",
                                                action = { listener(ClickDecrease); true },
                                        ),
                                )
                            }
                    )
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.Add),
                            onClick = { listener(ClickDecrease) },
                    )
                }
                CodexNumberFieldErrorText(
                        errorText = state.endSize.error,
                        testTag = AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR,
                )

                CodexButton(
                        text = "Add",
                        onClick = { listener(ClickSubmit) },
                        modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

enum class AddArrowCountTestTag : CodexTestTag {
    SCREEN,
    ADD_COUNT_INPUT,
    ADD_COUNT_INPUT_ERROR,
    ;

    override val screenName: String
        get() = "ADD_ARROW_COUNT"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun AddArrowCountScreen_Preview() {
    CodexTheme {
        AddArrowCountScreen(
                AddArrowCountState(
                        fullShootInfo = ShootPreviewHelperDsl.create {
                            round = RoundPreviewHelper.yorkRoundData
                            addArrowCounter(30)
                        },
                        endSize = NumberFieldState(
                                validators = NumberValidatorGroup(
                                        TypeValidator.IntValidator,
                                        NumberValidator.AtLeast(1),
                                ),
                                text = "6",
                        ),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NoRound_AddArrowCountScreen_Preview() {
    CodexTheme {
        AddArrowCountScreen(
                AddArrowCountState(
                        fullShootInfo = ShootPreviewHelperDsl.create {
                            addArrowCounter(24)
                        },
                        endSize = NumberFieldState(
                                validators = NumberValidatorGroup(
                                        TypeValidator.IntValidator,
                                        NumberValidator.AtLeast(1),
                                ),
                                text = "6",
                        ),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Error_AddArrowCountScreen_Preview() {
    CodexTheme {
        AddArrowCountScreen(
                AddArrowCountState(
                        fullShootInfo = ShootPreviewHelperDsl.create {
                            round = RoundPreviewHelper.yorkRoundData
                            addArrowCounter(24)
                        },
                        endSize = NumberFieldState(
                                validators = NumberValidatorGroup(
                                        TypeValidator.IntValidator,
                                        NumberValidator.AtLeast(1),
                                ),
                                text = "hi",
                        ),
                )
        ) {}
    }
}
