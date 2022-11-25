package eywa.projectcodex.components.newScore

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
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
    fun testRoundArrowCounts() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    listOf<RoundArrowCount>(),
                    state.roundArrowCounts,
            )
            assertEquals(
                    outdoorImperialRoundData.arrowCounts,
                    state.copy(selectedRound = outdoorImperialRoundData.getOnlyRound()).roundArrowCounts,
            )
        }
    }

    @Test
    fun testRoundSubTypes() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    listOf<RoundSubType>(),
                    state.roundSubTypes,
            )
            assertEquals(
                    outdoorImperialRoundData.subTypes,
                    state.copy(selectedRound = outdoorImperialRoundData.getOnlyRound()).roundSubTypes,
            )
        }
    }

    @Test
    fun testRoundDistances() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    listOf<RoundDistance>(),
                    state.roundDistances,
            )
            assertEquals(
                    outdoorImperialRoundData.distances,
                    state.copy(selectedRound = outdoorImperialRoundData.getOnlyRound()).roundDistances,
            )
        }
    }

    @Test
    fun testRoundSubTypeDistances() {
        with(paramProvider) {
            val state = NewScoreState(roundsData = roundsData)
            assertEquals(
                    listOf<RoundDistance>(),
                    state.roundSubtypeDistances,
            )
            assertEquals(
                    outdoorImperialRoundData.distances!!.subList(0, 2),
                    state.copy(
                            selectedRound = outdoorImperialRoundData.getOnlyRound(),
                            selectedSubtype = outdoorImperialRoundData.subTypes!![0]
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
                    state.copy(selectedRound = indoorMetricRoundData.getOnlyRound()).distanceUnitStringRes,
            )
            assertEquals(
                    R.string.units_yards_short,
                    state.copy(selectedRound = outdoorImperialRoundData.getOnlyRound()).distanceUnitStringRes,
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
                            selectedRound = singleSubtypeRoundData.getOnlyRound(),
                            selectedSubtype = singleSubtypeRoundData.subTypes!![0]
                    ).displayedSubtype,
            )
            assertEquals(
                    outdoorImperialRoundData.subTypes!![0],
                    state.copy(
                            selectedRound = outdoorImperialRoundData.getOnlyRound(),
                            selectedSubtype = outdoorImperialRoundData.subTypes!![0],
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
                    ResOrActual.fromActual(outdoorImperialRoundData.getOnlyRound().displayName),
                    NewScoreState(
                            roundsData = roundsData,
                            selectedRound = outdoorImperialRoundData.getOnlyRound(),
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
                    state.copy(selectedRound = outdoorImperialRoundData.getOnlyRound()).totalArrowsInSelectedRound,
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
                            selectedRound = outdoorImperialRoundData.getOnlyRound(),
                    ).tooManyArrowsWarningShown,
            )
            assertEquals(
                    true,
                    state.copy(
                            roundBeingEditedArrowsShot = 1000,
                            selectedRound = outdoorImperialRoundData.getOnlyRound(),
                    ).tooManyArrowsWarningShown,
            )
        }
    }
}
