package eywa.projectcodex.components.settings

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.datastore.DatastoreKey

data class SettingsState(
        val use2023System: Boolean = DatastoreKey.Use2023HandicapSystem.defaultValue,
        val useBetaFeatures: Boolean = DatastoreKey.UseBetaFeatures.defaultValue,
        val backupDb: Boolean = false,
        val restoreDb: Boolean = false,
        val clearingDb: Boolean = false,
        val dbMessage: Message? = null,
        val importDbConfirmDialogIsOpen: Boolean = false,
        val clearDbConfirmDialogIsOpen: Boolean = false,
        val permissionRequest: PermissionRequest? = null,
) {
    val dbOperationInProgress
        get() = backupDb || restoreDb || clearingDb
}

data class PermissionRequest(
        val isReadPermission: Boolean,
        val isGranted: Boolean = false,
        val showRationale: Boolean = false,
        val rationaleShown: Boolean = false,
)

enum class Message(val text: ResOrActual<String>) {
    RESTORE_IN_PROGRESS(ResOrActual.StringResource(R.string.settings__restore_in_progress)),
    BACKUP_IN_PROGRESS(ResOrActual.StringResource(R.string.settings__backup_in_progress)),
    CLEAR_IN_PROGRESS(ResOrActual.StringResource(R.string.settings__clear_in_progress)),

    RESTORE_FILE_NOT_FOUND_ERROR(ResOrActual.StringResource(R.string.settings__restore_file_not_found_error)),
    RESTORE_FILE_ERROR(ResOrActual.StringResource(R.string.settings__restore_file_error)),
    RESTORE_ERROR(ResOrActual.StringResource(R.string.settings__restore_error)),
    BACKUP_ERROR(ResOrActual.StringResource(R.string.settings__backup_error)),

    RESTORE_SUCCESS(ResOrActual.StringResource(R.string.settings__restore_success)),
    BACKUP_SUCCESS(ResOrActual.StringResource(R.string.settings__backup_success)),
    CLEAR_SUCCESS(ResOrActual.StringResource(R.string.settings__clear_success)),

    PERMISSION_DENIED(ResOrActual.StringResource(R.string.settings__db_permission_denied)),
}
