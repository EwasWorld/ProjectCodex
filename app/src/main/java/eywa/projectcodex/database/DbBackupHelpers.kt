package eywa.projectcodex.database

import android.content.Context
import android.content.Intent
import android.os.Environment
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

/**
 * https://stackoverflow.com/questions/73197105/how-to-export-room-database-as-a-db-to-download-file-so-i-can-use-it-later
 * Room uses Write-Ahead Logging (WAL) by default and it doesn't appear to be that easy to checkpoint the database.
 * Instead of backing up a single file, 3 files can be backed up and restored (the wal file and the shm file)
 * see https://www.sqlite.org/wal.html
 *
 * Note my DB doesn't use WAL at the moment
 */
object DbBackupHelpers {
    private const val DATABASE_BACKUP_NAME = "database-bkp"
    private const val SQLITE_WAL_FILE_SUFFIX = "-wal"
    private const val SQLITE_SHM_FILE_SUFFIX = "-shm"

    private fun getFolder() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "CodexArcheryAideDbBackup",
    )

    fun backupDb(context: Context, checkpoint: () -> Unit): DbResult {
        val dbFile = context.getDatabasePath(ScoresRoomDatabaseImpl.DATABASE_NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val backupFolder = getFolder()
        if (backupFolder.exists()) backupFolder.delete()
        backupFolder.mkdir()

        val bkpFile = File(backupFolder, DATABASE_BACKUP_NAME)
        val bkpWalFile = File(backupFolder, DATABASE_BACKUP_NAME + SQLITE_WAL_FILE_SUFFIX)
        val bkpShmFile = File(backupFolder, DATABASE_BACKUP_NAME + SQLITE_SHM_FILE_SUFFIX)

        if (bkpFile.exists()) bkpFile.delete()
        if (bkpWalFile.exists()) bkpWalFile.delete()
        if (bkpShmFile.exists()) bkpShmFile.delete()

        checkpoint()

        try {
            dbFile.copyTo(bkpFile, true)
            if (dbWalFile.exists()) dbWalFile.copyTo(bkpWalFile, true)
            if (dbShmFile.exists()) dbShmFile.copyTo(bkpShmFile, true)
        }
        catch (e: IOException) {
            return DbResult.UnknownError(e)
        }
        return DbResult.Success
    }

    fun restoreDb(context: Context, checkpoint: () -> Unit): DbResult {
        val backupFolder = getFolder()
        val bkpFile = File(backupFolder, DATABASE_BACKUP_NAME)

        if (!bkpFile.exists()) return DbResult.NoBackupFound

        val dbFile = context.getDatabasePath(ScoresRoomDatabaseImpl.DATABASE_NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val bkpWalFile = File(backupFolder, DATABASE_BACKUP_NAME + SQLITE_WAL_FILE_SUFFIX)
        val bkpShmFile = File(backupFolder, DATABASE_BACKUP_NAME + SQLITE_SHM_FILE_SUFFIX)

        try {
            bkpFile.copyTo(dbFile, true)
            if (bkpWalFile.exists()) bkpWalFile.copyTo(dbWalFile, true)
            if (bkpShmFile.exists()) bkpShmFile.copyTo(dbShmFile, true)

            checkpoint()
        }
        catch (e: IOException) {
            return DbResult.UnknownError(e)
        }

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
        exitProcess(0)
    }

    sealed class DbResult {
        data object Success : DbResult()
        data object NoBackupFound : DbResult()
        data class UnknownError(val error: Throwable) : DbResult()
    }
}
