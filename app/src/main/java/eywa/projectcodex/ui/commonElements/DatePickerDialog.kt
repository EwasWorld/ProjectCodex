package eywa.projectcodex.ui.commonElements

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import eywa.projectcodex.R
import java.util.*

class DatePickerDialog(
        private val title: String, private val message: String?, private val maxDate: Long?, private val minDate: Long?,
        private val startDate: Calendar, private val okListener: OnSelectListener
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = startDate
        val datePicker = DatePicker(activity)
        if (maxDate != null) datePicker.maxDate = maxDate
        if (minDate != null) datePicker.maxDate = minDate
        datePicker.updateDate(
                startDate.get(Calendar.YEAR), startDate.get(Calendar.DAY_OF_MONTH), startDate.get(Calendar.MONTH)
        )
        datePicker.calendarViewShown = false

        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle(title)
        if (message != null) {
            dialogBuilder.setMessage(message)
        }
        dialogBuilder.setView(datePicker)
        dialogBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog, _ ->
            calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            okListener.onSelect(calendar)
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton(resources.getString(R.string.button_cancel)) { dialog, _ -> dialog.dismiss() }
        return dialogBuilder.create()
    }

    interface OnSelectListener {
        fun onSelect(value: Calendar)
    }
}