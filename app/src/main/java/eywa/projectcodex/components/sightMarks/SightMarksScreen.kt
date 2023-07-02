package eywa.projectcodex.components.sightMarks

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.*
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.Add
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.Remove
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.sightMarks.SightMarksIntent.HelpShowcaseAction
import eywa.projectcodex.components.sightMarks.diagram.SightMarksDiagram
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuDialogItem
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent
import eywa.projectcodex.model.SightMark
import java.util.*

@Composable
fun SightMarksScreen(
        navController: NavController,
        viewModel: SightMarksViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: SightMarksIntent -> viewModel.handle(it) }
    SightMarksScreen(state, listener)

    LaunchedEffect(state) { handleEffects(state, navController, listener) }
}

private fun handleEffects(
        state: SightMarksState,
        navController: NavController,
        listener: (SightMarksIntent) -> Unit,
) {
    if (state !is SightMarksState.Loaded) return

    if (state.openSightMarkDetail != null) {
        CodexNavRoute.SIGHT_MARK_DETAIL.navigate(
                navController,
                mapOf(NavArgument.SIGHT_MARK_ID to state.openSightMarkDetail.toString()),
        )
        listener(SightMarksIntent.OpenSightMarkHandled)
    }

    if (state.createNewSightMark) {
        CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController)
        listener(SightMarksIntent.CreateSightMarkHandled)
    }
}

@Composable
fun SightMarksScreen(
        state: SightMarksState,
        listener: (SightMarksIntent) -> Unit,
) {
    Crossfade(
            targetState = state,
            modifier = Modifier
                    .background(CodexTheme.colors.appBackground)
                    .testTag(SightMarksTestTag.SCREEN.getTestTag())
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
                modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .padding(15.dp)
        ) {
            when (it) {
                is SightMarksState.Loaded -> SightMarksScreen(it, listener)
                is SightMarksState.Loading ->
                    Text(
                            text = stringResource(R.string.sight_marks__loading),
                            style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                            textAlign = TextAlign.Center,
                    )
            }
        }
    }
}

@Composable
private fun SightMarksScreen(
        state: SightMarksState.Loaded,
        listener: (SightMarksIntent) -> Unit
) {
    var isMenuShown by remember { mutableStateOf(false) }
    var isArchiveConfirmationShown by remember { mutableStateOf(false) }
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
    ) {
        CodexIconButton(
                onClick = { listener(SightMarksIntent.CreateSightMarkClicked) },
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.sight_marks__add_button),
                captionBelow = stringResource(R.string.sight_marks__add_button),
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_sight_marks__add_title),
                        helpBody = stringResource(R.string.help_sight_marks__add_body),
                ),
                modifier = Modifier
                        .testTag(SightMarksTestTag.ADD_BUTTON.getTestTag())
        )
        if (state.sightMarks.isNotEmpty()) {
            CodexIconButton(
                    onClick = { isMenuShown = true },
                    icon = Icons.Default.MoreHoriz,
                    contentDescription = stringResource(R.string.sight_marks__options_button),
                    captionBelow = stringResource(R.string.sight_marks__options_button),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_sight_marks__options_title),
                            helpBody = stringResource(R.string.help_sight_marks__options_body),
                    ),
                    modifier = Modifier
                            .testTag(SightMarksTestTag.OPTIONS_BUTTON.getTestTag())
            )
        }
    }
    if (state.sightMarks.isEmpty()) {
        Text(
                text = stringResource(R.string.sight_marks__diagram_placeholder),
                style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                textAlign = TextAlign.Center,
                modifier = Modifier
                        .padding(top = 10.dp)
                        .testTag(SightMarksTestTag.NO_SIGHT_MARKS_TEXT.getTestTag())
        )
        listener(HelpShowcaseAction(Remove(R.string.help_sight_marks__diagram_title)))
    }
    else {
        HelpShowcaseItem(
                helpTitle = R.string.help_sight_marks__diagram_title,
                helpBody = R.string.help_sight_marks__diagram_body,
                shape = HelpShowcaseShape.NO_SHAPE,
                priority = DEFAULT_HELP_PRIORITY - 1,
        ).let { helpListener(Add(it)) }
        SightMarksDiagram(
                state = state,
                onClick = { listener(SightMarksIntent.SightMarkClicked(it)) }
        )
    }

    val menuItems = SightMarksMenuDialogItem.values().map { item ->
        item.asCodexMenuItem(LocalContext.current.resources) {
            if (it == SightMarksMenuIntent.ArchiveAll) {
                isArchiveConfirmationShown = true
            }
            else {
                listener(SightMarksIntent.MenuAction(it))
            }
            isMenuShown = false
        }
    }
    CodexMenuDialog(isMenuShown, menuItems) { isMenuShown = false }

    SimpleDialog(
            isShown = isArchiveConfirmationShown,
            onDismissListener = { isArchiveConfirmationShown = false },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.sight_marks__archive_confirmation_title),
                message = stringResource(R.string.sight_marks__archive_confirmation_body),
                positiveButton = ButtonState(
                        text = stringResource(R.string.sight_marks__archive_confirmation_button),
                        onClick = {
                            listener(SightMarksIntent.MenuAction(SightMarksMenuIntent.ArchiveAll))
                            isArchiveConfirmationShown = false
                        },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { isArchiveConfirmationShown = false },
                ),
        )
    }
}

enum class SightMarksTestTag : CodexTestTag {
    SCREEN,
    NO_SIGHT_MARKS_TEXT,
    SIGHT_MARK_TEXT,
    DIAGRAM_TICK_LABEL,
    DIAGRAM_NOTE_ICON,
    ADD_BUTTON,
    OPTIONS_BUTTON,
    ARCHIVE_MENU_BUTTON,
    FLIP_DIAGRAM_MENU_BUTTON,
    ;

    override val screenName: String
        get() = "SIGHT_MARKS"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SightMarksScreen_Preview() {
    SightMarksScreen(
            SightMarksState.Loaded(
                    sightMarks = listOf(
                            SightMark(1, 10, true, Calendar.getInstance(), 3.25f),
                            SightMark(1, 20, true, Calendar.getInstance(), 3.2f),
                            SightMark(1, 50, false, Calendar.getInstance(), 2f),
                    ),
            ),
    ) {}
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        device = Devices.PIXEL_2,
)
@Composable
fun Empty_SightMarksScreen_Preview() {
    SightMarksScreen(SightMarksState.Loaded(sightMarks = emptyList())) {}
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        device = Devices.PIXEL_2,
)
@Composable
fun Loading_SightMarksScreen_Preview() {
    SightMarksScreen(SightMarksState.Loading()) {}
}
