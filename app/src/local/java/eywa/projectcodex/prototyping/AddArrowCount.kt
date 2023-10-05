package eywa.projectcodex.prototyping

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.database.rounds.FullRoundInfo

data class AddArrowCountState(
        val fullRoundInfo: FullRoundInfo? = null,
        val currentCount: Int = 0,
        val endSize: NumberFieldState<Int> = NumberFieldState(
                NumberValidatorGroup(TypeValidator.IntValidator, NumberValidator.AtLeast(1)),
        ),
)

sealed class AddArrowCountIntent {
    object ClickIncrease : AddArrowCountIntent()
    object ClickDecrease : AddArrowCountIntent()
    object ClickSubmit : AddArrowCountIntent()
    data class OnValueChanged(val value: String?) : AddArrowCountIntent()
}

@Composable
fun AddArrowCountScreen(
        state: AddArrowCountState,
        listener: (AddArrowCountIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            // TODO Add edit round box from stats screen

            Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                        text = "Shot:"
                )
                Text(
                        text = state.currentCount.toString(),
                        style = CodexTypography.LARGE.copy(color = CodexTheme.colors.onAppBackground),
                )
            }

            if (state.fullRoundInfo != null) {
                // TODO Pull from add end screen
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 15.dp)
                ) {
                    Text(
                            text = "Remaining Arrows in Round:"
                    )
                    Text(
                            text = "16 at 100yd",
                            style = CodexTypography.LARGE.copy(color = CodexTheme.colors.onAppBackground),
                    )
                    Text(
                            text = "24 at 80yd"
                    )
                }
            }

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.ArrowDownward),
                            onClick = { listener(AddArrowCountIntent.ClickIncrease) },
                    )
                    CodexNumberField(
                            contentDescription = "",
                            currentValue = state.endSize.text,
                            testTag = AddArrowCountTestTag.ADD_COUNT_INPUT,
                            placeholder = "6",
                            onValueChanged = { listener(AddArrowCountIntent.OnValueChanged(it)) },
                            modifier = Modifier.semantics {
                                customActions = listOf(
                                        CustomAccessibilityAction(
                                                label = "Increase by one",
                                                action = { listener(AddArrowCountIntent.ClickIncrease); true },
                                        ),
                                        CustomAccessibilityAction(
                                                label = "Decrease by one",
                                                action = { listener(AddArrowCountIntent.ClickDecrease); true },
                                        ),
                                )
                            }
                    )
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.Add),
                            onClick = { listener(AddArrowCountIntent.ClickDecrease) },
                    )
                }
                CodexNumberFieldErrorText(
                        errorText = state.endSize.error,
                        testTag = AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR,
                )

                CodexButton(
                        text = "Add",
                        onClick = { listener(AddArrowCountIntent.ClickSubmit) },
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
                        fullRoundInfo = RoundPreviewHelper.yorkRoundData,
                        currentCount = 24,
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
                        fullRoundInfo = null,
                        currentCount = 24,
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
                        fullRoundInfo = RoundPreviewHelper.yorkRoundData,
                        currentCount = 24,
                        endSize = NumberFieldState(
                                validators = NumberValidatorGroup(
                                        TypeValidator.IntValidator,
                                        NumberValidator.AtLeast(1),
                                ),
                                text = "-1",
                        ),
                )
        ) {}
    }
}
