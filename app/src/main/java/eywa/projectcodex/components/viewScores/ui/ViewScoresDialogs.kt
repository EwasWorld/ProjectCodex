package eywa.projectcodex.components.viewScores.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.ViewScoresIntent
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry


@Composable
fun ViewScoresEmptyListDialog(isShown: Boolean, listener: (ViewScoresIntent) -> Unit) {
    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener(ViewScoresIntent.NoRoundsDialogOkClicked) },
    ) {
        SimpleDialogContent(
                title = R.string.err_table_view__no_data,
                message = R.string.err_view_score__no_rounds,
                positiveButton = ButtonState(
                        text = stringResource(R.string.err_view_score__return_to_main_menu),
                        onClick = { listener(ViewScoresIntent.NoRoundsDialogOkClicked) },
                ),
        )
    }
}

@Composable
fun ViewScoresDeleteEntryDialog(isShown: Boolean, listener: (ViewScoresIntent) -> Unit, entry: ViewScoresEntry?) {
    val message = entry?.info?.archerRound?.dateShot
            ?.let {
                stringResource(
                        R.string.view_score__delete_score_dialog_body,
                        DateTimeFormat.SHORT_DATE_TIME.format(it)
                )
            }
            ?: stringResource(R.string.view_score__delete_score_dialog_body_generic)

    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener(ViewScoresIntent.DeleteDialogCancelClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.view_score__delete_score_dialog_title),
                message = message,
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_delete),
                        onClick = { listener(ViewScoresIntent.DeleteDialogOkClicked) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(ViewScoresIntent.DeleteDialogCancelClicked) },
                ),
        )
    }
}
