package eywa.projectcodex.components.shootDetails.addArrowCount

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.testTag


@Composable
fun ShotCount(
        state: AddArrowCountState,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
        onSightersClicked: () -> Unit,
) {
    if (state.isEditingSighters) {
        SightersShotCount(
                sighters = state.fullShootInfo.shootRound?.sightersCount ?: 0,
                shot = state.fullShootInfo.arrowsShot,
                modifier = modifier,
                helpListener = helpListener,
        )
    }
    else {
        NormalShotCount(
                sighters = state.fullShootInfo.shootRound?.sightersCount,
                shot = state.fullShootInfo.arrowsShot,
                modifier = modifier,
                helpListener = helpListener,
                onSightersClicked = onSightersClicked
        )
    }
}

@Composable
private fun NormalShotCount(
        sighters: Int?,
        shot: Int,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
        onSightersClicked: () -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = modifier
    ) {
        sighters?.let {
            DataRow(
                    title = stringResource(R.string.add_count__sighters),
                    text = it.toString(),
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_add_count__sighters_title),
                            helpBody = stringResource(R.string.help_input_end__sighters_body),
                    ).asHelpState(helpListener),
                    textModifier = Modifier.testTag(AddArrowCountTestTag.SIGHTERS_COUNT),
                    onClick = { onSightersClicked() },
                    textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
            )
        }
        DataRow(
                title = stringResource(R.string.add_count__shot),
                text = shot.toString(),
                titleStyle = CodexTypography.LARGE.copy(color = CodexTheme.colors.onAppBackground),
                textStyle = CodexTypography.X_LARGE.copy(color = CodexTheme.colors.onAppBackground),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_add_count__shot_title),
                        helpBody = stringResource(R.string.help_add_count__shot_body),
                ).asHelpState(helpListener),
                textModifier = Modifier.testTag(AddArrowCountTestTag.SHOT_COUNT),
                modifier = Modifier
                        .border(2.dp, color = CodexTheme.colors.onAppBackground)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
        )
        sighters?.let {
            DataRow(
                    title = stringResource(R.string.add_count__total),
                    text = (it + shot).toString(),
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_add_count__total_title),
                            helpBody = stringResource(R.string.help_add_count__total_body),
                    ).asHelpState(helpListener),
                    textModifier = Modifier.testTag(AddArrowCountTestTag.TOTAL_COUNT),
                    textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
            )
        }
    }
}

@Composable
private fun SightersShotCount(
        sighters: Int,
        shot: Int,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = modifier
    ) {
        DataRow(
                title = stringResource(R.string.add_count__sighters),
                text = sighters.toString(),
                titleStyle = CodexTypography.LARGE.copy(color = CodexTheme.colors.onAppBackground),
                textStyle = CodexTypography.X_LARGE.copy(color = CodexTheme.colors.onAppBackground),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_add_count__sighters_title),
                        helpBody = stringResource(R.string.help_input_end__sighters_body),
                ).asHelpState(helpListener),
                textModifier = Modifier.testTag(AddArrowCountTestTag.SHOT_COUNT),
                modifier = Modifier
                        .border(2.dp, color = CodexTheme.colors.onAppBackground)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
        )
        DataRow(
                title = stringResource(R.string.add_count__shot),
                text = shot.toString(),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_add_count__shot_title),
                        helpBody = stringResource(R.string.help_add_count__shot_body),
                ).asHelpState(helpListener),
                textModifier = Modifier.testTag(AddArrowCountTestTag.SIGHTERS_COUNT),
                textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
        )
        DataRow(
                title = stringResource(R.string.add_count__total),
                text = (sighters + shot).toString(),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_add_count__total_title),
                        helpBody = stringResource(R.string.help_add_count__total_body),
                ).asHelpState(helpListener),
                textModifier = Modifier.testTag(AddArrowCountTestTag.TOTAL_COUNT),
                textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Normal_ShotCountPreview() {
    CodexTheme {
        NormalShotCount(
                sighters = 6,
                shot = 48,
                helpListener = {},
                onSightersClicked = {},
                modifier = Modifier.padding(10.dp)
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NormalNoSighters_ShotCountPreview() {
    CodexTheme {
        NormalShotCount(
                sighters = 0,
                shot = 48,
                helpListener = {},
                onSightersClicked = {},
                modifier = Modifier.padding(10.dp)
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Sighters_ShotCountPreview() {
    CodexTheme {
        SightersShotCount(
                sighters = 0,
                shot = 48,
                helpListener = {},
                modifier = Modifier.padding(10.dp)
        )
    }
}
