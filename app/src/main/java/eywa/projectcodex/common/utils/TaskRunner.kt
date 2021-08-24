package eywa.projectcodex.common.utils

import android.os.Handler
import android.os.Looper
import eywa.projectcodex.CustomLogger
import java.util.concurrent.Callable
import java.util.concurrent.Executors

typealias OnToken<P> = (P) -> Unit

class TaskRunner {
    companion object {
        private const val LOG_TAG = "TaskRunner"
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    abstract class ProgressTask<P, R> {
        /**
         * Setting this to true will indicate to the task that a cancel request has occurred. It is down to the
         * [runTask] implementation to stop when this is set to true
         *
         * Thread safe by only allowing it to be set to true
         */
        var isSoftCancelled = false
            set(value) {
                if (!value) {
                    throw IllegalArgumentException("isSoftCancelled can only be set to true")
                }
                field = value
            }

        abstract fun runTask(progressToken: OnToken<P>): R
    }

    fun <R> executeTask(task: Callable<R>, onComplete: OnToken<R>) {
        executor.execute {
            val result = task.call()
            handler.post {
                onComplete(result)
            }
        }
    }

    /**
     * @param onProgress called if a progress token was invoked by the task
     * @param onComplete called when the task completes
     * @param onError called if any exception is thrown while running the task
     */
    fun <P, R> executeProgressTask(
            progressTask: ProgressTask<P, R>,
            onProgress: OnToken<P>,
            onComplete: OnToken<R>,
            onError: OnToken<Exception>
    ) {
        executor.execute {
            try {
                val result = progressTask.runTask { progress -> handler.post { onProgress(progress) } }
                if (progressTask.isSoftCancelled) {
                    CustomLogger.customLogger.i(LOG_TAG, "Task was soft cancelled")
                }
                handler.post {
                    onComplete(result)
                }
            }
            catch (e: Exception) {
                handler.post { onError(e) }
            }
        }
    }
}