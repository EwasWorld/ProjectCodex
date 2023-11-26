package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addIdenticalArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import org.junit.Assert.assertEquals
import org.junit.Test

class NewScoreStateUnitTest {
    private val paramProvider = NewScoreStatePreviewProvider()

    @Test
    fun testIsEditing() {
        assertEquals(
                true,
                NewScoreState(roundBeingEdited = ShootPreviewHelper.newFullShootInfo()).isEditing
        )
        assertEquals(
                false,
                NewScoreState(roundBeingEdited = null).isEditing
        )
    }

    @Test
    fun testTotalArrowsInSelectedRound() {
        with(paramProvider) {
            val state = NewScoreState(selectRoundDialogState = SelectRoundDialogState(allRounds = roundsData))
            assertEquals(
                    null,
                    state.totalArrowsInSelectedRound,
            )
            assertEquals(
                    36 + 24,
                    state.copy(
                            selectRoundDialogState = SelectRoundDialogState(
                                    allRounds = roundsData,
                                    selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                            ),
                    ).totalArrowsInSelectedRound,
            )
        }
    }

    @Test
    fun testTooManyArrowsWarningShown() {
        with(paramProvider) {
            val state = NewScoreState(selectRoundDialogState = SelectRoundDialogState(allRounds = roundsData))

            assertEquals(
                    false,
                    state.tooManyArrowsWarningShown,
            )
            assertEquals(
                    false,
                    state.copy(
                            roundBeingEdited = ShootPreviewHelper.newFullShootInfo()
                                    .addRound(round)
                                    .addIdenticalArrows(1000, 1),
                    ).tooManyArrowsWarningShown,
            )
            assertEquals(
                    false,
                    state.copy(
                            roundBeingEdited = ShootPreviewHelper.newFullShootInfo()
                                    .addRound(round)
                                    .addIdenticalArrows(2, 1),
                            selectRoundDialogState = SelectRoundDialogState(
                                    allRounds = roundsData,
                                    selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                            ),
                    ).tooManyArrowsWarningShown,
            )
            assertEquals(
                    true,
                    state.copy(
                            roundBeingEdited = ShootPreviewHelper.newFullShootInfo()
                                    .addRound(round)
                                    .addIdenticalArrows(1000, 1),
                            selectRoundDialogState = SelectRoundDialogState(
                                    allRounds = roundsData,
                                    selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                            ),
                    ).tooManyArrowsWarningShown,
            )
        }
    }
}
