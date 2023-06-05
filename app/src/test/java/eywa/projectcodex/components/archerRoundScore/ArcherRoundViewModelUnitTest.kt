package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockSavedStateHandle
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ArcherRoundViewModelUnitTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val db = MockScoresRoomDatabase()

    private val helpShowcase: HelpShowcaseUseCase = mock { }
    private val datastore = MockDatastore()

    private fun getSut(
            startingScreen: ArcherRoundScreen = ArcherRoundScreen.INPUT_END,
            datastoreUse2023System: Boolean = true,
            archerRoundId: Int? = 1,
    ): ArcherRoundViewModel {
        datastore.values = mapOf(DatastoreKey.Use2023HandicapSystem to datastoreUse2023System)
        val savedState = MockSavedStateHandle().apply {
            values["screen"] = startingScreen.toString()
            archerRoundId?.let { values["archerRoundId"] = it }
        }.mock
        return ArcherRoundViewModel(db.mock, helpShowcase, datastore.mock, savedState)
    }

    @Test
    fun testInitialisation_RoundNotFound() = runTest {
        val sut = getSut()

        Assert.assertEquals(
                ArcherRoundState.Loading(ArcherRoundScreen.INPUT_END),
                sut.state.value,
        )

        advanceUntilIdle()
        Assert.assertEquals(
                ArcherRoundState.InvalidArcherRoundError(),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_NoRoundSet() = runTest {
        val sut = getSut(archerRoundId = null)

        Assert.assertEquals(
                ArcherRoundState.InvalidArcherRoundError(),
                sut.state.value,
        )
        advanceUntilIdle()
        Assert.assertEquals(
                ArcherRoundState.InvalidArcherRoundError(),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_ArcherRoundsAnd2023System() = runTest {
        fun create(arrowCount: Int) =
                DatabaseFullArcherRoundInfo(
                        archerRound = ArcherRound(1, Calendar.getInstance().time, 1),
                        arrows = List(arrowCount) { ArrowValue(1, it + 1, 7, false) },
                )

        val archerRoundInitial = create(0)
        val archerRoundSecond = create(12)
        db.archerRoundDao.fullArcherRounds = listOf(archerRoundInitial)
        db.archerRoundDao.secondFullArcherRounds = listOf(archerRoundSecond)
        datastore.valuesDelayed = mapOf(DatastoreKey.Use2023HandicapSystem to false)
        val sut = getSut()

        Assert.assertEquals(
                ArcherRoundState.Loading(ArcherRoundScreen.INPUT_END),
                sut.state.value,
        )
        advanceTimeBy(1)
        Assert.assertEquals(
                ArcherRoundState.Loaded(
                        currentScreen = ArcherRoundScreen.INPUT_END,
                        fullArcherRoundInfo = FullArcherRoundInfo(archerRoundInitial, true),
                ),
                sut.state.value,
        )

        advanceUntilIdle()
        Assert.assertEquals(
                ArcherRoundState.Loaded(
                        currentScreen = ArcherRoundScreen.INPUT_END,
                        fullArcherRoundInfo = FullArcherRoundInfo(archerRoundSecond, false),
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_RoundCompleted() = runTest {
        fun create(arrowCount: Int) =
                DatabaseFullArcherRoundInfo(
                        archerRound = ArcherRound(1, Calendar.getInstance().time, 1, roundId = 1),
                        arrows = List(arrowCount) { ArrowValue(1, it + 1, 7, false) },
                        round = Round(1, "", "", true, true, listOf()),
                        roundArrowCounts = listOf(RoundArrowCount(1, 1, 122f, 36)),
                        allRoundSubTypes = listOf(),
                        allRoundDistances = listOf(RoundDistance(1, 1, 1, 50)),
                )

        val archerRoundInitial = create(0)
        val archerRoundSecond = create(36)
        db.archerRoundDao.fullArcherRounds = listOf(archerRoundInitial)
        db.archerRoundDao.secondFullArcherRounds = listOf(archerRoundSecond)
        val sut = getSut()

        advanceTimeBy(1)
        Assert.assertEquals(
                ArcherRoundState.Loaded(
                        currentScreen = ArcherRoundScreen.INPUT_END,
                        fullArcherRoundInfo = FullArcherRoundInfo(archerRoundInitial, true),
                ),
                sut.state.value,
        )

        advanceUntilIdle()
        Assert.assertEquals(
                ArcherRoundState.Loaded(
                        currentScreen = ArcherRoundScreen.INPUT_END,
                        fullArcherRoundInfo = FullArcherRoundInfo(archerRoundSecond, true),
                        displayRoundCompletedDialog = true,
                ),
                sut.state.value,
        )
    }

    // TODO_CURRENT
}
