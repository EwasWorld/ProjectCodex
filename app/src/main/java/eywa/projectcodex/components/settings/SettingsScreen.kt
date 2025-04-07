package eywa.projectcodex.components.settings

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SetOfDialogs
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.FileUtils
import eywa.projectcodex.database.DbBackupHelpers

@Composable
fun SettingsScreen(
        viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val showR = state.permissionRequest?.let { if (!it.showRationale) null else it.isReadPermission }

    val context = LocalContext.current

    val filePickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/zip"
    }

    // Open file picker
    var result: DbBackupHelpers.DbResult = DbBackupHelpers.DbResult.UnknownError(null)
    val launcher = FileUtils.rememberLauncherForFileActivityResult(
            onSuccess = { result = DbBackupHelpers.restoreDb(context, it) { viewModel.database.checkpoint() } },
            onFail = { result = DbBackupHelpers.DbResult.ErrorOpeningBackup },
            onComplete = { viewModel.handle(SettingsIntent.ImportDbHandled(result)) },
    )

    SettingsScreen(
            state = state,
            showPermissionRationaleFor = showR,
            listener = { viewModel.handle(it) },
            onImportClicked = { launcher.launch(filePickerIntent) },
    )

    LaunchedEffect(state.backupDb, state.restoreDb, state.permissionRequest) {
        state.permissionRequest?.let {
            if (it.isGranted) {
                if (it.isReadPermission) {
                    val r = DbBackupHelpers.restoreDb(context) { viewModel.database.checkpoint() }
                    viewModel.handle(SettingsIntent.ImportDbHandled(r))
                }
                else {
                    val r = DbBackupHelpers.backupDb(context) { viewModel.database.checkpoint() }
                    viewModel.handle(SettingsIntent.ExportDbHandled(r))
                }
            }
        }
    }

    state.permissionRequest?.let { request ->
        if (!request.isGranted) {
            RequestPermission(
                    isReadPermission = request.isReadPermission,
                    rationaleShown = request.rationaleShown,
                    requestCompleted = { viewModel.handle(SettingsIntent.PermissionRequestComplete(it)) },
                    showRationale = { viewModel.handle(SettingsIntent.PermissionRationaleRequested) },
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermission(
        isReadPermission: Boolean,
        rationaleShown: Boolean,
        requestCompleted: (granted: Boolean) -> Unit,
        showRationale: () -> Unit,
) {
    val permission =
            if (isReadPermission) Manifest.permission.READ_EXTERNAL_STORAGE
            else Manifest.permission.WRITE_EXTERNAL_STORAGE

    val permissionState = rememberPermissionState(permission = permission)

    val requestPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                requestCompleted(isGranted)
            }

    LaunchedEffect(permissionState, rationaleShown) {
        if (!permissionState.status.isGranted && permissionState.status.shouldShowRationale && !rationaleShown) {
            showRationale()
        }
        else {
            requestPermissionLauncher.launch(permission)
        }
    }
}

@Composable
fun SettingsScreen(
        state: SettingsState,
        showPermissionRationaleFor: Boolean? = null,
        listener: (SettingsIntent) -> Unit,
        onImportClicked: () -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(SettingsIntent.HelpShowcaseAction(it)) }

    SimpleDialog(
            isShown = showPermissionRationaleFor != null,
            onDismissListener = { listener(SettingsIntent.PermissionRationaleComplete(false)) },
    ) {
        val isRead = state.permissionRequest?.isReadPermission == true
        SimpleDialogContent(
                title = stringResource(R.string.settings__db_permission_required_title),
                message = stringResource(
                        if (isRead) R.string.settings__db_permission_required_read_message
                        else R.string.settings__db_permission_required_write_message,
                ),
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_ok),
                        onClick = { listener(SettingsIntent.PermissionRationaleComplete(true)) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(SettingsIntent.PermissionRationaleComplete(false)) },
                ),
        )
    }

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        SettingsDialogs(state, listener)

        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CodexTheme.dimens.screenPadding)
                        .testTag(SettingsTestTag.SCREEN)
        ) {
            DataRow(
                    title = stringResource(R.string.settings__handicap_system_title),
                    text = stringResource(
                            if (state.use2023System) R.string.settings__handicap_system_agb_2023
                            else R.string.settings__handicap_system_david_lane,
                    ),
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_settings__use_2023_system_title),
                            helpBody = stringResource(R.string.help_settings__use_2023_system_body),
                    ).asHelpState(helpListener),
                    onClick = { listener(SettingsIntent.ToggleUse2023System) },
                    accessibilityRole = Role.Switch,
                    titleStyle = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground),
                    modifier = Modifier
                            .padding(bottom = 2.dp)
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )

            Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            ) {
                DataRow(
                        title = stringResource(R.string.settings__use_beta_features),
                        text = stringResource(
                                if (state.useBetaFeatures) R.string.general_on
                                else R.string.general_off,
                        ),
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_settings__use_beta_features_title),
                                helpBody = stringResource(R.string.help_settings__use_beta_features_body),
                        ).asHelpState(helpListener),
                        onClick = { listener(SettingsIntent.ToggleUseBetaFeatures) },
                        accessibilityRole = Role.Switch,
                        titleStyle = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground),
                        modifier = Modifier.padding(bottom = 2.dp)
                )

                Text(
                        text = stringResource(R.string.settings__use_beta_features_warning),
                        textAlign = TextAlign.Center,
                )
            }

            if (state.dbFunctionsEnabled) {
                DbFunctions(state, listener, onImportClicked)
            }
        }
    }
}

@Composable
private fun SettingsDialogs(
        state: SettingsState,
        listener: (SettingsIntent) -> Unit,
) {
    SetOfDialogs(
            state.importDbConfirmDialogIsOpen to {
                SimpleDialog(
                        isShown = it,
                        onDismissListener = { listener(SettingsIntent.ImportDbDialogCancel) },
                ) {
                    SimpleDialogContent(
                            title = stringResource(R.string.settings__import_dialog_title),
                            message = stringResource(R.string.settings__import_dialog_message),
                            positiveButton = ButtonState(
                                    text = stringResource(R.string.settings__import_dialog_confirm),
                                    onClick = { listener(SettingsIntent.ImportDbDialogOk) },
                            ),
                            negativeButton = ButtonState(
                                    text = stringResource(R.string.general_cancel),
                                    onClick = { listener(SettingsIntent.ImportDbDialogCancel) },
                            ),
                    )
                }
            },
            state.clearDbConfirmDialogIsOpen to {
                SimpleDialog(
                        isShown = it,
                        onDismissListener = { listener(SettingsIntent.ClearDbDialogCancel) },
                ) {
                    SimpleDialogContent(
                            title = stringResource(R.string.settings__clear_dialog_title),
                            message = stringResource(R.string.settings__clear_dialog_message),
                            positiveButton = ButtonState(
                                    text = stringResource(R.string.settings__clear_dialog_confirm),
                                    onClick = { listener(SettingsIntent.ClearDbDialogOk) },
                            ),
                            negativeButton = ButtonState(
                                    text = stringResource(R.string.general_cancel),
                                    onClick = { listener(SettingsIntent.ClearDbDialogCancel) },
                            ),
                    )
                }
            },
    )
}

@Composable
private fun DbFunctions(
        state: SettingsState,
        listener: (SettingsIntent) -> Unit,
        onImportClicked: () -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(SettingsIntent.HelpShowcaseAction(it)) }

    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                    .padding(top = 10.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = CodexTheme.dimens.screenPadding)
                    .modifierIf(
                            state.dbOperationInProgress,
                            Modifier.background(
                                    color = CodexTheme.colors.onAppBackground.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(CodexTheme.dimens.cornerRounding),
                            ),
                    )
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .border(
                                width = 1.dp,
                                color = CodexTheme.colors.onAppBackground,
                                shape = RoundedCornerShape(CodexTheme.dimens.cornerRounding),
                        )
                        .padding(horizontal = 20.dp, vertical = 15.dp)
        ) {
            Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                            .updateHelpDialogPosition(
                                    HelpShowcaseItem(
                                            helpTitle = stringResource(R.string.help_settings__backup_restore_title),
                                            helpBody = stringResource(R.string.help_settings__backup_restore_body),
                                    ).asHelpState(helpListener),
                            )
            ) {
                CodexButton(
                        text = stringResource(R.string.settings__backup_button),
                        enabled = !state.dbOperationInProgress,
                        onClick = { listener(SettingsIntent.ClickExportDb) },
                )
                CodexButton(
                        text = stringResource(R.string.settings__restore_button),
                        enabled = !state.dbOperationInProgress,
                        onClick = { onImportClicked() },
                )
            }

            CodexButton(
                    text = stringResource(R.string.settings__clear_button),
                    enabled = !state.dbOperationInProgress,
                    onClick = { listener(SettingsIntent.ClickClearDb) },
                    modifier = Modifier.updateHelpDialogPosition(
                            HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_settings__clear_db_title),
                                    helpBody = stringResource(R.string.help_settings__clear_db_body),
                            ).asHelpState(helpListener),
                    )
            )

            state.dbMessage?.let {
                Text(
                        text = state.dbMessage.text.get(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(IntrinsicSize.Min)
                )
            }
        }
        if (state.dbOperationInProgress) {
            CircularProgressIndicator()
        }
    }
}

enum class SettingsTestTag : CodexTestTag {
    SCREEN,
    ;

    override val screenName: String
        get() = "SETTINGS"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SettingsScreen_Preview() {
    CodexTheme {
        SettingsScreen(SettingsState(dbMessage = Message.CLEAR_SUCCESS), listener = {}) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Loading_SettingsScreen_Preview() {
    CodexTheme {
        SettingsScreen(SettingsState(restoreDb = true), listener = {}) {}
    }
}
