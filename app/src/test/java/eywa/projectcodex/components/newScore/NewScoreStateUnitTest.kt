package eywa.projectcodex.components.newScore

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import org.junit.Assert.assertEquals
import org.junit.Test

class NewScoreStateUnitTest {
    private val paramProvider = NewScoreStatePreviewProvider()

    @Test
    fun testIsEditing() {
        with(paramProvider) {
            assertEquals(
                    true,
                    NewScoreState(roundBeingEdited = editingArcherRound).isEditing
            )
            assertEquals(
                    false,
                    NewScoreState(roundBeingEdited = null).isEditing
            )
        }
    }

    @Test
    fun testRoundInfo() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    null,
                    state.selectedRoundInfo,
            )
            assertEquals(
                    outdoorImperialRoundData,
                    state.copy(selectedRound = outdoorImperialRoundData.round).selectedRoundInfo,
            )
        }
    }

    @Test
    fun testRoundSubTypeDistances() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    null,
                    state.roundSubtypeDistances,
            )
            assertEquals(
                    outdoorImperialRoundData.roundDistances!!.subList(0, 2),
                    state.copy(
                            selectedRound = outdoorImperialRoundData.round,
                            selectedSubtype = outdoorImperialRoundData.roundSubTypes!![0]
                    ).roundSubtypeDistances,
            )
        }
    }

    @Test
    fun testDistanceUnit() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    null,
                    state.selectedRound,
            )
            assertEquals(
                    R.string.units_meters_short,
                    state.copy(selectedRound = indoorMetricRoundData.round).distanceUnitStringRes,
            )
            assertEquals(
                    R.string.units_yards_short,
                    state.copy(selectedRound = outdoorImperialRoundData.round).distanceUnitStringRes,
            )
        }
    }

    @Test
    fun testDisplayedSubtype() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    null,
                    state.displayedSubtype,
            )
            // Only one subtype
            assertEquals(
                    null,
                    state.copy(
                            selectedRound = singleSubtypeRoundData.round,
                            selectedSubtype = singleSubtypeRoundData.roundSubTypes!![0]
                    ).displayedSubtype,
            )
            assertEquals(
                    outdoorImperialRoundData.roundSubTypes!![0],
                    state.copy(
                            selectedRound = outdoorImperialRoundData.round,
                            selectedSubtype = outdoorImperialRoundData.roundSubTypes!![0],
                    ).displayedSubtype,
            )
        }
    }

    @Test
    fun testDisplayedRound() {
        with(paramProvider) {
            assertEquals(
                    ResOrActual.fromRes<String>(R.string.create_round__no_rounds_found),
                    NewScoreState().displayedRound,
            )
            assertEquals(
                    ResOrActual.fromRes<String>(R.string.create_round__no_round),
                    NewScoreState(roundsData = roundsData).displayedRound,
            )
            assertEquals(
                    ResOrActual.fromActual(outdoorImperialRoundData.round.displayName),
                    NewScoreState(
                            roundsData = roundsData,
                            selectedRound = outdoorImperialRoundData.round,
                    ).displayedRound,
            )
        }
    }

    @Test
    fun testTotalArrowsInSelectedRound() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    null,
                    state.totalArrowsInSelectedRound,
            )
            assertEquals(
                    36 + 24,
                    state.copy(selectedRound = outdoorImperialRoundData.round).totalArrowsInSelectedRound,
            )
        }
    }

    @Test
    fun testTooManyArrowsWarningShown() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)

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
                            selectedRound = outdoorImperialRoundData.round,
                    ).tooManyArrowsWarningShown,
            )
            assertEquals(
                    true,
                    state.copy(
                            roundBeingEditedArrowsShot = 1000,
                            selectedRound = outdoorImperialRoundData.round,
                    ).tooManyArrowsWarningShown,
            )
        }
    }
}
