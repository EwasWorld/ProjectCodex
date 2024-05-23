package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.previewHelpers.asDatabaseFullShootInfo
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.*
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.datastore.get
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.*

typealias SimpleState = Pair<Int?, FullShootInfo?>
typealias SimpleLoaded = ShootDetailsResponse.Loaded<SimpleState>
typealias SimpleError = ShootDetailsResponse.Error<SimpleState>

@OptIn(ExperimentalCoroutinesApi::class)
class ShootDetailsRepoUnitTest {
    private val db = MockScoresRoomDatabase()
    private val datastore = MockDatastore()
    private val helpShowcase = mock<HelpShowcaseUseCase> { }

    private val jobs = mutableListOf<Job>()

    private val shootInfo = createShootInfo(1)
    private val initialState = ShootDetailsState(
            shootId = 1,
            fullShootInfo = shootInfo,
            use2023System = DatastoreKey.Use2023HandicapSystem.defaultValue,
            useBetaFeatures = DatastoreKey.UseBetaFeatures.defaultValue,
            archerHandicaps = listOf(),
            bow = DatabaseBowPreviewHelper.default,
            archerInfo = DatabaseArcherPreviewHelper.default,
            wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
    )

    private fun createShootInfo(id: Int) = ShootPreviewHelperDsl.create {
        shoot = shoot.copy(shootId = id)
        addIdenticalArrows(1, 10)
    }

    private fun TestScope.getSut(
            shoots: FullShootInfo? = shootInfo
    ): ShootDetailsRepo {
        shoots?.let { db.shootDao.fullShoots = listOf(it.asDatabaseFullShootInfo()) }
        val sut = ShootDetailsRepo(db.mock, datastore.mock, helpShowcase)

        sut.connect { jobs.add(launch { it() }) }
        advanceTimeBy(1)

        return sut
    }

    private fun teardown() {
        jobs.forEach { it.cancel() }
    }

    @Test
    fun testConnectAndGetState() = runTest {
        val loadingState = ShootDetailsResponse.Loading as ShootDetailsResponse<SimpleState>
        val shootInfo = createShootInfo(1)
        val shootInfo2 = createShootInfo(2)

        /*
         * Initial
         */
        val sut = getSut(shootInfo)
        verify(datastore.mock).get(DatastoreKey.Use2023HandicapSystem, DatastoreKey.UseBetaFeatures)
        verify(db.mock, never()).shootsRepo()

        fun startCollectingStateForId(shootId: Int, collection: MutableList<ShootDetailsResponse<SimpleState>>) =
                jobs.add(
                        launch {
                            sut.getState(shootId) { it.shootId to it.fullShootInfo }.collect { collection.add(it) }
                        }
                )

        fun FullShootInfo.asLoadedState() =
                SimpleLoaded(
                        data = shoot.shootId to this,
                        shootId = shoot.shootId,
                        navBarClicked = null,
                        isCounting = this.arrowCounter != null,
                )

        /*
         * getState for id 1
         */
        val id1CollectedStates = mutableListOf<ShootDetailsResponse<SimpleState>>()
        startCollectingStateForId(1, id1CollectedStates)
        advanceTimeBy(1)
        verify(db.shootDao.mockRepo).getFullShootInfo(1)
        assertEquals(
                listOf(loadingState, shootInfo.asLoadedState()),
                id1CollectedStates.toList(),
        )

        /*
         * getState for id 2
         */
        id1CollectedStates.clear()
        val id2CollectedStates = mutableListOf<ShootDetailsResponse<SimpleState>>()
        startCollectingStateForId(2, id2CollectedStates)
        db.shootDao.fullShoots = listOf(shootInfo2.asDatabaseFullShootInfo())
        db.shootDao.secondFullShoots = listOf()
        advanceTimeBy(1)
        verify(db.shootDao.mockRepo).getFullShootInfo(2)
        Assert.assertTrue(id1CollectedStates.all { it == loadingState })
        assertEquals(
                listOf(loadingState, shootInfo2.asLoadedState()),
                id2CollectedStates.toList(),
        )

        /*
         * db didn't find state
         */
        id1CollectedStates.clear()
        id2CollectedStates.clear()
        advanceUntilIdle()
        assertEquals(listOf(SimpleError()), id1CollectedStates.toList())
        assertEquals(listOf(SimpleError()), id2CollectedStates.toList())

        teardown()
    }

    private fun TestScope.collectLatestState(
            sut: ShootDetailsRepo,
            collector: (ShootDetailsResponse<ShootDetailsState>) -> Unit
    ) {
        jobs.add(
                launch {
                    sut.getState(shootInfo.shoot.shootId) { it }.collect(collector)
                }
        )
    }

    @Test
    fun testData() = runTest {
        val shootInfo = ShootPreviewHelperDsl.create {
            round = RoundPreviewHelper.yorkRoundData
            addIdenticalArrows(72, 10)
        }
        val highest = listOf(
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.yorkRoundData
                    completeRoundWithFinalScore(800)
                },
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.yorkRoundData
                    completeRoundWithFinalScore(700)
                },
        )
        val recent = listOf(
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.yorkRoundData
                    completeRoundWithFinalScore(600)
                },
                ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.yorkRoundData
                    completeRoundWithFinalScore(500)
                },
        )
        val sightMark = SightMarksPreviewHelper.sightMarks.take(1).map { SightMark(it) }

        db.rounds.fullRoundsInfo = listOf(RoundPreviewHelper.yorkRoundData)
        db.shootDao.highestScoreForRound = highest
        db.shootDao.mostRecentForRound = recent
        db.archerRepo.handicaps = ArcherHandicapsPreviewHelper.handicaps.take(1)
        db.archerRepo.defaultArcher =
                DatabaseArcherPreviewHelper.default.copy(isGent = false, age = ClassificationAge.OVER_50)
        db.bow.defaultBow = DatabaseBowPreviewHelper.default.copy(type = ClassificationBow.COMPOUND)
        db.sightMarksDao.sightMarks = sightMark
        datastore.values = mapOf(
                DatastoreKey.Use2023HandicapSystem to false,
                DatastoreKey.UseBetaFeatures to true,
        )

        val sut = getSut(shootInfo)
        var latestState: ShootDetailsState? = null
        collectLatestState(sut) {
            latestState = it.getData()
        }
        advanceUntilIdle()
        assertEquals(
                ShootDetailsState(
                        shootId = 1,
                        useBetaFeatures = true,
                        use2023System = false,
                        fullShootInfo = shootInfo.copy(use2023HandicapSystem = false),
                        archerHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        archerInfo = db.archerRepo.defaultArcher,
                        bow = db.bow.defaultBow,
                        wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
                        classification = null,
                        roundPbs = highest.map { it.asShort() },
                        pastRoundRecords = recent.map { it.asShort() },
                        sightMark = sightMark.first(),
                ),
                latestState
        )

        teardown()
    }

    private fun FullShootInfo.asShort() = DatabaseShootShortRecord(
            shootId = shoot.shootId,
            dateShot = shoot.dateShot,
            score = score,
            isComplete = isRoundComplete,
    )

    @Test
    fun testHelpShowcaseAction() = runTest {
        val sut = getSut()
        var latestState: ShootDetailsState? = null
        collectLatestState(sut) {
            latestState = it.getData()
        }
        advanceUntilIdle()
        assertEquals(initialState, latestState)

        sut.handle(HelpShowcaseAction(HelpShowcaseIntent.Clear), CodexNavRoute.SHOOT_DETAILS_INSERT_END)
        advanceUntilIdle()
        verify(helpShowcase).handle(HelpShowcaseIntent.Clear, CodexNavRoute.SHOOT_DETAILS_INSERT_END::class)
        assertEquals(initialState, latestState)

        sut.handle(HelpShowcaseAction(HelpShowcaseIntent.Clear), CodexNavRoute.MAIN_MENU)
        advanceUntilIdle()
        verify(helpShowcase).handle(HelpShowcaseIntent.Clear, CodexNavRoute.MAIN_MENU::class)
        assertEquals(initialState, latestState)

        teardown()
    }

    @Test
    fun testReturnToMenu() = runTest {
        val sut = getSut(null)
        var latestResponse: ShootDetailsResponse<ShootDetailsState>? = null
        collectLatestState(sut) {
            latestResponse = it
        }
        advanceUntilIdle()
        val initialState = ShootDetailsResponse.Error<ShootDetailsState>()
        assertEquals(initialState, latestResponse)

        sut.handle(ReturnToMenuClicked, CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(initialState.copy(mainMenuClicked = true), latestResponse)

        sut.handle(ReturnToMenuHandled, CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(initialState, latestResponse)

        teardown()
    }

    @Test
    fun testNavBarClicked() = runTest {
        val sut = getSut()
        var latestResponse: ShootDetailsResponse<ShootDetailsState>? = null
        collectLatestState(sut) {
            latestResponse = it
        }
        val initialResponse = ShootDetailsResponse.Loaded(initialState, 1, null, false)
        advanceUntilIdle()
        assertEquals(initialResponse, latestResponse)

        sut.handle(NavBarClicked(CodexNavRoute.SHOOT_DETAILS_INSERT_END), CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(
                initialResponse.copy(
                        navBarClicked = CodexNavRoute.SHOOT_DETAILS_INSERT_END,
                        data = initialResponse.data.copy(navBarClickedItem = CodexNavRoute.SHOOT_DETAILS_INSERT_END),
                ),
                latestResponse,
        )

        sut.handle(NavBarClickHandled(CodexNavRoute.SHOOT_DETAILS_INSERT_END), CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(initialResponse, latestResponse)

        teardown()
    }

    @Test
    fun testSelectScorePadEnd() = runTest {
        val sut = getSut()
        var latestState: ShootDetailsState? = null
        collectLatestState(sut) {
            latestState = it.getData()
        }
        advanceUntilIdle()
        assertEquals(initialState, latestState)

        sut.handle(SelectScorePadEnd(1), CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(initialState.copy(scorePadSelectedEnd = 1), latestState)

        teardown()
    }

    @Test
    fun testSetAddEndEndSize() = runTest {
        val sut = getSut()
        var latestState: ShootDetailsState? = null
        collectLatestState(sut) {
            latestState = it.getData()
        }
        advanceUntilIdle()
        assertEquals(initialState, latestState)

        sut.handle(SetAddEndEndSize(3), CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(initialState.copy(addEndSize = 3), latestState)

        teardown()
    }

    @Test
    fun testSetScorePadEndSize() = runTest {
        val sut = getSut()
        var latestState: ShootDetailsState? = null
        collectLatestState(sut) {
            latestState = it.getData()
        }
        advanceUntilIdle()
        assertEquals(initialState, latestState)

        sut.handle(SetScorePadEndSize(3), CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(initialState.copy(scorePadEndSize = 3), latestState)

        teardown()
    }

    @Test
    fun testSetInputtedArrows() = runTest {
        val sut = getSut()
        var latestState: ShootDetailsState? = null
        collectLatestState(sut) {
            latestState = it.getData()
        }
        advanceUntilIdle()
        assertEquals(initialState, latestState)

        val arrowList = listOf(Arrow(1), Arrow(3))
        sut.handle(SetInputtedArrows(arrowList), CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(initialState.copy(addEndArrows = arrowList), latestState)

        sut.handle(SetInputtedArrows(emptyList()), CodexNavRoute.ABOUT)
        advanceUntilIdle()
        assertEquals(initialState.copy(addEndArrows = emptyList()), latestState)

        teardown()
    }
}
