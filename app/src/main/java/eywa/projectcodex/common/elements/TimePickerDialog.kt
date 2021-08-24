package eywa.projectcodex.common.elements

import android.view.View
import android.widget.TimePicker

/**
 * @param startHour 0-23
 */
class TimePickerDialog(
        title: String, message: String?, private val startHour: Int, private val startMinute: Int,
        private val is24hr: Boolean, private val okListener: OnOkListener
) : CustomDialog(title, message) {
    private lateinit var timePicker: TimePicker

    override fun getDialogView(): View {
        timePicker = TimePicker(context)
        // TODO_MINSDK Must use deprecated function as API level is not high enough for replacement
        @Suppress("DEPRECATION")
        timePicker.currentHour = startHour
        @Suppress("DEPRECATION")
        timePicker.currentMinute = startMinute
        timePicker.setIs24HourView(is24hr)
        return timePicker
    }

    override fun okFunction() {
        okListener.onSelect(timePicker.currentHour, timePicker.currentMinute)
    }

    interface OnOkListener {
        /**
         * @param hours 0-23
         */
        fun onSelect(hours: Int, minutes: Int)
    }
}