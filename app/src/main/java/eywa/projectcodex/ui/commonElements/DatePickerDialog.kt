package eywa.projectcodex.ui.commonElements

import android.view.View
import android.widget.DatePicker
import java.util.*

class DatePickerDialog(
        title: String, message: String?, private val maxDate: Long?, private val minDate: Long?, startDate: Calendar,
        private val okListener: OnOkListener
) : CustomDialog(title, message) {
    private val date = startDate
    private lateinit var datePicker: DatePicker

    override fun getDialogView(): View {
        datePicker = DatePicker(context)
        if (maxDate != null) datePicker.maxDate = maxDate
        if (minDate != null) datePicker.maxDate = minDate
        datePicker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH))
        // It will try to show both a spinner and calendar if this is not done. Cannot find an alternative to this
        //     deprecated function, possibly due to the API level
        datePicker.calendarViewShown = false
        return datePicker
    }

    override fun okFunction() {
        date.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        okListener.onSelect(date)
    }

    interface OnOkListener {
        fun onSelect(value: Calendar)
    }
}