package eywa.projectcodex.ui.commonElements

import android.view.View
import android.widget.NumberPicker

class NumberPickerDialog(
        title: String, message: String?, private val maxValue: Int, private val minValue: Int,
        private val startValue: Int, private val okListener: OnOkListener
) : CustomDialog(title, message) {
    private lateinit var numberPicker: NumberPicker

    override fun getDialogView(): View {
        numberPicker = NumberPicker(context)
        numberPicker.maxValue = maxValue
        numberPicker.minValue = minValue
        numberPicker.value = startValue
        return numberPicker
    }

    override fun okFunction() {
        okListener.onSelect(numberPicker.value)
    }

    interface OnOkListener {
        fun onSelect(value: Int)
    }
}