package eywa.projectcodex.components.sightMarks

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexMenuDialog
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.sightMarks.diagram.SightMarksDiagram
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuDialogItem
import java.util.*

@Composable
fun SightMarksScreen(
        state: SightMarksState,
        listener: (SightMarksIntent) -> Unit
) {
    var isMenuShown by remember { mutableStateOf(false) }

    // TODO_CURRENT Text display for no sight marks

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                    .background(CodexTheme.colors.appBackground)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(15.dp)
    ) {
        Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
        ) {
            CodexIconButton(
                    onClick = { listener(SightMarksIntent.CreateSightMarkClicked) },
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.sight_marks__add_button),
            )
            CodexIconButton(
                    onClick = { isMenuShown = true },
                    icon = Icons.Default.MoreHoriz,
                    contentDescription = stringResource(R.string.sight_marks__options_button),
            )
        }
        SightMarksDiagram(
                state = state,
                onClick = { listener(SightMarksIntent.SightMarkClicked(it)) }
        )
    }

    val menuItems = SightMarksMenuDialogItem.values().map { item ->
        item.asCodexMenuItem(LocalContext.current.resources) {
            listener(SightMarksIntent.MenuAction(it))
            isMenuShown = false
        }
    }
    CodexMenuDialog(isMenuShown, menuItems) { isMenuShown = false }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SightMarksScreen_Preview() {
    SightMarksScreen(
            SightMarksState(
                    sightMarks = listOf(
                            SightMark(1, 10, true, Calendar.getInstance(), 3.25f),
                            SightMark(1, 20, true, Calendar.getInstance(), 3.2f),
                            SightMark(1, 50, false, Calendar.getInstance(), 2f),
                    ),
            ),
    ) {}
}
