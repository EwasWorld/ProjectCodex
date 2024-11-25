package eywa.projectcodex.components.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.settings.SettingsIntent.*
import eywa.projectcodex.database.DbBackupHelpers
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey.Use2023HandicapSystem
import eywa.projectcodex.datastore.DatastoreKey.UseBetaFeatures
import eywa.projectcodex.datastore.retrieve
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val datastore: CodexDatastore,
        val database: ScoresRoomDatabase,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            datastore.get(listOf(Use2023HandicapSystem, UseBetaFeatures)).collect { values ->
                _state.update {
                    it.copy(
                            use2023System = values.retrieve(Use2023HandicapSystem),
                            useBetaFeatures = values.retrieve(UseBetaFeatures),
                    )
                }
            }
        }
    }

    fun handle(action: SettingsIntent) {
        when (action) {
            ToggleUse2023System -> viewModelScope.launch { datastore.toggle(Use2023HandicapSystem) }
            ToggleUseBetaFeatures -> viewModelScope.launch { datastore.toggle(UseBetaFeatures) }
            is HelpShowcaseAction -> helpShowcaseUseCase.handle(action.action, CodexNavRoute.SETTINGS::class)

            ClickExportDb -> _state.update {
                it.copy(
                        backupDb = true,
                        dbMessage = Message.BACKUP_IN_PROGRESS,
                        permissionRequest = PermissionRequest(
                                isReadPermission = false,
                                isGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                        ),
                )
            }

            is ExportDbHandled -> {
                _state.update {
                    it.copy(
                            backupDb = false,
                            dbMessage = when (action.outcome) {
                                DbBackupHelpers.DbResult.Success -> Message.BACKUP_SUCCESS
                                else -> Message.BACKUP_ERROR
                            },
                            permissionRequest = it.permissionRequest?.takeIf { r -> r.isReadPermission },
                    )
                }
            }

            ClickImportDb -> _state.update { it.copy(importDbConfirmDialogIsOpen = true) }
            ImportDbDialogCancel -> _state.update { it.copy(importDbConfirmDialogIsOpen = false) }
            ImportDbDialogOk -> {
                _state.update {
                    it.copy(
                            restoreDb = true,
                            dbMessage = Message.RESTORE_IN_PROGRESS,
                            importDbConfirmDialogIsOpen = false,
                            permissionRequest = PermissionRequest(
                                    isReadPermission = true,
                                    isGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                            ),
                    )
                }
            }

            is ImportDbHandled -> {
                _state.update {
                    it.copy(
                            restoreDb = false,
                            dbMessage = when (action.outcome) {
                                DbBackupHelpers.DbResult.Success -> Message.RESTORE_SUCCESS
                                DbBackupHelpers.DbResult.NoBackupFound -> Message.RESTORE_FILE_NOT_FOUND_ERROR
                                else -> Message.RESTORE_ERROR
                            },
                            permissionRequest = it.permissionRequest?.takeIf { r -> !r.isReadPermission },
                    )
                }
            }

            ClearDbDialogCancel -> _state.update { it.copy(clearDbConfirmDialogIsOpen = false) }
            ClickClearDb -> _state.update { it.copy(clearDbConfirmDialogIsOpen = true) }
            ClearDbDialogOk -> viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    _state.update { it.copy(clearingDb = true, dbMessage = Message.CLEAR_IN_PROGRESS) }
                    database.clearAllData()

                    _state.update {
                        it.copy(
                                clearingDb = false,
                                dbMessage = Message.CLEAR_SUCCESS,
                                clearDbConfirmDialogIsOpen = false,
                        )
                    }
                }
            }

            PermissionRationaleRequested ->
                _state.update { it.copy(permissionRequest = it.permissionRequest?.copy(showRationale = true)) }

            is PermissionRequestComplete -> {
                _state.update {
                    if (action.granted) {
                        it.copy(permissionRequest = it.permissionRequest?.copy(isGranted = true))
                    }
                    else {
                        it.copy(
                                restoreDb = false,
                                backupDb = false,
                                permissionRequest = null,
                                dbMessage = Message.PERMISSION_DENIED,
                        )
                    }
                }
            }

            is PermissionRationaleComplete -> {
                _state.update {
                    if (action.launch) {
                        val request = it.permissionRequest?.copy(showRationale = false, rationaleShown = true)
                        it.copy(permissionRequest = request)
                    }
                    else {
                        it.copy(
                                restoreDb = false,
                                backupDb = false,
                                permissionRequest = null,
                                dbMessage = Message.PERMISSION_DENIED,
                        )
                    }
                }
            }
        }
    }
}
