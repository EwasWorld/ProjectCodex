package eywa.projectcodex.database

import android.content.Context
import android.content.Intent
import android.os.Environment
import eywa.projectcodex.common.logging.debugLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

/**
 * https://stackoverflow.com/questions/73197105/how-to-export-room-database-as-a-db-to-download-file-so-i-can-use-it-later
 * Room uses Write-Ahead Logging (WAL) by default and it doesn't appear to be that easy to checkpoint the database.
 * Instead of backing up a single file, 3 files can be backed up and restored (the wal file and the shm file)
 * see https://www.sqlite.org/wal.html
 *
 * Note my DB doesn't use WAL at the moment - better to add them to the back up code anyway in case it's turned back on
 */
object DbBackupHelpers {
    private const val DATABASE_BACKUP_NAME = "CodexArcheryAideDbBackup.zip"
    private const val SQLITE_WAL_FILE_SUFFIX = "-wal"
    private const val SQLITE_SHM_FILE_SUFFIX = "-shm"

    private fun getFolder() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    fun backupDb(context: Context, checkpoint: () -> Unit): DbResult {
        val dbFile = context.getDatabasePath(ScoresRoomDatabaseImpl.DATABASE_NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val backupFile = File(getFolder(), DATABASE_BACKUP_NAME)
        if (backupFile.exists()) backupFile.delete()

        // Ensure all changes are committed
        checkpoint()

        try {
            backupFile.createNewFile()
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
                listOf(dbFile, dbWalFile, dbShmFile).forEach { file ->
                    if (file.exists()) {
                        FileInputStream(file).use { fis ->
                            val zipEntry = ZipEntry(file.name)
                            zipOut.putNextEntry(zipEntry)
                            fis.copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }
                }
            }
        }
        catch (e: IOException) {
            return DbResult.UnknownError(e)
        }
        return DbResult.Success
    }

    fun restoreDb(
            context: Context,
            backupFile: File = File(getFolder(), DATABASE_BACKUP_NAME),
            checkpoint: () -> Unit,
    ): DbResult {
        if (!backupFile.exists()) {
            debugLog(backupFile.path)
            return DbResult.NoBackupFound
        }

        val dbFile = context.getDatabasePath(ScoresRoomDatabaseImpl.DATABASE_NAME)
        try {
            ZipFile(backupFile).use { zipFile ->
                zipFile.entries().asSequence().forEach { entry ->
                    zipFile.getInputStream(entry).use { input ->
                        val name = entry.name
                        val outputFile = when {
                            name.contains(SQLITE_WAL_FILE_SUFFIX) -> File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
                            name.contains(SQLITE_SHM_FILE_SUFFIX) -> File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)
                            else -> dbFile
                        }
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

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
        data object ErrorOpeningBackup : DbResult()
        data class UnknownError(val error: Throwable?) : DbResult()
    }
}
