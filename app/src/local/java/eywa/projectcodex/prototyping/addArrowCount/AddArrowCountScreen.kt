package eywa.projectcodex.prototyping.addArrowCount

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
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberField
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberFieldErrorText
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.addEnd.RemainingArrowsIndicator
import eywa.projectcodex.components.shootDetails.stats.NewScoreSection
import eywa.projectcodex.prototyping.addArrowCount.AddArrowCountIntent.ClickDecrease
import eywa.projectcodex.prototyping.addArrowCount.AddArrowCountIntent.ClickEditShootInfo
import eywa.projectcodex.prototyping.addArrowCount.AddArrowCountIntent.ClickIncrease
import eywa.projectcodex.prototyping.addArrowCount.AddArrowCountIntent.ClickSubmit
import eywa.projectcodex.prototyping.addArrowCount.AddArrowCountIntent.HelpShowcaseAction
import eywa.projectcodex.prototyping.addArrowCount.AddArrowCountIntent.OnValueChanged

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
            ShotCount(state)
            IncreaseCountInputs(state, listener)
        }
    }
}

@Composable
private fun ShotCount(
        state: AddArrowCountState,
) {
    val sighters = state.fullShootInfo.shootRound?.sightersCount?.takeIf { it != 0 }
    val shot = state.fullShootInfo.arrowsShot

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                    .padding(vertical = 30.dp)
    ) {
        sighters?.let {
            Text(
                    text = "Sighters: $it",
                    color = CodexTheme.colors.onAppBackground,
            )
        }
        Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                        .border(2.dp, color = CodexTheme.colors.onAppBackground)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                    text = "Shot:",
                    style = CodexTypography.LARGE,
                    color = CodexTheme.colors.onAppBackground,
            )
            Text(
                    text = shot.toString(),
                    style = CodexTypography.X_LARGE,
                    color = CodexTheme.colors.onAppBackground,
            )
        }
        sighters?.let {
            Text(
                    text = "Total: ${it + shot}",
                    color = CodexTheme.colors.onAppBackground,
            )
        }
    }
}

@Composable
private fun IncreaseCountInputs(
        state: AddArrowCountState,
        listener: (AddArrowCountIntent) -> Unit,
) {
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
                            addRound(RoundPreviewHelper.yorkRoundData, 12)
                            addArrowCounter(30)
                        },
                ).let { it.copy(endSize = it.endSize.onTextChanged("6")) }
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
                ).let { it.copy(endSize = it.endSize.onTextChanged("6")) }
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
                ).let { it.copy(endSize = it.endSize.onTextChanged("hi")) }
        ) {}
    }
}
