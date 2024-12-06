package eywa.projectcodex.common.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import eywa.projectcodex.common.logging.CustomLogger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object FileUtils {
    @Composable
    fun rememberLauncherForFileActivityResult(
            onSuccess: (File) -> Unit,
            onFail: () -> Unit,
            onComplete: () -> Unit,
    ): ManagedActivityResultLauncher<Intent, ActivityResult> {
        val context = LocalContext.current

        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@rememberLauncherForActivityResult

                val tempCopy = File.createTempFile("codex_temp_", getFileType(uri, context.contentResolver))
                try {
                    copyInputStreamToFile(inputStream = context.contentResolver.openInputStream(uri)!!, file = tempCopy)
                    onSuccess(tempCopy)
                }
                catch (e: IOException) {
                    CustomLogger.customLogger.e("Could not open backup file for restore: ", e.message.toString())
                    onFail()
                }


                tempCopy.delete()
                onComplete()
            }
        }
    }

    /**
     * Get the extension of the file the provided uri points to
     **/
    private fun getFileType(uri: Uri, resolver: ContentResolver): String? {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri))
    }

    @Throws(IOException::class)
    private fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        FileOutputStream(file, false).use { outputStream ->
            var read: Int
            val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
            while (inputStream.read(bytes).also { read = it } != -1) {
                outputStream.write(bytes, 0, read)
            }
        }
    }
}
