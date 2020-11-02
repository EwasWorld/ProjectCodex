package eywa.projectcodex.ui.commonElements

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import eywa.projectcodex.R

class NumberPickerDialog(
        private val title: String, private val message: String?, private val maxValue: Int, private val minValue: Int,
        private val startValue: Int, private val okListener: OnSelectListener
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val numberPicker = NumberPicker(activity)
        numberPicker.maxValue = maxValue
        numberPicker.minValue = minValue
        numberPicker.value = startValue

        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle(title)
        if (message != null) {
            dialogBuilder.setMessage(message)
        }
        dialogBuilder.setView(numberPicker)
        dialogBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog, _ ->
            okListener.onSelect(numberPicker.value)
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton(resources.getString(R.string.button_cancel)) { dialog, _ -> dialog.dismiss() }
        return dialogBuilder.create()
    }

    interface OnSelectListener {
        fun onSelect(value: Int)
    }
}