package eywa.projectcodex.components.viewScores.utils

import androidx.compose.foundation.lazy.LazyListState
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.*
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.viewScores.ui.ViewScoreHelpPriority
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class ViewScoresShowcaseInfo(
        /**
         * The classes of the entries currently being displayed in order
         */
        private val entryClasses: List<KClass<*>>,
        private val lazyListState: LazyListState,
) : DynamicHelpShowcaseInfo {
    override val type: KClass<out ActionBarHelp> = CodexNavRoute.VIEW_SCORES::class

    /**
     * The height in px of the list of entries that is unobstructed, used to decide which row's
     * [HelpShowcaseItem]s should be shown
     */
    var unobstructedHeight: Float = 0f

    /**
     * The help info of the entries currently being displayed.
     * This info is specific to the row type.
     * Indexes match that in [entryClasses]
     */
    val specificEntryHelpInfo: List<HelpShowcaseUseCase> = List(entryClasses.size) { HelpShowcaseUseCase() }

    /**
     * The help info of the entries currently being displayed.
     * This info is generic and common to all rows.
     * Indexes match that in [entryClasses]
     */
    val genericEntryHelpInfo: List<HelpShowcaseUseCase> = List(entryClasses.size) {
        HelpShowcaseUseCase().apply {
            handle(
                    HelpShowcaseIntent.Add(
                            HelpShowcaseItem(
                                    helpTitle = R.string.help_view_score__row_title,
                                    helpBody = R.string.help_view_score__row_body,
                                    priority = ViewScoreHelpPriority.GENERIC_ROW_ACTIONS.ordinal
                            )
                    ),
                    CodexNavRoute.VIEW_SCORES::class,
            )
        }
    }

    private var currentShowcase: Map<ResOrActual<String>, HelpShowcaseItem>? = null

    override fun start(): Collection<HelpShowcaseItem> {
        // Wait for any scrolling to complete
        while (lazyListState.isScrollInProgress) {
            runBlocking {
                delay(100)
            }
        }

        val fullyVisibleItems = lazyListState.layoutInfo.visibleItemsInfo
                // Ignore indexes that are only partially visible
                .filter {
                    it.offset >= 0 && it.offset + it.size < unobstructedHeight
                }

        val genericItemHelp = genericEntryHelpInfo[fullyVisibleItems[0].index]
        val specificItemHelp = fullyVisibleItems
                .distinctBy { entryClasses[it.index] }
                .map { specificEntryHelpInfo[it.index] }

        currentShowcase = HelpShowcaseUseCase.combineContent(listOf(genericItemHelp).plus(specificItemHelp))
        return currentShowcase!!.values
    }

    override fun end() {
        currentShowcase = null
    }

    override fun getInfoShowcases(key: ResOrActual<String>): HelpShowcaseItem? = currentShowcase!![key]
}
