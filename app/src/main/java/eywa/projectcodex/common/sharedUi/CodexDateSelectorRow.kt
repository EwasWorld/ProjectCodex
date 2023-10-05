package eywa.projectcodex.common.sharedUi

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.DateTimeFormat
import java.util.Calendar

@Composable
fun CodexDateSelectorRow(
        date: Calendar,
        updateDateListener: (UpdateCalendarInfo) -> Unit,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val timePicker by lazy {
        TimePickerDialog(
                context,
                { _, hours, minutes ->
                    updateDateListener(UpdateCalendarInfo(hours = hours, minutes = minutes))
                },
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE),
                true,
        )
    }
    val datePicker by lazy {
        DatePickerDialog(
                context,
                { _, year, month, day ->
                    updateDateListener(UpdateCalendarInfo(day = day, month = month, year = year))
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DATE),
        )
    }

    DataRow(
            title = stringResource(R.string.create_round__date),
            helpState = HelpState(
                    helpTitle = stringResource(R.string.help_create_round__date_title),
                    helpBody = stringResource(R.string.help_create_round__date_body),
                    helpListener = helpListener,
            ),
            modifier = modifier
    ) {
        Text(
                text = DateTimeFormat.TIME_24_HOUR.format(date),
                style = CodexTypography.NORMAL.asClickableStyle(),
                modifier = Modifier
                        .clickable { timePicker.show() }
                        .testTag(DateSelectorRowTestTag.TIME_BUTTON.getTestTag())
        )
        Text(
                text = DateTimeFormat.LONG_DATE.format(date),
                style = CodexTypography.NORMAL.asClickableStyle(),
                modifier = Modifier
                        .clickable { datePicker.show() }
                        .testTag(DateSelectorRowTestTag.DATE_BUTTON.getTestTag())
        )
    }
}

data class UpdateCalendarInfo(
        val day: Int? = null,
        val month: Int? = null,
        val year: Int? = null,
        val hours: Int? = null,
        val minutes: Int? = null,
) {
    init {
        require(
                day != null || month != null || year != null
                        || hours != null || minutes != null
        ) { "No new values set" }
    }

    /**
     * @return a copy of [initial] with the non-null values in this object replacing those in [initial].
     * For example if only [day] is not null, [initial]'s day will be replaced with [day] and all other fields will be
     * retained
     */
    fun updateCalendar(initial: Calendar): Calendar {
        val newValue = initial.clone() as Calendar
        day?.let { newValue.set(Calendar.DATE, day) }
        month?.let { newValue.set(Calendar.MONTH, month) }
        year?.let { newValue.set(Calendar.YEAR, year) }
        hours?.let { newValue.set(Calendar.HOUR_OF_DAY, hours) }
        minutes?.let { newValue.set(Calendar.MINUTE, minutes) }
        return newValue
    }
}

enum class DateSelectorRowTestTag : CodexTestTag {
    DATE_BUTTON,
    TIME_BUTTON,
    ;

    override val screenName: String
        get() = "DATE_SELECTOR_ROW"

    override fun getElement(): String = name
}
