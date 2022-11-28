package eywa.projectcodex

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LoggerModule {
    @Singleton
    @Provides
    fun providesLogger() = CustomLogger()
}

/**
 * Ensures all message tags are prepended with a master tag for easy searching
 */
class CustomLogger {
    companion object {
        private const val MASTER_TAG = "ProjCoLog"

        var customLogger = CustomLogger()
    }

    fun d(tag: String, msg: String): Int {
        return Log.d(formatTag(tag), msg)
    }

    fun i(tag: String, msg: String): Int {
        return Log.i(formatTag(tag), msg)
    }

    fun w(tag: String, msg: String): Int {
        return Log.w(formatTag(tag), msg)
    }

    fun e(tag: String, msg: String): Int {
        return Log.w(formatTag(tag), msg)
    }

    private fun formatTag(tag: String): String {
        return "${MASTER_TAG}_$tag"
    }
}