package eywa.projectcodex.common.sharedUi.numberField

import android.content.res.Resources
import androidx.annotation.StringRes

interface DisplayableError {
    fun toErrorString(resources: Resources): String
}

data class StringResError(@StringRes val id: Int) : DisplayableError {
    override fun toErrorString(resources: Resources): String = resources.getString(id)
}
