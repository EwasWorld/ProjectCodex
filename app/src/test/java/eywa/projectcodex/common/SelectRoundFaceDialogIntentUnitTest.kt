package eywa.projectcodex.common

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent.*
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogState
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectRoundFaceDialogIntentUnitTest {
    @Test
    fun testOpen() {
        assertEquals(
                SelectRoundFaceDialogState(isShown = true),
                Open.handle(SelectRoundFaceDialogState()),
        )
        assertEquals(
                SelectRoundFaceDialogState(isShown = true),
                Open.handle(SelectRoundFaceDialogState(isShown = true)),
        )
    }

    @Test
    fun testClose() {
        assertEquals(
                SelectRoundFaceDialogState(isShown = false),
                Close.handle(SelectRoundFaceDialogState(isShown = true)),
        )
        assertEquals(
                SelectRoundFaceDialogState(isShown = false),
                Close.handle(SelectRoundFaceDialogState()),
        )
    }

    @Test
    fun testFaceTypeHelpClicked() {
        // TODO
    }

    @Test
    fun testToggleAllDifferentAllSame() {
        // Valid
        assertEquals(
                SelectRoundFaceDialogState(isShown = true, isSingleMode = false),
                ToggleSingleMode.handle(SelectRoundFaceDialogState(isShown = true, isSingleMode = true)),
        )
        assertEquals(
                SelectRoundFaceDialogState(isShown = true, isSingleMode = true),
                ToggleSingleMode.handle(SelectRoundFaceDialogState(isShown = true, isSingleMode = false)),
        )

        // Invalid
        assertEquals(
                SelectRoundFaceDialogState(isSingleMode = true),
                ToggleSingleMode.handle(SelectRoundFaceDialogState(isSingleMode = true)),
        )
        assertEquals(
                SelectRoundFaceDialogState(isSingleMode = false),
                ToggleSingleMode.handle(SelectRoundFaceDialogState(isSingleMode = false)),
        )
    }

    @Test
    fun testCloseDropdown() {
        assertEquals(
                SelectRoundFaceDialogState(),
                CloseDropdown.handle(SelectRoundFaceDialogState()),
        )
        assertEquals(
                SelectRoundFaceDialogState(),
                CloseDropdown.handle(SelectRoundFaceDialogState(dropdownExpandedFor = 1)),
        )
    }

    @Test
    fun testOpenDropdown() {
        val state = SelectRoundFaceDialogState(
                round = RoundPreviewHelper.indoorMetricRoundData.round,
                distances = listOf(20, 10),
                isSingleMode = false,
                isShown = true,
        )

        // Valid
        assertEquals(state.copy(dropdownExpandedFor = 0), OpenDropdown(0).handle(state))
        assertEquals(state.copy(dropdownExpandedFor = 1), OpenDropdown(1).handle(state))

        // Invalid
        assertEquals(state, OpenDropdown(3).handle(state))

        state.copy(distances = listOf(20)).let { testState ->
            assertEquals(testState, OpenDropdown(1).handle(testState))
        }
        state.copy(isSingleMode = true).let { testState ->
            assertEquals(testState, OpenDropdown(1).handle(testState))
        }
        state.copy(isShown = false).let { testState ->
            assertEquals(testState, OpenDropdown(1).handle(testState))
        }
    }

    @Test
    fun testDropdownItemClicked() {
        val state = SelectRoundFaceDialogState(
                round = RoundPreviewHelper.indoorMetricRoundData.round,
                distances = listOf(20, 10),
                isSingleMode = false,
                dropdownExpandedFor = 0,
                isShown = true,
        )

        // Valid
        assertEquals(
                state.copy(dropdownExpandedFor = null, selectedFaces = listOf(RoundFace.TRIPLE, RoundFace.FULL)),
                DropdownItemClicked(RoundFace.TRIPLE).handle(state),
        )
        assertEquals(
                state.copy(dropdownExpandedFor = null, selectedFaces = listOf(RoundFace.FULL, RoundFace.TRIPLE)),
                DropdownItemClicked(RoundFace.TRIPLE).handle(state.copy(dropdownExpandedFor = 1)),
        )
        assertEquals(
                state.copy(dropdownExpandedFor = null, selectedFaces = listOf(RoundFace.TRIPLE, RoundFace.HALF)),
                DropdownItemClicked(RoundFace.TRIPLE).handle(
                        state.copy(selectedFaces = listOf(RoundFace.FITA_SIX, RoundFace.HALF))
                ),
        )

        // Invalid
        state.copy(isShown = false).let { testState ->
            assertEquals(
                    testState.copy(dropdownExpandedFor = null),
                    DropdownItemClicked(RoundFace.TRIPLE).handle(testState),
            )
        }
        state.copy(dropdownExpandedFor = 3).let { testState ->
            assertEquals(
                    testState.copy(dropdownExpandedFor = null),
                    DropdownItemClicked(RoundFace.TRIPLE).handle(testState),
            )
        }
        state.copy(dropdownExpandedFor = null).let { testState ->
            assertEquals(
                    testState.copy(dropdownExpandedFor = null),
                    DropdownItemClicked(RoundFace.TRIPLE).handle(testState),
            )
        }
        state.copy(distances = listOf(20)).let { testState ->
            assertEquals(
                    testState.copy(dropdownExpandedFor = null),
                    DropdownItemClicked(RoundFace.TRIPLE).handle(testState),
            )
        }
        state.copy(isSingleMode = true).let { testState ->
            assertEquals(
                    testState.copy(dropdownExpandedFor = null),
                    DropdownItemClicked(RoundFace.TRIPLE).handle(testState),
            )
        }
    }

    @Test
    fun testSingleFaceClicked() {
        val state = SelectRoundFaceDialogState(
                isShown = true,
                round = RoundPreviewHelper.indoorMetricRoundData.round,
                distances = listOf(20, 10),
        )

        // Valid
        assertEquals(
                state.copy(isShown = false, selectedFaces = listOf(RoundFace.TRIPLE)),
                SingleFaceClicked(RoundFace.TRIPLE).handle(state),
        )
        assertEquals(
                state.copy(isShown = false, selectedFaces = listOf(RoundFace.TRIPLE)),
                SingleFaceClicked(RoundFace.TRIPLE).handle(
                        state.copy(selectedFaces = listOf(RoundFace.HALF))
                ),
        )
        assertEquals(
                state.copy(isShown = false, selectedFaces = listOf(RoundFace.TRIPLE)),
                SingleFaceClicked(RoundFace.TRIPLE).handle(
                        state.copy(selectedFaces = listOf(RoundFace.FITA_SIX, RoundFace.HALF))
                ),
        )
        state.copy(round = null, distances = null).let { testState ->
            assertEquals(
                    testState.copy(isShown = false, selectedFaces = listOf(RoundFace.TRIPLE)),
                    SingleFaceClicked(RoundFace.TRIPLE).handle(testState),
            )
        }

        // Invalid
        state.copy(isShown = false).let { testState ->
            assertEquals(testState, SingleFaceClicked(RoundFace.TRIPLE).handle(testState))
        }
        state.copy(isSingleMode = false).let { testState ->
            assertEquals(testState, SingleFaceClicked(RoundFace.TRIPLE).handle(testState))
        }
    }

    @Test
    fun testSetRound() {
        val round = RoundPreviewHelper.indoorMetricRoundData.round
        val distances = listOf(20, 10)
        val roundDistances = distances.mapIndexed { index, distance ->
            RoundDistance(
                    roundId = round.roundId,
                    distanceNumber = index,
                    subTypeId = 1,
                    distance = distance,
            )
        }
        val faces = listOf(RoundFace.TRIPLE, RoundFace.HALF)

        // Valid
        assertEquals(
                SelectRoundFaceDialogState(round = round, distances = distances),
                SetRound(round = round, distances = roundDistances).handle(SelectRoundFaceDialogState()),
        )
        assertEquals(
                SelectRoundFaceDialogState(round = round, distances = distances, selectedFaces = faces.take(1)),
                SetRound(round = round, distances = roundDistances)
                        .handle(
                                SelectRoundFaceDialogState(
                                        round = RoundPreviewHelper.outdoorImperialRoundData.round,
                                        distances = listOf(50, 40),
                                        selectedFaces = faces,
                                )
                        ),
        )

        // Invalid
        assertEquals(
                SelectRoundFaceDialogState(),
                SetRound(
                        round = round,
                        distances = roundDistances.map { it.copy(roundId = round.roundId + 1) },
                ).handle(SelectRoundFaceDialogState())
        )
    }

    @Test
    fun testSetDistances() {
        val round = RoundPreviewHelper.indoorMetricRoundData.round
        val distances = listOf(20, 10)
        val roundDistances = distances.mapIndexed { index, distance ->
            RoundDistance(
                    roundId = round.roundId,
                    distanceNumber = index,
                    subTypeId = 1,
                    distance = distance,
            )
        }
        val state = SelectRoundFaceDialogState(round = round, distances = listOf(50, 40, 30))

        // Valid
        assertEquals(
                state.copy(distances = distances),
                SetDistances(distances = roundDistances).handle(state),
        )

        // Invalid
        assertEquals(
                SelectRoundFaceDialogState(),
                SetDistances(distances = roundDistances).handle(SelectRoundFaceDialogState()),
        )
        assertEquals(
                state,
                SetDistances(distances = roundDistances.map { it.copy(roundId = round.roundId + 1) }).handle(state)
        )
    }

    @Test
    fun testSetNoRound() {
        val state = SelectRoundFaceDialogState(
                round = RoundPreviewHelper.indoorMetricRoundData.round,
                distances = listOf(20, 10),
        )
        assertEquals(
                state.copy(round = null, distances = null),
                SetNoRound.handle(state),
        )
        assertEquals(
                state.copy(round = null, distances = null, selectedFaces = listOf(RoundFace.TRIPLE)),
                SetNoRound.handle(state.copy(selectedFaces = listOf(RoundFace.TRIPLE, RoundFace.HALF))),
        )
    }
}
