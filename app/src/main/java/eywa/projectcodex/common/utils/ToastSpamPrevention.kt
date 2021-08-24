package eywa.projectcodex.common.utils

import android.content.Context
import android.widget.Toast
import eywa.projectcodex.CustomLogger

class ToastSpamPrevention private constructor() {
    companion object {
        private const val LOG_TAG = "ToastSpamPrevention"
        private val INSTANCE = ToastSpamPrevention()

        fun displayToast(context: Context, message: String) {
            INSTANCE.displayToast(context, message)
        }
    }

    /**
     * Stop a toast being displayed if identical text was shown within the past [toleranceMillis] ms
     * Default is the time of a short toast
     */
    private val toleranceMillis: Long = 2000

    /**
     * Max size of [toastTime] and [toastText]
     */
    private val itemsToStore: Int = 1

    /**
     * The index of the oldest item in [toastTime] and [toastText]
     */
    private var indexOfTail = 0

    /**
     * Buffer to store the ms time that the last [itemsToStore] items were displayed at
     */
    private val toastTime = MutableList(itemsToStore) { 0L }

    /**
     * Buffer to store the message displayed for the last [itemsToStore] items
     */
    private val toastText = MutableList(itemsToStore) { "" }

    /**
     * Stores the time and message and increments [indexOfTail] as appropriate
     */
    private fun setTimeLastPressed(message: String, time: Long) {
        toastTime[indexOfTail] = time
        toastText[indexOfTail] = message

        val nextTail = indexOfTail + 1
        indexOfTail = if (nextTail >= itemsToStore) 0 else nextTail
    }

    /**
     * Displays the [message] on the screen as a [Toast.LENGTH_SHORT] toast. Prevents the same toast from being spammed
     */
    private fun displayToast(context: Context, message: String) {
        synchronized(this) {
            val currentTime = System.currentTimeMillis()
            // If the same message has been shown recently
            if (currentTime < toastTime[indexOfTail] + toleranceMillis && toastText.contains(message)) {
                CustomLogger.customLogger.d(LOG_TAG, "Prevented message")
                return
            }
            setTimeLastPressed(message, currentTime)
        }

        // Execute
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}