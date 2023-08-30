package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.model.Arrow

private val SPACING = 5.dp


@Composable
fun ArrowButtonGroup(
        round: Round?,
        modifier: Modifier = Modifier,
        roundFace: RoundFace? = null,
        horizontalPadding: Dp = 0.dp,
        onClick: (Arrow) -> Unit,
) {
    Row(
            modifier = modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding)
    ) {
        when {
            round == null -> TenZoneArrowButtonGroup(roundFace, onClick)
            round.name.contains("WORCESTER", ignoreCase = true) -> WorcesterArrowButtonGroup(roundFace, onClick)
            round.isImperial && round.isOutdoor -> FiveZoneArrowButtonGroup(onClick)
            else -> TenZoneArrowButtonGroup(roundFace, onClick)
        }
    }
}

@Composable
private fun TenZoneArrowButtonGroup(
        roundFace: RoundFace? = null,
        onClick: (Arrow) -> Unit,
) =
        when (roundFace) {
            null, RoundFace.FULL -> FullFaceTenZoneArrowButtonGroup(onClick)
            RoundFace.FITA_SIX -> FitaSixFaceTenZoneArrowButtonGroup(onClick)
            RoundFace.WORCESTER_FIVE -> WorcesterArrowButtonGroup(roundFace, onClick)
            else -> HalfFaceTenZoneArrowButtonGroup(roundFace, onClick)
        }

@Composable
private fun FullFaceTenZoneArrowButtonGroup(
        onClick: (Arrow) -> Unit,
) {
    ColumnSection {
        RowSection {
            GeneralTargetScoreButton.M.Button(null, onClick)
            GeneralTargetScoreButton.ONE.Button(null, onClick)
            GeneralTargetScoreButton.TWO.Button(null, onClick)
            GeneralTargetScoreButton.THREE.Button(null, onClick)
            GeneralTargetScoreButton.FOUR.Button(null, onClick)
            GeneralTargetScoreButton.FIVE.Button(null, onClick)
        }
        RowSection {
            GeneralTargetScoreButton.SIX.Button(null, onClick)
            GeneralTargetScoreButton.SEVEN.Button(null, onClick)
            GeneralTargetScoreButton.EIGHT.Button(null, onClick)
            GeneralTargetScoreButton.NINE.Button(null, onClick)
            GeneralTargetScoreButton.TEN.Button(null, onClick)
            GeneralTargetScoreButton.X.Button(null, onClick)
        }
    }
}

@Composable
private fun HalfFaceTenZoneArrowButtonGroup(
        roundFace: RoundFace? = null,
        onClick: (Arrow) -> Unit,
) {
    RowSection {
        GeneralTargetScoreButton.M.Button(roundFace, onClick)

        ColumnSection {
            RowSection {
                GeneralTargetScoreButton.SIX.Button(roundFace, onClick)
                GeneralTargetScoreButton.SEVEN.Button(roundFace, onClick)
                GeneralTargetScoreButton.EIGHT.Button(roundFace, onClick)
            }
            RowSection {
                GeneralTargetScoreButton.NINE.Button(roundFace, onClick)
                GeneralTargetScoreButton.TEN.Button(roundFace, onClick)
                GeneralTargetScoreButton.X.Button(roundFace, onClick)
            }
        }
    }
}

@Composable
private fun FitaSixFaceTenZoneArrowButtonGroup(
        onClick: (Arrow) -> Unit,
) {
    ColumnSection {
        RowSection {
            GeneralTargetScoreButton.M.Button(RoundFace.FITA_SIX, onClick)
            GeneralTargetScoreButton.FIVE.Button(RoundFace.FITA_SIX, onClick)
            GeneralTargetScoreButton.SIX.Button(RoundFace.FITA_SIX, onClick)
            GeneralTargetScoreButton.SEVEN.Button(RoundFace.FITA_SIX, onClick)
        }
        RowSection {
            GeneralTargetScoreButton.EIGHT.Button(RoundFace.FITA_SIX, onClick)
            GeneralTargetScoreButton.NINE.Button(RoundFace.FITA_SIX, onClick)
            GeneralTargetScoreButton.TEN.Button(RoundFace.FITA_SIX, onClick)
            GeneralTargetScoreButton.X.Button(RoundFace.FITA_SIX, onClick)
        }
    }
}

@Composable
private fun FiveZoneArrowButtonGroup(
        onClick: (Arrow) -> Unit,
) {
    RowSection {
        GeneralTargetScoreButton.M.Button(null, onClick)
        GeneralTargetScoreButton.ONE.Button(null, onClick)
        GeneralTargetScoreButton.THREE.Button(null, onClick)
        GeneralTargetScoreButton.FIVE.Button(null, onClick)
        GeneralTargetScoreButton.SEVEN.Button(null, onClick)
        GeneralTargetScoreButton.NINE.Button(null, onClick)
    }
}

@Composable
private fun WorcesterArrowButtonGroup(
        roundFace: RoundFace? = null,
        onClick: (Arrow) -> Unit,
) {
    RowSection {
        WorcesterTargetScoreButton.values().forEach {
            it.Button(roundFace, onClick)
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

@Composable
private fun ArrowButton.Button(
        roundFace: RoundFace?,
        onClick: (Arrow) -> Unit,
) {
    if (roundFace != null && !shouldShow(roundFace)) return

    val contentDescription = contentDescription()
    Surface(
            color = getBackgroundColour(),
            contentColor = getContentColour(),
            modifier = Modifier.size(50.dp)
    ) {
        Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClick(arrow) }
                        .semantics {
                            this.contentDescription = contentDescription
                        }
                        .testTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON.getTestTag())
        ) {
            Text(
                    text = text.get(),
                    style = CodexTypography.NORMAL,
                    modifier = Modifier
                            .clearAndSetSemantics { }
            )
        }
    }
}


@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 480,
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
                "10-zone: triple" to { TenZoneArrowButtonGroup(RoundFace.TRIPLE) {} },
                "10-zone: six" to { TenZoneArrowButtonGroup(RoundFace.FITA_SIX) {} },
                "5-zone" to { FiveZoneArrowButtonGroup {} },
                "Worcester" to { WorcesterArrowButtonGroup {} },
                "Worcester 5" to { WorcesterArrowButtonGroup(RoundFace.WORCESTER_FIVE) {} },
        )
)
