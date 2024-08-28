package eywa.projectcodex.components.referenceTables.handicapTables.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.selectRoundDialog.RoundsUpdatingWrapper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialog
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesIntent
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesState

@Composable
internal fun RoundSelector(
        state: HandicapTablesState,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HandicapTablesIntent.HelpShowcaseAction(it)) }

    Surface(
            shape = RoundedCornerShape(
                    if (!state.updateDefaultRoundsState.hasTaskFinished) CodexTheme.dimens.cornerRounding
                    else if (state.selectRoundDialogState.selectedRound == null) CodexTheme.dimens.smallCornerRounding
                    else CodexTheme.dimens.cornerRounding
            ),
            border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
            color = CodexTheme.colors.appBackground,
            modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = CodexTheme.dimens.screenPadding)
    ) {
        RoundsUpdatingWrapper(
                state = state.updateDefaultRoundsState,
                warningModifier = Modifier.padding(10.dp)
        ) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                SelectRoundRows(
                        state = state.selectRoundDialogState,
                        helpListener = helpListener,
                        listener = { listener(HandicapTablesIntent.SelectRoundDialogAction(it)) },
                )
                if (state.selectRoundDialogState.selectedRound != null) {
                    SelectRoundFaceDialog(
                            state = state.selectFaceDialogState,
                            helpListener = helpListener,
                            listener = { listener(HandicapTablesIntent.SelectFaceDialogAction(it)) },
                    )
                }
            }
        }
    }
}
