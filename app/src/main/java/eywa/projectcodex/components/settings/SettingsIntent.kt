package eywa.projectcodex.components.settings

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.database.DbBackupHelpers

sealed class SettingsIntent {
    data object ToggleUse2023System : SettingsIntent()
    data object ToggleUseBetaFeatures : SettingsIntent()
    data object ClickExportDb : SettingsIntent()
    data class ExportDbHandled(val outcome: DbBackupHelpers.DbResult) : SettingsIntent()
    data object ClickImportDb : SettingsIntent()
    data class ImportDbHandled(val outcome: DbBackupHelpers.DbResult) : SettingsIntent()
    data class PermissionRequestComplete(val granted: Boolean) : SettingsIntent()
    data object PermissionRationaleRequested : SettingsIntent()
    data class PermissionRationaleComplete(val launch: Boolean) : SettingsIntent()
    data object ClickClearDb : SettingsIntent()
    data object ImportDbDialogOk : SettingsIntent()
    data object ImportDbDialogCancel : SettingsIntent()
    data object ClearDbDialogOk : SettingsIntent()
    data object ClearDbDialogCancel : SettingsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : SettingsIntent()
}
