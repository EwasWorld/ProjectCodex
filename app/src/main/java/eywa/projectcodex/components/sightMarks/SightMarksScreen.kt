package eywa.projectcodex.components.sightMarks

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
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
import eywa.projectcodex.components.sightMarks.SightMarksIntent.*
import eywa.projectcodex.components.sightMarks.diagram.SightMarksDiagram
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuDialogItem
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent
import eywa.projectcodex.model.SightMark
import java.util.*

private val screenPadding = 15.dp

@Composable
fun SightMarksScreen(
        navController: NavController,
        viewModel: SightMarksViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: SightMarksIntent -> viewModel.handle(it) }
    SightMarksScreen(state, listener)

    BackHandler((state as? SightMarksState.Loaded)?.isShiftAndScalePreview == true) {
        listener(EndShiftAndScale)
    }

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
        listener(OpenSightMarkHandled)
    }

    if (state.createNewSightMark) {
        CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController)
        listener(CreateSightMarkHandled)
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
                    .fillMaxSize()
                    .testTag(SightMarksTestTag.SCREEN.getTestTag())
    ) {
        when {
            it is SightMarksState.Loading -> LoadingScreen()
            it !is SightMarksState.Loaded -> throw NotImplementedError()
            it.sightMarks.isEmpty() -> EmptyScreen(listener)
            it.isShiftAndScalePreview -> ScalingScreen(it, listener)
            else -> MainScreen(it, listener)
        }
    }
}

@Composable
private fun ScrollingColumn(content: @Composable ColumnScope.() -> Unit) =
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
                content = content,
                modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .padding(screenPadding)
        )

@Composable
private fun CentredColumn(content: @Composable ColumnScope.() -> Unit) =
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
                content = content,
                modifier = Modifier
                        .fillMaxSize()
                        .padding(screenPadding)
        )

@Composable
private fun ScalingScreen(
        state: SightMarksState.Loaded,
        listener: (SightMarksIntent) -> Unit
) {
    check(state.sightMarks.isNotEmpty()) { "Cannot be empty" }
    Box {
        ScrollingColumn {
            SightMarksDiagram(
                    state = state.getShiftAndScaleState(),
                    onClick = { },
                    modifier = Modifier.padding(bottom = 230.dp, top = 30.dp)
            )
        }
        Text(
                text = stringResource(R.string.sight_marks__preview_heading),
                style = CodexTypography.LARGE.copy(color = CodexTheme.colors.onAppBackground),
                modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(screenPadding)
                        .background(CodexTheme.colors.appBackground, shape = RoundedCornerShape(30))
                        .border(1.dp, CodexTheme.colors.onAppBackground, shape = RoundedCornerShape(30))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        Surface(
                color = CodexTheme.colors.appBackground,
                border = BorderStroke(1.dp, color = CodexTheme.colors.onAppBackground),
                shape = RoundedCornerShape(20),
                modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(screenPadding)
                        .horizontalScroll(rememberScrollState())
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp)
            ) {
                Text(
                        text = stringResource(R.string.sight_marks__preview_flip),
                        style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                        modifier = Modifier
                                .padding(bottom = 5.dp)
                                .clickable { listener(ShiftAndScaleIntent.FlipClicked) }
                )
                Shifter(
                        title = stringResource(R.string.sight_marks__preview_shift),
                        helpState = null,
                        onClick = { isAdd, isBig -> listener(ShiftAndScaleIntent.Shift(isAdd, isBig)) },
                        onResetClicked = { listener(ShiftAndScaleIntent.ShiftReset) }
                )
                Shifter(
                        title = stringResource(R.string.sight_marks__preview_scale),
                        negativeButtonsEnabled = state.canScaleLower,
                        helpState = null,
                        onClick = { isAdd, isBig -> listener(ShiftAndScaleIntent.Scale(isAdd, isBig)) },
                        onResetClicked = { listener(ShiftAndScaleIntent.ScaleReset) }
                )
                CodexButton(
                        text = stringResource(R.string.general_complete),
                        onClick = { listener(ShiftAndScaleIntent.SubmitClicked) },
                        buttonStyle = CodexButtonDefaults.DefaultOutlinedButton,
                        modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun Shifter(
        title: String,
        negativeButtonsEnabled: Boolean = true,
        helpState: HelpState?,
        onClick: (isAdd: Boolean, isBig: Boolean) -> Unit,
        onResetClicked: () -> Unit,
) {
    @Composable
    fun ChangeIcon(
            icon: ImageVector,
            isAdd: Boolean,
            isBig: Boolean,
    ) {
        IconButton(
                enabled = isAdd || negativeButtonsEnabled,
                onClick = { onClick(isAdd, isBig) },
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CodexTheme.colors.onAppBackground,
            )
        }
    }

    val accessibilityActions = listOf(
            Triple(R.string.sight_marks__preview_increase_description, true, false),
            Triple(R.string.sight_marks__preview_big_increase_description, true, true),
            Triple(R.string.sight_marks__preview_decrease_description, false, false),
            Triple(R.string.sight_marks__preview_big_decrease_description, false, true),
    ).map { (stringId, isAdd, isBig) ->
        CustomAccessibilityAction(stringResource(stringId)) { onClick(isAdd, isBig); true }
    }.plus(
            CustomAccessibilityAction(
                    label = stringResource(R.string.sight_marks__preview_reset_description),
                    action = { onResetClicked(); true },
            )
    )

    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.updateHelpDialogPosition(helpState)
    ) {
        ChangeIcon(icon = Icons.Default.KeyboardDoubleArrowDown, isAdd = false, isBig = true)
        ChangeIcon(icon = Icons.Default.KeyboardArrowDown, isAdd = false, isBig = false)
        Text(
                text = title,
                style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                modifier = Modifier
                        .semantics { customActions = accessibilityActions }
                        .padding(horizontal = 15.dp)
        )
        IconButton(onClick = onResetClicked) {
            Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = CodexTheme.colors.onAppBackground,
            )
        }
        ChangeIcon(icon = Icons.Default.KeyboardArrowUp, isAdd = true, isBig = false)
        ChangeIcon(icon = Icons.Default.KeyboardDoubleArrowUp, isAdd = true, isBig = true)
    }
}

@Composable
private fun EmptyScreen(
        listener: (SightMarksIntent) -> Unit,
) = CentredColumn {
    AddNewSightMarkButton(listener)
    Text(
            text = stringResource(R.string.sight_marks__diagram_placeholder),
            style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
            textAlign = TextAlign.Center,
            modifier = Modifier
                    .padding(top = 10.dp)
                    .testTag(SightMarksTestTag.NO_SIGHT_MARKS_TEXT.getTestTag())
    )
    listener(HelpShowcaseAction(Remove(stringResource(R.string.help_sight_marks__diagram_title))))
}

@Composable
private fun LoadingScreen() =
        CentredColumn {
            Text(
                    text = stringResource(R.string.sight_marks__loading),
                    style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                    textAlign = TextAlign.Center,
            )
        }

@Composable
private fun AddNewSightMarkButton(
        listener: (SightMarksIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    CodexIconButton(
            onClick = { listener(CreateSightMarkClicked) },
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
}

@Composable
private fun MainScreen(
        state: SightMarksState.Loaded,
        listener: (SightMarksIntent) -> Unit,
) {
    require(state.sightMarks.isNotEmpty()) { "Sight marks cannot be empty" }

    var isMenuShown by remember { mutableStateOf(false) }
    var isArchiveConfirmationShown by remember { mutableStateOf(false) }
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    ScrollingColumn {
        Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
        ) {
            AddNewSightMarkButton(listener)
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
        HelpShowcaseItem(
                helpTitle = stringResource(R.string.help_sight_marks__diagram_title),
                helpBody = stringResource(R.string.help_sight_marks__diagram_body),
                shape = HelpShowcaseShape.NO_SHAPE,
                priority = DEFAULT_HELP_PRIORITY - 1,
        ).let { helpListener(Add(it)) }
        SightMarksDiagram(
                state = state,
                onClick = { listener(SightMarkClicked(it)) }
        )

        val menuItems = SightMarksMenuDialogItem.values().map { item ->
            item.asCodexMenuItem(LocalContext.current.resources) {
                if (it == SightMarksMenuIntent.ArchiveAll) {
                    isArchiveConfirmationShown = true
                }
                else {
                    listener(MenuAction(it))
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
                                listener(MenuAction(SightMarksMenuIntent.ArchiveAll))
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
    SHIFT_AND_SCALE_MENU_BUTTON,
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
)
@Composable
fun Scale_SightMarksScreen_Preview() {
    SightMarksScreen(
            SightMarksState.Loaded(
                    sightMarks = listOf(
                            SightMark(1, 10, true, Calendar.getInstance(), 3.25f),
                            SightMark(1, 20, true, Calendar.getInstance(), 3.2f),
                            SightMark(1, 50, false, Calendar.getInstance(), 2f),
                    ),
                    scaleAmount = 1.2f,
                    shiftAmount = 0.5f,
            ),
    ) {}
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
