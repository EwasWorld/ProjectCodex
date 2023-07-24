package eywa.projectcodex.common

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectRoundDialogStateUnitTest {
    private val roundsData = listOf(
            RoundPreviewHelper.outdoorImperialRoundData,
            RoundPreviewHelper.indoorMetricRoundData,
            RoundPreviewHelper.singleSubtypeRoundData,
    )
    private val outdoorRound = RoundPreviewHelper.outdoorImperialRoundData

    // TODO_CURRENT Check throws in init

    @Test
    fun testRoundInfo() {
        val state = SelectRoundDialogState(allRounds = roundsData)
        assertEquals(
                null,
                state.selectedRound,
        )
        assertEquals(
                outdoorRound,
                state.copy(allRounds = roundsData, selectedRoundId = outdoorRound.round.roundId).selectedRound,
        )
    }

    @Test
    fun testRoundSubTypeDistances() {
        val state = SelectRoundDialogState(allRounds = roundsData)
        assertEquals(
                null,
                state.roundSubTypeDistances,
        )
        assertEquals(
                outdoorRound.roundDistances!!.subList(0, 2),
                state.copy(
                        selectedRoundId = outdoorRound.round.roundId,
                        selectedSubTypeId = outdoorRound.roundSubTypes!![0].subTypeId,
                ).roundSubTypeDistances,
        )
    }

    @Test
    fun testDisplayedSubtype() {
        val state = SelectRoundDialogState(allRounds = roundsData)
        assertEquals(
                null,
                state.selectedSubType,
        )
        // Only one subtype
        assertEquals(
                null,
                state.copy(
                        allRounds = roundsData,
                        selectedRoundId = RoundPreviewHelper.singleSubtypeRoundData.round.roundId,
                        selectedSubTypeId = RoundPreviewHelper.singleSubtypeRoundData.roundSubTypes!![0].subTypeId,
                ).selectedSubType,
        )
        assertEquals(
                outdoorRound.roundSubTypes!![0],
                state.copy(
                        allRounds = roundsData,
                        selectedRoundId = outdoorRound.round.roundId,
                        selectedSubTypeId = outdoorRound.roundSubTypes!![0].subTypeId,
                ).selectedSubType,
        )
    }

    @Test
    fun testGetFurthestDistance() {
        val state = SelectRoundDialogState(allRounds = roundsData)

        assertEquals(
                null,
                state.getFurthestDistance(null),
        )

        assertEquals(
                outdoorRound.roundDistances!!.first(),
                state.copy(selectedRoundId = outdoorRound.round.roundId).getFurthestDistance(null),
        )
        assertEquals(
                outdoorRound.roundDistances!!.first(),
                state.copy(selectedRoundId = outdoorRound.round.roundId)
                        .getFurthestDistance(outdoorRound.roundSubTypes!!.first()),
        )
        assertEquals(
                outdoorRound.roundDistances!![2],
                state.copy(selectedRoundId = outdoorRound.round.roundId)
                        .getFurthestDistance(outdoorRound.roundSubTypes!![1]),
        )
    }

    @Test
    fun testFurthestSubtype() {
        val state = SelectRoundDialogState(allRounds = roundsData)

        assertEquals(
                null,
                state.furthestSubType,
        )
        assertEquals(
                outdoorRound.roundSubTypes!!.first(),
                state.copy(selectedRoundId = outdoorRound.round.roundId).furthestSubType,
        )
    }
}
