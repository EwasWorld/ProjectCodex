package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.diActivityHelpers.ShootIdsUseCase
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.asDatabaseFullShootInfo
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.completeRound
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.components.viewScores.ViewScoresIntent.*
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreIntent
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBarIntent
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.ShootFilter
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
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

@OptIn(ExperimentalCoroutinesApi::class)
class ViewScoresViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()
    private val helpShowcase: HelpShowcaseUseCase = mock { }
    private val customLogger: CustomLogger = mock { }
    private val datastore = MockDatastore()
    private val shootIdsUseCase = ShootIdsUseCase()

    private fun getSut(datastoreUse2023System: Boolean = true): ViewScoresViewModel {
        datastore.values = mapOf(DatastoreKey.Use2023HandicapSystem to datastoreUse2023System)
        return ViewScoresViewModel(db.mock, helpShowcase, customLogger, datastore.mock, shootIdsUseCase)
    }

    /**
     * Check that [ViewScoresState.data] is updated correctly based on DB emitted values.
     * Checks that [ViewScoresEntry.isSelected] is preserved where appropriate.
     */
    @Test
    fun testDataIsSetAndUpdatedCorrectly_WithSelectedItem() = runTest {
        fun create(id: Int, date: Long) =
                DatabaseFullShootInfo(
                        shoot = DatabaseShoot(id, date.asCalendar(), 1),
                        arrows = listOf(DatabaseArrowScore(id, 1, 10, false)),
                )

        val shootsInitial = listOf(create(1, 5), create(3, 3))
        val shootsSecond = listOf(create(1, 5), create(2, 3))
        db.shootDao.fullShoots = shootsInitial
        db.shootDao.secondFullShoots = shootsSecond
        val sut = getSut()

        assertEquals(
                listOf<ViewScoresEntry>(),
                sut.state.value.data,
        )

        advanceTimeBy(1)
        assertEquals(
                shootsInitial.map {
                    ViewScoresEntry(FullShootInfo(it, true), false, customLogger)
                },
                sut.state.value.data.sortedBy { it.id },
        )

        sut.handle(MultiSelectAction(MultiSelectBarIntent.ClickOpen))
        sut.handle(EntryClicked(1))
        advanceUntilIdle()
        assertEquals(
                shootsSecond.map {
                    ViewScoresEntry(FullShootInfo(it, true), it.shoot.shootId == 1, customLogger)
                },
                sut.state.value.data.sortedBy { it.id },
        )
    }

    @Test
    fun testDataIsSetAndUpdatedCorrectly_WithOldHandicapSystem() = runTest {
        fun create(id: Int, date: Long) =
                DatabaseFullShootInfo(
                        shoot = DatabaseShoot(id, date.asCalendar(), 1),
                        arrows = listOf(DatabaseArrowScore(id, 1, 10, false)),
                )

        val shootsInitial = listOf(create(1, 5), create(3, 3))
        val shootsSecond = listOf(create(1, 5), create(2, 3))
        db.shootDao.fullShoots = shootsInitial
        db.shootDao.secondFullShoots = shootsSecond
        val sut = getSut(datastoreUse2023System = false)

        assertEquals(
                listOf<ViewScoresEntry>(),
                sut.state.value.data,
        )

        advanceTimeBy(1)
        assertEquals(
                shootsInitial.map {
                    ViewScoresEntry(FullShootInfo(it, false), false, customLogger)
                },
                sut.state.value.data.sortedBy { it.id },
        )
    }

    /**
     * Database call should be re-triggered with different arguments
     * Filter should be added
     */
    @Test
    fun testAddFilter() = runTest {
        val sut = getSut()
        advanceUntilIdle()

        assertEquals(Filters<ShootFilter>(), sut.state.value.filters)
        assertEquals(listOf<ViewScoresEntry>(), sut.state.value.data)
        verify(db.shootDao.mock).getAllFullShootInfo(false, null, null, null, null)

        sut.handle(AddFilter(ShootFilter.PersonalBests))
        advanceUntilIdle()

        assertEquals(Filters<ShootFilter>(setOf(ShootFilter.PersonalBests)), sut.state.value.filters)
        assertEquals(listOf<ViewScoresEntry>(), sut.state.value.data)
        verify(db.shootDao.mock).getAllFullShootInfo(true, null, null, null, null)

        verify(db.shootDao.mock, times(2))
                .getAllFullShootInfo(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun testMultiSelectStatesAndTransitions() = runTest {
        val shoots = listOf(
                ShootPreviewHelper.newFullShootInfo(1),
                ShootPreviewHelper.newFullShootInfo(2),
                ShootPreviewHelper.newFullShootInfo(3),
        )
        db.shootDao.fullShoots = shoots.map { it.asDatabaseFullShootInfo() }
        val sut = getSut()
        advanceUntilIdle()

        fun addSelected(isSelected: List<Boolean>): List<ViewScoresEntry> {
            check(isSelected.size == shoots.size) { "Invalid size" }
            return shoots.mapIndexed { index, far ->
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
        expectedState = expectedState.copy(openEmailClicked = true)
        checkState()

        sut.handle(HandledEmailOpened)
        expectedState = expectedState.copy(openEmailClicked = false)
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
            originalArrows = List(12) { DatabaseArrowScore(1, it, if (it < 6) 10 else 5, it < 6) },
            changedArrows = List(6) { DatabaseArrowScore(1, it, 10, false) },
            type = ConvertScoreType.XS_TO_TENS,
    )

    @Test
    fun testDropdownMenu_ConvertTO_FIVE_ZONE() = testConvertScore(
            originalArrows = List(12) { DatabaseArrowScore(1, it, if (it < 6) 10 else 5, it < 6) },
            changedArrows = List(6) { DatabaseArrowScore(1, it, 9, false) },
            type = ConvertScoreType.TO_FIVE_ZONE,
    )

    @Test
    fun testDropdownMenu_ConvertNoArrows() = testConvertScore(originalArrows = listOf())

    @Test
    fun testDropdownMenu_ConvertClose() = testConvertScore(
            originalArrows = List(12) { DatabaseArrowScore(1, it, if (it < 6) 10 else 5, it < 6) },
            testClose = true,
    )

    private fun testConvertScore(
            originalArrows: List<DatabaseArrowScore>,
            changedArrows: List<DatabaseArrowScore>? = null,
            type: ConvertScoreType = ConvertScoreType.TO_FIVE_ZONE,
            testClose: Boolean = false,
    ) = runTest {
        val shoot = ShootPreviewHelper
                .newFullShootInfo()
                .copy(arrows = originalArrows)
        var expectedState = ViewScoresState(
                data = listOf(ViewScoresEntry(info = shoot, isSelected = false, customLogger = customLogger))
        )
        db.shootDao.fullShoots = listOf(shoot.asDatabaseFullShootInfo())
        val sut = getSut()
        advanceUntilIdle()

        fun checkState() = assertEquals(expectedState, sut.state.value)
        checkState()

        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(
                dropdownMenuOpen = true,
                lastClickedEntryId = 1,
        )
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.CONVERT, 1))
        expectedState =
                expectedState.copy(dropdownMenuOpen = false, lastClickedEntryId = 1, convertScoreDialogOpen = true)
        checkState()

        advanceUntilIdle()
        verify(db.arrowScoreDao, never()).update(anyVararg())

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
            verify(db.arrowScoreDao, never()).update(anyVararg())
        }
        else {
            verify(db.arrowScoreDao).update(*changedArrows.toTypedArray())
        }
    }

    @Test
    fun testEntryClicked() = runTest {
        val shoot =
                listOf(
                        ShootPreviewHelper.newFullShootInfo(1),
                        ShootPreviewHelper.newFullShootInfo(2)
                                .addRound(RoundPreviewHelper.indoorMetricRoundData)
                                .completeRound(5),
                )
        var expectedState = ViewScoresState(
                data = shoot.map { ViewScoresEntry(info = it, isSelected = false, customLogger = customLogger) }
        ).reorderDataById()
        db.shootDao.fullShoots = shoot.map { it.asDatabaseFullShootInfo() }
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
        val shoot =
                listOf(
                        ShootPreviewHelper.newFullShootInfo(1),
                        ShootPreviewHelper.newFullShootInfo(2)
                                .addRound(RoundPreviewHelper.indoorMetricRoundData)
                                .completeRound(5),
                )
        var expectedState = ViewScoresState(
                data = shoot.map { ViewScoresEntry(info = it, isSelected = false, customLogger = customLogger) }
        ).reorderDataById()
        db.shootDao.fullShoots = shoot.map { it.asDatabaseFullShootInfo() }
        val sut = getSut()
        advanceUntilIdle()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        checkState()

        // Open dropdown - with continue
        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(dropdownMenuOpen = true, lastClickedEntryId = 1)
        checkState()

        // Close
        sut.handle(DropdownMenuClosed)
        expectedState = expectedState.copy(dropdownMenuOpen = false, lastClickedEntryId = 1)
        checkState()

        // Open dropdown - no continue
        sut.handle(EntryLongClicked(2))
        expectedState = expectedState.copy(dropdownMenuOpen = true, lastClickedEntryId = 2)
        checkState()

        // Close
        sut.handle(DropdownMenuClosed)
        expectedState = expectedState.copy(dropdownMenuOpen = false, lastClickedEntryId = 2)
        checkState()
    }

    @Test
    fun testDropdownMenu_Options() = runTest {
        val shoot = ShootPreviewHelper.newFullShootInfo()
                .addRound(RoundPreviewHelper.indoorMetricRoundData)
        var expectedState = ViewScoresState(
                data = listOf(ViewScoresEntry(info = shoot, isSelected = false, customLogger = customLogger))
        )
        db.shootDao.fullShoots = listOf(shoot.asDatabaseFullShootInfo())
        val sut = getSut()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        advanceUntilIdle()
        checkState()

        fun openDropdown() {
            sut.handle(EntryLongClicked(1))
            expectedState = expectedState.copy(dropdownMenuOpen = true, lastClickedEntryId = 1)
            checkState()
        }

        // SCORE_PAD
        openDropdown()
        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.SCORE_PAD, 1))
        expectedState = expectedState.copy(openScorePadClicked = true, dropdownMenuOpen = false)
        checkState()

        sut.handle(HandledScorePadOpened)
        expectedState = expectedState.copy(openScorePadClicked = false)
        checkState()

        // EMAIL_SCORE
        openDropdown()
        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.EMAIL_SCORE, 1))
        expectedState = expectedState.copy(openEmailClicked = true, dropdownMenuOpen = false)
        checkState()

        sut.handle(HandledEmailOpened)
        expectedState = expectedState.copy(openEmailClicked = false)
        checkState()

        // EDIT_INFO
        openDropdown()
        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.EDIT_INFO, 1))
        expectedState = expectedState.copy(openEditInfoClicked = true, dropdownMenuOpen = false)
        checkState()

        sut.handle(HandledEditInfoOpened)
        expectedState = expectedState.copy(openEditInfoClicked = false)
        checkState()
    }

    @Test
    fun testDropdownMenu_Continue() = runTest {
        val shoot =
                listOf(
                        ShootPreviewHelper.newFullShootInfo(1),
                        ShootPreviewHelper.newFullShootInfo(2)
                                .addRound(RoundPreviewHelper.indoorMetricRoundData)
                                .completeRound(5),
                )
        var expectedState = ViewScoresState(
                data = shoot.map { ViewScoresEntry(info = it, isSelected = false, customLogger = customLogger) }
        ).reorderDataById()
        db.shootDao.fullShoots = shoot.map { it.asDatabaseFullShootInfo() }
        val sut = getSut()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        advanceUntilIdle()
        checkState()

        // Continue incomplete round
        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(dropdownMenuOpen = true, lastClickedEntryId = 1)
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.CONTINUE, 1))
        expectedState = expectedState.copy(openInputEndClicked = true, dropdownMenuOpen = false)
        checkState()

        sut.handle(HandledInputEndOpened)
        expectedState = expectedState.copy(openInputEndClicked = false)
        checkState()

        // Continue completed round
        sut.handle(EntryLongClicked(2))
        expectedState = expectedState.copy(dropdownMenuOpen = true, lastClickedEntryId = 2)
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.CONTINUE, 2))
        expectedState = expectedState.copy(openInputEndOnCompletedRound = true, dropdownMenuOpen = false)
        checkState()

        sut.handle(HandledInputEndOnCompletedRound)
        expectedState = expectedState.copy(openInputEndOnCompletedRound = false)
        checkState()
    }

    @Test
    fun testDropdownMenu_Delete() = runTest {
        val shoot = ShootPreviewHelper.newFullShootInfo()
                .addRound(RoundPreviewHelper.indoorMetricRoundData)
        var expectedState = ViewScoresState(
                data = listOf(ViewScoresEntry(info = shoot, isSelected = false, customLogger = customLogger))
        )
        db.shootDao.fullShoots = listOf(shoot.asDatabaseFullShootInfo())
        val sut = getSut()
        fun checkState() = assertEquals(expectedState, sut.state.value.reorderDataById())
        advanceUntilIdle()
        checkState()

        // Open -> Delete -> Cancel
        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(dropdownMenuOpen = true, lastClickedEntryId = 1)
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.DELETE, 1))
        expectedState = expectedState.copy(deleteDialogOpen = true, dropdownMenuOpen = false)
        checkState()

        sut.handle(DeleteDialogCancelClicked)
        expectedState = expectedState.copy(deleteDialogOpen = false)
        checkState()

        // Open -> Delete -> OK
        sut.handle(EntryLongClicked(1))
        expectedState = expectedState.copy(dropdownMenuOpen = true, lastClickedEntryId = 1)
        checkState()

        sut.handle(DropdownMenuClicked(ViewScoresDropdownMenuItem.DELETE, 1))
        expectedState = expectedState.copy(deleteDialogOpen = true, dropdownMenuOpen = false)
        checkState()

        advanceUntilIdle()
        verify(db.shootDao.mock, never()).deleteRound(any())

        sut.handle(DeleteDialogOkClicked)
        expectedState = expectedState.copy(deleteDialogOpen = false)
        checkState()

        advanceUntilIdle()
        verify(db.shootDao.mock).deleteRound(1)
    }

    @Test
    fun testNoRoundsDialogOkClicked() = runTest {
        db.shootDao.fullShoots = listOf()
        val sut = getSut()
        advanceUntilIdle()
        assertEquals(ViewScoresState(), sut.state.value.reorderDataById())

        sut.handle(NoRoundsDialogOkClicked)
        assertEquals(ViewScoresState(noRoundsDialogOkClicked = true), sut.state.value)

        sut.handle(HandledNoRoundsDialogOkClicked)
        assertEquals(ViewScoresState(noRoundsDialogOkClicked = false), sut.state.value)
    }

    private fun ViewScoresState.reorderDataById() = copy(data = data.sortedBy { it.id })
}
