package eywa.projectcodex.common.elements

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import eywa.projectcodex.R

abstract class CustomDialog(private val title: String, private val message: String?) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle(title)
        if (message != null) {
            dialogBuilder.setMessage(message)
        }
        dialogBuilder.setView(getDialogView())
        dialogBuilder.setPositiveButton(resources.getString(R.string.general_ok)) { dialog, _ ->
            okFunction()
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton(resources.getString(R.string.general_cancel)) { dialog, _ -> dialog.dismiss() }
        return dialogBuilder.create()
    }

    /**
     * The view which the dialog will display
     * @see onCreateDialog
     */
    abstract fun getDialogView(): View

    /**
     * Called when OK is pressed on the dialog (before dismissing)
     */
    abstract fun okFunction()
}