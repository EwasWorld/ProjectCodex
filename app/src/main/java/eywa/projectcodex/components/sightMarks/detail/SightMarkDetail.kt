package eywa.projectcodex.components.sightMarks.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.sightMarks.SightMark
import java.util.*

@Composable
fun SightMarkDetail(
        sightMark: SightMark,
) {

    val distanceUnit = stringResource(
            if (sightMark.isMetric) R.string.units_meters_short else R.string.units_yards_short
    )

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DataRow(
                    title = R.string.sight_marks__distance,
                    text = "${sightMark.distance}$distanceUnit",
            )
            DataRow(
                    title = R.string.sight_marks__sight,
                    text = sightMark.sightMark.toString(),
            )
            DataRow(
                    title = R.string.sight_marks__date_set,
                    text = DateTimeFormat.SHORT_DATE.format(sightMark.dateSet),
            )
            DataRow(
                    title = R.string.sight_marks__note,
                    text = sightMark.note ?: stringResource(R.string.sight_marks__note_empty),
            )
            DataRow(
                    title = R.string.sight_marks__marked,
                    text = sightMark.marked.toString(),
            )
            DataRow(
                    title = R.string.sight_marks__archived,
                    text = sightMark.isArchive.toString(),
            )
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SightMarkDetail_Preview() {
    SightMarkDetail(
            SightMark(
                    distance = 50,
                    isMetric = false,
                    dateSet = Calendar.getInstance(),
                    sightMark = 2.3f,
                    note = "This is a note",
                    marked = false,
            )
    )
}
