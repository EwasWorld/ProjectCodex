package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import org.junit.Assert.assertEquals
import org.junit.Test

class NewScoreStateUnitTest {
    private val paramProvider = NewScoreStatePreviewProvider()

    @Test
    fun testIsEditing() {
        assertEquals(
                true,
                NewScoreState(roundBeingEdited = ArcherRoundPreviewHelper.newFullArcherRoundInfo()).isEditing
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
                    state.copy(roundBeingEditedArrowsShot = 1000).tooManyArrowsWarningShown,
            )
            assertEquals(
                    false,
                    state.copy(
                            roundBeingEditedArrowsShot = 2,
                            selectRoundDialogState = SelectRoundDialogState(
                                    allRounds = roundsData,
                                    selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                            ),
                    ).tooManyArrowsWarningShown,
            )
            assertEquals(
                    true,
                    state.copy(
                            roundBeingEditedArrowsShot = 1000,
                            selectRoundDialogState = SelectRoundDialogState(
                                    allRounds = roundsData,
                                    selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                            ),
                    ).tooManyArrowsWarningShown,
            )
        }
    }
}
