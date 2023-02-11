package eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.database.rounds.Round

private val SPACING = 5.dp


@Composable
fun ArrowButtonGroup(
        round: Round?,
        modifier: Modifier = Modifier,
        onClick: (Arrow) -> Unit,
) {
    Box(modifier = modifier) {
        when {
            round == null -> TenZoneArrowButtonGroup(onClick)
            round.name.contains("WORCESTER", ignoreCase = true) -> WorcesterArrowButtonGroup(onClick)
            round.isMetric || !round.isOutdoor -> TenZoneArrowButtonGroup(onClick)
            else -> FiveZoneArrowButtonGroup(onClick)
        }
    }
}

@Composable
private fun TenZoneArrowButtonGroup(
        onClick: (Arrow) -> Unit
) {
    ColumnSection {
        RowSection {
            GeneralTargetScoreButton.M.Button(onClick)
            GeneralTargetScoreButton.ONE.Button(onClick)
            GeneralTargetScoreButton.TWO.Button(onClick)
            GeneralTargetScoreButton.THREE.Button(onClick)
            GeneralTargetScoreButton.FOUR.Button(onClick)
            GeneralTargetScoreButton.FIVE.Button(onClick)
        }
        RowSection {
            GeneralTargetScoreButton.SIX.Button(onClick)
            GeneralTargetScoreButton.SEVEN.Button(onClick)
            GeneralTargetScoreButton.EIGHT.Button(onClick)
            GeneralTargetScoreButton.NINE.Button(onClick)
            GeneralTargetScoreButton.TEN.Button(onClick)
            GeneralTargetScoreButton.X.Button(onClick)
        }
    }
}

@Composable
private fun FiveZoneArrowButtonGroup(
        onClick: (Arrow) -> Unit
) {
    RowSection {
        GeneralTargetScoreButton.M.Button(onClick)
        GeneralTargetScoreButton.ONE.Button(onClick)
        GeneralTargetScoreButton.THREE.Button(onClick)
        GeneralTargetScoreButton.FIVE.Button(onClick)
        GeneralTargetScoreButton.SEVEN.Button(onClick)
        GeneralTargetScoreButton.NINE.Button(onClick)
    }
}

@Composable
private fun WorcesterArrowButtonGroup(
        onClick: (Arrow) -> Unit
) {
    RowSection {
        WorcesterTargetScoreButton.values().forEach {
            it.Button(onClick)
        }
    }
}

@Composable
private fun ColumnSection(content: @Composable ColumnScope.() -> Unit) {
    Column(
            verticalArrangement = Arrangement.spacedBy(SPACING),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
    )
}

@Composable
private fun RowSection(content: @Composable RowScope.() -> Unit) {
    Row(
            horizontalArrangement = Arrangement.spacedBy(SPACING),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ArrowButton.Button(
        onClick: (Arrow) -> Unit,
) {
    Surface(
            onClick = { onClick(arrow) },
            color = getBackgroundColour(),
            contentColor = getContentColour(),
            modifier = Modifier
                    .size(50.dp)
    ) {
        Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
        ) {
            Text(
                    text = text.get(),
                    style = CodexTypography.NORMAL,
                    modifier = Modifier.testTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON)
            )
        }
    }
}


@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 480
)
@Composable
fun ArrowButtonGroup_Preview(
        @PreviewParameter(ArrowButtonGroupPreviewProvider::class) param: Pair<String, @Composable () -> Unit>
) {
    val (name, content) = param
    CodexTheme {
        Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                    text = name,
                    textAlign = TextAlign.Center,
                    style = CodexTypography.NORMAL,
                    modifier = Modifier.fillMaxWidth()
            )
            content()
        }
    }
}

class ArrowButtonGroupPreviewProvider : CollectionPreviewParameterProvider<Pair<String, @Composable () -> Unit>>(
        listOf(
                "10-zone" to { TenZoneArrowButtonGroup {} },
                "5-zone" to { FiveZoneArrowButtonGroup {} },
                "Worcester" to { WorcesterArrowButtonGroup {} },
        )
)
