package eywa.projectcodex.components.viewScores.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType


@Composable
fun ViewScoresEmptyListDialog(isShown: Boolean, listener: ListActionListener) {
    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener.noRoundsDialogDismissedListener() }
    ) {
        SimpleDialogContent(
                title = R.string.err_table_view__no_data,
                message = R.string.err_view_score__no_rounds,
                positiveButton = ButtonState(
                        text = stringResource(R.string.err_view_score__return_to_main_menu),
                        onClick = { listener.noRoundsDialogDismissedListener() }
                ),
        )
    }
}

@Composable
fun ViewScoresConvertScoreDialog(isShown: Boolean, listener: ListActionListener) {
    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener.convertScoreDialogDismissedListener() }
    ) {
        RadioButtonDialogContent(
                title = R.string.view_score__convert_score_dialog_title,
                message = R.string.view_score__convert_score_dialog_body,
                positiveButtonText = R.string.general_ok,
                onPositiveButtonPressed = { listener.convertScoreDialogOkListener(it) },
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener.convertScoreDialogDismissedListener() }
                ),
                state = rememberRadioButtonDialogState(items = ConvertScoreType.values().toList()),
        )
    }
}

@Composable
fun ViewScoresDeleteEntryDialog(isShown: Boolean, listener: ListActionListener, entry: ViewScoresEntry?) {
    val message = entry?.archerRound?.dateShot
            ?.let {
                stringResource(
                        R.string.view_score__delete_score_dialog_body,
                        DateTimeFormat.SHORT_DATE_TIME.format(it)
                )
            }
            ?: stringResource(R.string.view_score__delete_score_dialog_body_generic)

    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener.deleteDialogDismissedListener() }
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.view_score__delete_score_dialog_title),
                message = message,
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_delete),
                        onClick = { listener.deleteDialogOkListener() }
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener.deleteDialogDismissedListener() }
                ),
        )
    }
}
