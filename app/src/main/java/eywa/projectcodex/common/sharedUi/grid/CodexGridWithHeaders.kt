package eywa.projectcodex.common.sharedUi.grid

import android.content.res.Resources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual

@Composable
fun <RowData : CodexGridRowMetadata, ExtraData> CodexGridWithHeaders(
        data: List<RowData>,
        columnMetadata: List<CodexGridColumnMetadata<RowData, ExtraData>>,
        extraData: ExtraData,
        modifier: Modifier = Modifier,
        columns: List<CodexGridColumn> = List(columnMetadata.size) { CodexGridColumn.WrapContent },
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val resource = LocalContext.current.resources

    CodexGridWithHeaders(
            columnMetadata = columnMetadata,
            columns = columns,
            helpListener = helpListener,
            modifier = modifier
    ) {
        data.forEach { row ->
            columnMetadata.forEach { column ->
                val cellModifier = column.testTag?.let { Modifier.testTag(it) } ?: Modifier
                val value = column.mapping(row)?.get(resource)

                if (value != null) {
                    item(
                            horizontalSpan = column.cellHorizontalSpan(row),
                            verticalSpan = column.cellVerticalSpan(row),
                            backgroundColor = {
                                if (row.useAccentColor() || column.useAccentColor()) {
                                    CodexTheme.colors.listAccentRowItemOnAppBackground
                                }
                                else {
                                    CodexTheme.colors.listItemOnAppBackground
                                }
                            },
                            padding = column.padding(),
                    ) {
                        val weight =
                                if (row.useBoldText() || column.useBoldText()) FontWeight.Bold
                                else FontWeight.Normal

                        Text(
                                text = value,
                                fontWeight = weight,
                                color = column.textColour(row) ?: CodexTheme.colors.onListItemAppOnBackground,
                                textAlign = TextAlign.Center,
                                modifier = cellModifier
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .semantics {
                                            column
                                                    .cellContentDescription(row, extraData)
                                                    ?.get(resource)
                                                    ?.let {
                                                        contentDescription = it
                                                    }
                                        }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun <RowData : Any, ExtraData> CodexGridWithHeaders(
        columnMetadata: List<CodexGridColumnMetadata<RowData, ExtraData>>,
        modifier: Modifier = Modifier,
        columns: List<CodexGridColumn> = List(columnMetadata.size) { CodexGridColumn.WrapContent },
        helpListener: (HelpShowcaseIntent) -> Unit = { },
        config: CodexGridConfig.() -> Unit,
) {
    val resource = LocalContext.current.resources

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        CodexGrid(
                columns = columns,
                alignment = Alignment.Center,
                verticalSpacing = 4.dp,
                horizontalSpacing = 4.dp,
                modifier = modifier
        ) {
            columnMetadata.forEach { column ->
                column.primaryTitle?.let { primaryTitle ->
                    val helpState =
                            if (column.primaryTitleHorizontalSpan > 1) null
                            else column.getHelpState(resource)?.asHelpState(helpListener)
                    item(
                            backgroundColor = { CodexTheme.colors.listAccentRowItemOnAppBackground },
                            padding = column.padding(),
                            horizontalSpan = column.primaryTitleHorizontalSpan,
                            verticalSpan = column.primaryTitleVerticalSpan,
                    ) {
                        Text(
                                text = primaryTitle.get(),
                                fontWeight = FontWeight.Bold,
                                color = CodexTheme.colors.onListItemAppOnBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .wrapContentHeight(Alignment.CenterVertically)
                                        .updateHelpDialogPosition(helpState)
                                        .clearAndSetSemantics { }
                        )
                    }
                }
                if (column.primaryTitle == null && column.secondaryTitle == null) {
                    item {
                        Box {}
                    }
                }
            }
            columnMetadata.forEach { column ->
                column.secondaryTitle?.let { secondaryTitle ->
                    val helpState =
                            if (column.primaryTitleHorizontalSpan == 1 && column.primaryTitle != null) null
                            else column.getHelpState(resource)?.asHelpState(helpListener)
                    item(
                            backgroundColor = { CodexTheme.colors.listAccentRowItemOnAppBackground },
                            padding = column.padding(),
                    ) {
                        Text(
                                text = secondaryTitle.get(),
                                fontWeight = FontWeight.Bold,
                                color = CodexTheme.colors.onListItemAppOnBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .updateHelpDialogPosition(helpState)
                                        .clearAndSetSemantics { }
                        )
                    }
                }
            }

            config()
        }
    }
}

internal fun CodexGridRowMetadata.useAccentColor() = isTotal() || isAccentColor()
internal fun CodexGridRowMetadata.useBoldText() = isTotal() || isBoldText()
internal fun CodexGridColumnMetadata<*, *>.useAccentColor() = isTotal() || isAccentColor()
internal fun CodexGridColumnMetadata<*, *>.useBoldText() = isTotal() || isBoldText()

interface CodexGridRowMetadata {
    fun isTotal(): Boolean = false
    fun isAccentColor(): Boolean = false
    fun isBoldText(): Boolean = false
}

/**
 * A grid with the option of a primary and secondary header
 */
interface CodexGridColumnMetadata<RowData, ExtraData> {
    /**
     * The title displayed on the first header row
     */
    val primaryTitle: ResOrActual<String>?

    /**
     * How many columns [primaryTitle] should span
     */
    val primaryTitleHorizontalSpan: Int

    /**
     * How many rows [primaryTitle] should span (should be 1 or 2)
     */
    val primaryTitleVerticalSpan: Int

    /**
     * The title displayed on the second header row
     */
    val secondaryTitle: ResOrActual<String>?

    val helpTitle: ResOrActual<String>?
    val helpBody: ResOrActual<String>?
    val testTag: CodexTestTag?

    val mapping: (RowData) -> ResOrActual<String>?
    val cellContentDescription: (RowData, ExtraData) -> ResOrActual<String>?

    fun cellVerticalSpan(row: RowData): Int = 1
    fun cellHorizontalSpan(row: RowData): Int = 1

    fun isTotal(): Boolean = false
    fun isAccentColor(): Boolean = false
    fun isBoldText(): Boolean = false

    @Composable
    fun textColour(rowData: RowData): Color? = null

    fun padding(): PaddingValues = PaddingValues()

    fun getHelpState(resources: Resources): HelpShowcaseItem? {
        if (helpTitle == null || helpBody == null) return null
        return HelpShowcaseItem(
                helpTitle = helpTitle!!.get(resources),
                helpBody = helpBody!!.get(resources),
        )
    }
}
