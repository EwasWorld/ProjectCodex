package eywa.projectcodex.common

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent.*
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SetRounds
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SubTypeIntent.*
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundFilter
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectRoundDialogIntentUnitTest {
    private val roundsData = listOf(
            RoundPreviewHelper.outdoorImperialRoundData,
            RoundPreviewHelper.indoorMetricRoundData,
            RoundPreviewHelper.singleSubtypeRoundData,
    )

    @Test
    fun testSetRounds() {
        assertEquals(
                SelectRoundDialogState(allRounds = roundsData) to SelectRoundFaceDialogIntent.SetNoRound,
                SetRounds(roundsData).handle(SelectRoundDialogState()),
        )
        assertEquals(
                SelectRoundDialogState(allRounds = emptyList()) to
                        SelectRoundFaceDialogIntent.SetNoRound,
                SetRounds(emptyList()).handle(SelectRoundDialogState(allRounds = roundsData, isRoundDialogOpen = true)),
        )
        assertEquals(
                SelectRoundDialogState(allRounds = roundsData.take(1), isRoundDialogOpen = true) to
                        SelectRoundFaceDialogIntent.SetNoRound,
                SetRounds(roundsData.take(1))
                        .handle(SelectRoundDialogState(allRounds = roundsData, isRoundDialogOpen = true)),
        )

        val outdoorRound = RoundPreviewHelper.outdoorImperialRoundData
        val filters = SelectRoundEnabledFilters().plus(SelectRoundFilter.IMPERIAL)
        assertEquals(
                SelectRoundDialogState(
                        allRounds = emptyList(),
                        filters = filters,
                ) to SelectRoundFaceDialogIntent.SetNoRound,
                SetRounds(emptyList()).handle(
                        SelectRoundDialogState(
                                isSubtypeDialogOpen = true,
                                selectedRoundId = outdoorRound.round.roundId,
                                selectedSubTypeId = outdoorRound.roundSubTypes!!.last().subTypeId,
                                allRounds = roundsData,
                                filters = filters,
                        )
                ),
        )
    }

    @Test
    fun testOpenRoundDialog() {
        var state = SelectRoundDialogState(allRounds = roundsData)

        assertEquals(
                state.copy(isRoundDialogOpen = true) to null,
                OpenRoundDialog.handle(state),
        )
        assertEquals(
                state.copy(isRoundDialogOpen = true) to null,
                OpenRoundDialog.handle(state.copy(isRoundDialogOpen = true)),
        )
        assertEquals(
                state.copy(isRoundDialogOpen = true) to null,
                OpenRoundDialog.handle(
                        state.copy(filters = SelectRoundEnabledFilters().plus(SelectRoundFilter.IMPERIAL)),
                ),
        )

        state = SelectRoundDialogState(
                selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                allRounds = roundsData,
                isSubtypeDialogOpen = true,
        )
        assertEquals(
                state to null,
                OpenRoundDialog.handle(state),
        )
        assertEquals(
                SelectRoundDialogState() to null,
                OpenRoundDialog.handle(SelectRoundDialogState()),
        )
    }

    @Test
    fun testCloseRoundDialog() {
        val state = SelectRoundDialogState(allRounds = roundsData)

        assertEquals(
                state.copy(isRoundDialogOpen = false) to null,
                CloseRoundDialog.handle(state),
        )
        assertEquals(
                state.copy(isRoundDialogOpen = false) to null,
                CloseRoundDialog.handle(state.copy(isRoundDialogOpen = true)),
        )
    }

    @Test
    fun testNoRoundSelected() {
        val roundInfo = RoundPreviewHelper.outdoorImperialRoundData
        val state = SelectRoundDialogState(allRounds = roundsData, isRoundDialogOpen = true)

        val stateWithSelectedRound = state.copy(
                selectedRoundId = roundInfo.round.roundId,
                selectedSubTypeId = roundInfo.roundSubTypes!![1].subTypeId,
        )
        assertEquals(
                state.copy(isRoundDialogOpen = false) to SelectRoundFaceDialogIntent.SetNoRound,
                NoRoundSelected.handle(stateWithSelectedRound),
        )
        assertEquals(
                stateWithSelectedRound.copy(isRoundDialogOpen = false) to null,
                NoRoundSelected.handle(stateWithSelectedRound.copy(isRoundDialogOpen = false)),
        )
    }

    @Test
    fun testRoundSelected() {
        val roundInfo1 = RoundPreviewHelper.outdoorImperialRoundData
        val roundInfo2 = RoundPreviewHelper.indoorMetricRoundData
        val state = SelectRoundDialogState(allRounds = roundsData, isRoundDialogOpen = true)

        val stateWithSelectedRound = state.copy(
                selectedRoundId = roundInfo1.round.roundId,
                selectedSubTypeId = roundInfo1.roundSubTypes!![1].subTypeId,
        )

        assertEquals(
                stateWithSelectedRound.copy(
                        isRoundDialogOpen = false,
                        selectedRoundId = roundInfo2.round.roundId,
                        selectedSubTypeId = roundInfo2.roundSubTypes!![1].subTypeId,
                ) to SelectRoundFaceDialogIntent.SetRound(roundInfo2.round, roundInfo2.roundDistances!!.takeLast(1)),
                RoundSelected(roundInfo2.round).handle(stateWithSelectedRound),
        )
        assertEquals(
                stateWithSelectedRound.copy(isRoundDialogOpen = false) to null,
                RoundSelected(roundInfo2.round).handle(stateWithSelectedRound.copy(isRoundDialogOpen = false)),
        )
    }

    @Test
    fun testFilterClicked() {
        val state = SelectRoundDialogState(allRounds = roundsData, isRoundDialogOpen = true)
        val withImperialFilter = SelectRoundEnabledFilters().plus(SelectRoundFilter.IMPERIAL)

        assertEquals(
                state.copy(filters = withImperialFilter) to null,
                FilterClicked(SelectRoundFilter.IMPERIAL).handle(state),
        )
        assertEquals(
                state to null,
                FilterClicked(SelectRoundFilter.IMPERIAL).handle(state.copy(filters = withImperialFilter)),
        )

        assertEquals(
                state.copy(filters = withImperialFilter.plus(SelectRoundFilter.INDOOR)) to null,
                FilterClicked(SelectRoundFilter.INDOOR).handle(state.copy(filters = withImperialFilter)),
        )

        assertEquals(
                SelectRoundDialogState() to null,
                FilterClicked(SelectRoundFilter.IMPERIAL).handle(SelectRoundDialogState()),
        )
    }

    @Test
    fun testClearFilters() {
        val withImperialFilter = SelectRoundEnabledFilters().plus(SelectRoundFilter.IMPERIAL)

        assertEquals(
                SelectRoundDialogState() to null,
                ClearFilters.handle(SelectRoundDialogState(filters = withImperialFilter)),
        )
        assertEquals(
                SelectRoundDialogState() to null,
                ClearFilters
                        .handle(SelectRoundDialogState(filters = withImperialFilter.plus(SelectRoundFilter.INDOOR))),
        )
    }

    @Test
    fun testOpenSubTypeDialog() {
        var state = SelectRoundDialogState(
                selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                allRounds = roundsData,
        )

        assertEquals(
                state.copy(isSubtypeDialogOpen = true) to null,
                OpenSubTypeDialog.handle(state),
        )
        assertEquals(
                state.copy(isSubtypeDialogOpen = true) to null,
                OpenSubTypeDialog.handle(state.copy(isSubtypeDialogOpen = true)),
        )

        assertEquals(
                state.copy(selectedRoundId = null) to null,
                OpenSubTypeDialog.handle(state.copy(selectedRoundId = null)),
        )
        state = state.copy(selectedRoundId = RoundPreviewHelper.singleSubtypeRoundData.round.roundId)
        assertEquals(
                state to null,
                OpenSubTypeDialog.handle(state),
        )
    }

    @Test
    fun testCloseSubTypeDialog() {
        val state = SelectRoundDialogState(
                selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                allRounds = roundsData,
        )

        assertEquals(
                state.copy(isSubtypeDialogOpen = false) to null,
                CloseSubTypeDialog.handle(state),
        )
        assertEquals(
                state.copy(isSubtypeDialogOpen = false) to null,
                CloseSubTypeDialog.handle(state.copy(isSubtypeDialogOpen = true)),
        )
    }

    @Test
    fun testSubTypeSelected() {
        val roundInfo = RoundPreviewHelper.outdoorImperialRoundData
        val state = SelectRoundDialogState(
                selectedRoundId = roundInfo.round.roundId,
                allRounds = roundsData,
                isSubtypeDialogOpen = true,
        )

        assertEquals(
                state.copy(
                        isSubtypeDialogOpen = false,
                        selectedSubTypeId = 2,
                ) to SelectRoundFaceDialogIntent.SetDistances(
                        roundInfo.roundDistances!!.takeLast(2),
                ),
                SubTypeSelected(roundInfo.roundSubTypes!![1]).handle(state),
        )

        assertEquals(
                state.copy(isSubtypeDialogOpen = false) to null,
                SubTypeSelected(roundInfo.roundSubTypes!![1]).handle(state.copy(isSubtypeDialogOpen = false)),
        )
    }
}
