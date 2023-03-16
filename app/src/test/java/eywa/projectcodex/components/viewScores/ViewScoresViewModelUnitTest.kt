package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.helpShowcase.HelpShowcase
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.asDatabaseFullArcherRoundInfo
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.completeRound
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.viewScores.ViewScoresIntent.*
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreIntent
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBarIntent
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ViewScoresViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val helpShowcase: HelpShowcase = mock { }
    private val customLogger: CustomLogger = mock { }

    private fun getSut() = ViewScoresViewModel(db.mock, helpShowcase, customLogger)

    @Test
    fun testPbsAreSet() = runTest {
        val pbs = listOf(1, 2, 4)
        db.archerRoundDao.personalBests = pbs
        val sut = getSut()

        advanceUntilIdle()
        assertEquals(pbs, sut.state.value.personalBestArcherRoundIds)
    }

    /**
     * Check that [ViewScoresState.data] is updated correctly based on DB emitted values.
     * Checks that [ViewScoresEntry.isSelected] is preserved where appropriate.
     */
    @Test
    fun testDataIsSetAndUpdatedCorrectly_WithSelectedItem() = runTest {
        fun create(id: Int, date: Long) =
                DatabaseFullArcherRoundInfo(
                        archerRound = ArcherRound(id, Date(date), 1),
                        arrows = listOf(ArrowValue(id, 1, 10, false)),
                )

        val archerRoundsInitial = listOf(create(1, 5), create(3, 3))
        val archerRoundsSecond = listOf(create(1, 5), create(2, 3))
        db.archerRoundDao.fullArcherRounds = archerRoundsInitial
        db.archerRoundDao.secondFullArcherRounds = archerRoundsSecond
        val sut = getSut()

        assertEquals(
                listOf<ViewScoresEntry>(),
                sut.state.value.data,
        )

        advanceTimeBy(1)
        assertEquals(
                archerRoundsInitial.map {
                    ViewScoresEntry(FullArcherRoundInfo(it), false, customLogger)
                },
                sut.state.value.data,
        )

        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickOpen))
        sut.handle(EntryClicked(1))
        advanceUntilIdle()
        assertEquals(
                archerRoundsSecond.map {
                    ViewScoresEntry(FullArcherRoundInfo(it), it.archerRound.archerRoundId == 1, customLogger)
                },
                sut.state.value.data,
        )
    }

    @Test
    fun testHelpShowcaseAction() {
        getSut().handle(HelpShowcaseAction(HelpShowcaseIntent.Clear))
        verify(helpShowcase).handle(HelpShowcaseIntent.Clear, ViewScoresFragment::class)
    }

    @Test
    fun testMultiSelectStatesAndTransitions() = runTest {
        val archerRounds = listOf(
                ArcherRoundPreviewHelper.newFullArcherRoundInfo(1),
                ArcherRoundPreviewHelper.newFullArcherRoundInfo(2),
                ArcherRoundPreviewHelper.newFullArcherRoundInfo(3),
        )
        db.archerRoundDao.fullArcherRounds = archerRounds.map { it.asDatabaseFullArcherRoundInfo() }
        val sut = getSut()
        advanceUntilIdle()

        fun addSelected(isSelected: List<Boolean>): List<ViewScoresEntry> {
            check(isSelected.size == archerRounds.size) { "Invalid size" }
            return archerRounds.mapIndexed { index, far ->
                ViewScoresEntry(far, isSelected[index], customLogger)
            }
        }

        var expectedState = ViewScoresState(data = addSelected(List(3) { false }))
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())

        checkState()

        // Turn on
        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickOpen))
        expectedState = expectedState.copy(isInMultiSelectMode = true)
        checkState()

        // Email with no selection
        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickEmail))
        expectedState = expectedState.copy(multiSelectEmailNoSelection = true)
        checkState()

        sut.handle(HandledEmailNoSelection)
        expectedState = expectedState.copy(multiSelectEmailNoSelection = false)
        checkState()

        // All or none
        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickAllOrNone))
        expectedState = expectedState.copy(data = addSelected(List(3) { true }))
        checkState()

        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickAllOrNone))
        expectedState = expectedState.copy(data = addSelected(List(3) { false }))
        checkState()

        // Individual select
        sut.handle(EntryClicked(2))
        expectedState = expectedState.copy(data = addSelected(listOf(false, true, false)))
        checkState()

        sut.handle(EntryClicked(1))
        expectedState = expectedState.copy(data = addSelected(listOf(true, true, false)))
        checkState()

        sut.handle(EntryClicked(1))
        expectedState = expectedState.copy(data = addSelected(listOf(false, true, false)))
        checkState()

        // All or none from some selected
        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickAllOrNone))
        expectedState = expectedState.copy(data = addSelected(List(3) { true }))
        checkState()

        // Email
        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickEmail))
        expectedState = expectedState.copy(multiSelectEmailClicked = true)
        checkState()

        sut.handle(HandledEmailClicked)
        expectedState = expectedState.copy(multiSelectEmailClicked = false)
        checkState()

        // Turn off
        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickClose))
        expectedState = expectedState.copy(isInMultiSelectMode = false, data = addSelected(List(3) { false }))
        checkState()

        // Action while off
        try {
            sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickEmail))
            fail("Did not throw exception")
        }
        catch (_: IllegalStateException) {
        }
    }

    @Test
    fun testDropdownMenu_ConvertXS_TO_TENS() = testConvertScore(
            originalArrows = List(12) { ArrowValue(1, it, if (it < 6) 10 else 5, it < 6) },
            changedArrows = List(6) { ArrowValue(1, it, 10, false) },
            type = ConvertScoreType.XS_TO_TENS,
    )

    @Test
    fun testDropdownMenu_ConvertTO_FIVE_ZONE() = testConvertScore(
            originalArrows = List(12) { ArrowValue(1, it, if (it < 6) 10 else 5, it < 6) },
            changedArrows = List(6) { ArrowValue(1, it, 9, false) },
            type = ConvertScoreType.TO_FIVE_ZONE,
    )

    @Test
    fun testDropdownMenu_ConvertNoArrows() = testConvertScore(originalArrows = listOf())

    @Test
    fun testDropdownMenu_ConvertClose() = testConvertScore(
            originalArrows = List(12) { ArrowValue(1, it, if (it < 6) 10 else 5, it < 6) },
            testClose = true,
    )

    private fun testConvertScore(
            originalArrows: List<ArrowValue>,
            changedArrows: List<ArrowValue>? = null,
            type: ConvertScoreType = ConvertScoreType.TO_FIVE_ZONE,
            testClose: Boolean = false,
    ) = runTest {
        val archerRound = ArcherRoundPreviewHelper
                .newFullArcherRoundInfo()
                .copy(arrows = originalArrows)
        var expectedState = ViewScoresState(
                data = listOf(ViewScoresEntry(info = archerRound, isSelected = false, customLogger = customLogger))
        )
        db.archerRoundDao.fullArcherRounds = listOf(archerRound.asDatabaseFullArcherRoundInfo())
        val sut = getSut()
        advanceUntilIdle()

        fun checkState() = assertEquals(expectedState, sut.state.value)
        checkState()

        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(
                dropdownItems = listOf(
                        ViewScoresDropdownMenuItem.SCORE_PAD,
                        ViewScoresDropdownMenuItem.CONTINUE,
                        ViewScoresDropdownMenuItem.EMAIL_SCORE,
                        ViewScoresDropdownMenuItem.EDIT_INFO,
                        ViewScoresDropdownMenuItem.DELETE,
                        ViewScoresDropdownMenuItem.CONVERT,
                ),
                lastClickedEntryId = 1,
        )
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.CONVERT))
        expectedState = expectedState.copy(dropdownItems = null, lastClickedEntryId = 1, convertScoreDialogOpen = true)
        checkState()

        advanceUntilIdle()
        verify(db.arrowValueDao, never()).update(anyVararg())

        if (testClose) {
            sut.handle(ConvertScoreAction(ConvertScoreIntent.Close))
        }
        else {
            sut.handle(ConvertScoreAction(ConvertScoreIntent.Ok(type)))
        }
        expectedState = expectedState.copy(convertScoreDialogOpen = false)
        checkState()

        advanceUntilIdle()
        if (changedArrows.isNullOrEmpty() || testClose) {
            verify(db.arrowValueDao, never()).update(anyVararg())
        }
        else {
            verify(db.arrowValueDao).update(*changedArrows.toTypedArray())
        }
    }

    @Test
    fun testEntryClicked() = runTest {
        val archerRound =
                listOf(
                        ArcherRoundPreviewHelper.newFullArcherRoundInfo(1),
                        ArcherRoundPreviewHelper.newFullArcherRoundInfo(2)
                                .addRound(RoundPreviewHelper.indoorMetricRoundData)
                                .completeRound(5),
                )
        var expectedState = ViewScoresState(
                data = archerRound.map { ViewScoresEntry(info = it, isSelected = false, customLogger = customLogger) }
        ).reorderDataById()
        db.archerRoundDao.fullArcherRounds = archerRound.map { it.asDatabaseFullArcherRoundInfo() }
        val sut = getSut()
        advanceUntilIdle()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        checkState()

        // Normal
        sut.handle(EntryClicked(2))
        expectedState = expectedState.copy(lastClickedEntryId = 2, openScorePadClicked = true)
        checkState()

        sut.handle(HandledScorePadOpened)
        expectedState = expectedState.copy(openScorePadClicked = false)
        checkState()

        // Multiselect
        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickOpen))
        expectedState = expectedState.copy(isInMultiSelectMode = true)
        checkState()

        sut.handle(EntryClicked(1))
        expectedState = expectedState.copy(
                data = expectedState.data.map { if (it.id == 1) it.copy(isSelected = true) else it },
        )
        checkState()
    }

    @Test
    fun testDropdownMenu_OpenDisplayClose() = runTest {
        val archerRound =
                listOf(
                        ArcherRoundPreviewHelper.newFullArcherRoundInfo(1),
                        ArcherRoundPreviewHelper.newFullArcherRoundInfo(2)
                                .addRound(RoundPreviewHelper.indoorMetricRoundData)
                                .completeRound(5),
                )
        var expectedState = ViewScoresState(
                data = archerRound.map { ViewScoresEntry(info = it, isSelected = false, customLogger = customLogger) }
        ).reorderDataById()
        db.archerRoundDao.fullArcherRounds = archerRound.map { it.asDatabaseFullArcherRoundInfo() }
        val sut = getSut()
        advanceUntilIdle()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        checkState()

        // Open dropdown - with continue
        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(dropdownItems = allDropdownItems, lastClickedEntryId = 1)
        checkState()

        // Close
        sut.handle(DropdownMenuClosed)
        expectedState = expectedState.copy(dropdownItems = null, lastClickedEntryId = 1)
        checkState()

        // Open dropdown - no continue
        sut.handle(EntryLongClicked(2))
        expectedState = expectedState.copy(dropdownItems = noContinueDropdownItems, lastClickedEntryId = 2)
        checkState()

        // Close
        sut.handle(DropdownMenuClosed)
        expectedState = expectedState.copy(dropdownItems = null, lastClickedEntryId = 2)
        checkState()
    }

    @Test
    fun testDropdownMenu_Options() = runTest {
        val archerRound = ArcherRoundPreviewHelper.newFullArcherRoundInfo()
                .addRound(RoundPreviewHelper.indoorMetricRoundData)
        var expectedState = ViewScoresState(
                data = listOf(ViewScoresEntry(info = archerRound, isSelected = false, customLogger = customLogger))
        )
        db.archerRoundDao.fullArcherRounds = listOf(archerRound.asDatabaseFullArcherRoundInfo())
        val sut = getSut()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        advanceUntilIdle()
        checkState()

        fun openDropdown() {
            sut.handle(EntryLongClicked(1))
            expectedState = expectedState.copy(dropdownItems = allDropdownItems, lastClickedEntryId = 1)
            checkState()
        }

        // SCORE_PAD
        openDropdown()
        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.SCORE_PAD))
        expectedState = expectedState.copy(openScorePadClicked = true, dropdownItems = null)
        checkState()

        sut.handle(HandledScorePadOpened)
        expectedState = expectedState.copy(openScorePadClicked = false)
        checkState()

        // EMAIL_SCORE
        openDropdown()
        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.EMAIL_SCORE))
        expectedState = expectedState.copy(openEmailClicked = true, dropdownItems = null)
        checkState()

        sut.handle(HandledEmailOpened)
        expectedState = expectedState.copy(openEmailClicked = false)
        checkState()

        // EDIT_INFO
        openDropdown()
        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.EDIT_INFO))
        expectedState = expectedState.copy(openEditInfoClicked = true, dropdownItems = null)
        checkState()

        sut.handle(HandledEditInfoOpened)
        expectedState = expectedState.copy(openEditInfoClicked = false)
        checkState()
    }

    @Test
    fun testDropdownMenu_Continue() = runTest {
        val archerRound =
                listOf(
                        ArcherRoundPreviewHelper.newFullArcherRoundInfo(1),
                        ArcherRoundPreviewHelper.newFullArcherRoundInfo(2)
                                .addRound(RoundPreviewHelper.indoorMetricRoundData)
                                .completeRound(5),
                )
        var expectedState = ViewScoresState(
                data = archerRound.map { ViewScoresEntry(info = it, isSelected = false, customLogger = customLogger) }
        ).reorderDataById()
        db.archerRoundDao.fullArcherRounds = archerRound.map { it.asDatabaseFullArcherRoundInfo() }
        val sut = getSut()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        advanceUntilIdle()
        checkState()

        // Continue incomplete round
        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(dropdownItems = allDropdownItems, lastClickedEntryId = 1)
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.CONTINUE))
        expectedState = expectedState.copy(openInputEndClicked = true, dropdownItems = null)
        checkState()

        sut.handle(HandledInputEndOpened)
        expectedState = expectedState.copy(openInputEndClicked = false)
        checkState()

        // Continue completed round
        sut.handle(EntryLongClicked(2))
        expectedState = expectedState.copy(dropdownItems = noContinueDropdownItems, lastClickedEntryId = 2)
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.CONTINUE))
        expectedState = expectedState.copy(openInputEndOnCompletedRound = true)
        checkState()

        sut.handle(HandledInputEndOnCompletedRound)
        expectedState = expectedState.copy(openInputEndOnCompletedRound = false)
        checkState()
    }

    @Test
    fun testDropdownMenu_Delete() = runTest {
        val archerRound = ArcherRoundPreviewHelper.newFullArcherRoundInfo()
                .addRound(RoundPreviewHelper.indoorMetricRoundData)
        var expectedState = ViewScoresState(
                data = listOf(ViewScoresEntry(info = archerRound, isSelected = false, customLogger = customLogger))
        )
        db.archerRoundDao.fullArcherRounds = listOf(archerRound.asDatabaseFullArcherRoundInfo())
        val sut = getSut()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        advanceUntilIdle()
        checkState()

        // Open -> Delete -> Cancel
        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(dropdownItems = allDropdownItems, lastClickedEntryId = 1)
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.DELETE))
        expectedState = expectedState.copy(deleteDialogOpen = true, dropdownItems = null)
        checkState()

        sut.handle(DeleteDialogCancelClicked)
        expectedState = expectedState.copy(deleteDialogOpen = false)
        checkState()

        // Open -> Delete -> OK
        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(dropdownItems = allDropdownItems, lastClickedEntryId = 1)
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.DELETE))
        expectedState = expectedState.copy(deleteDialogOpen = true, dropdownItems = null)
        checkState()

        advanceUntilIdle()
        verify(db.archerRoundDao.mock, never()).deleteRound(any())

        sut.handle(DeleteDialogOkClicked)
        expectedState = expectedState.copy(deleteDialogOpen = false)
        checkState()

        advanceUntilIdle()
        verify(db.archerRoundDao.mock).deleteRound(1)
    }

    @Test
    fun testNoRoundsDialogOkClicked() = runTest {
        db.archerRoundDao.fullArcherRounds = listOf()
        val sut = getSut()
        advanceUntilIdle()
        assertEquals(ViewScoresState(), sut.state.value.reorderDataById())

        sut.handle(NoRoundsDialogOkClicked)
        assertEquals(ViewScoresState(noRoundsDialogOkClicked = true), sut.state.value)

        sut.handle(HandledNoRoundsDialogOkClicked)
        assertEquals(ViewScoresState(noRoundsDialogOkClicked = false), sut.state.value)
    }

    private fun ViewScoresState.reorderDataById() = copy(data = data.sortedBy { it.id })

    companion object {
        private val allDropdownItems = listOf(
                ViewScoresDropdownMenuItem.SCORE_PAD,
                ViewScoresDropdownMenuItem.CONTINUE,
                ViewScoresDropdownMenuItem.EMAIL_SCORE,
                ViewScoresDropdownMenuItem.EDIT_INFO,
                ViewScoresDropdownMenuItem.DELETE,
                ViewScoresDropdownMenuItem.CONVERT,
        )
        private val noContinueDropdownItems =
                allDropdownItems.filterNot { it == ViewScoresDropdownMenuItem.CONTINUE }
    }
}
