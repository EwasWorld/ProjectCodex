package eywa.projectcodex.exceptions

import android.content.res.Resources

class UserException(private val messageResourceId: Int, vararg formatParams: Any?) : IllegalArgumentException() {
    private val formatParams = formatParams.asList().toTypedArray()

    fun getMessage(resources: Resources): String {
        // TODO Locale?
        return resources.getString(messageResourceId).format(*formatParams)
    }
}