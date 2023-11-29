package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.*
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.datastore.get
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
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
            fullShootInfo = FullShootInfo(shootInfo, true),
            use2023System = DatastoreKey.Use2023HandicapSystem.defaultValue,
            useBetaFeatures = DatastoreKey.UseBetaFeatures.defaultValue,
    )

    private fun createShootInfo(id: Int) = DatabaseFullShootInfo(
            shoot = DatabaseShoot(id, Calendar.getInstance()),
            arrows = listOf(DatabaseArrowScore(id, 1, 10, false)),
    )

    private fun TestScope.getSut(
            shoots: List<DatabaseFullShootInfo>? = listOf(shootInfo)
    ): ShootDetailsRepo {
        shoots?.let { db.shootDao.fullShoots = it }
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
        db.shootDao.fullShoots = listOf(shootInfo)

        /*
         * Initial
         */
        val sut = getSut(null)
        verify(datastore.mock).get(DatastoreKey.Use2023HandicapSystem, DatastoreKey.UseBetaFeatures)
        verify(db.mock, never()).shootsRepo()

        fun startCollectingStateForId(shootId: Int, collection: MutableList<ShootDetailsResponse<SimpleState>>) =
                jobs.add(
                        launch {
                            sut.getState(shootId) { it.shootId to it.fullShootInfo }.collect { collection.add(it) }
                        }
                )

        fun DatabaseFullShootInfo.asLoadedState() =
                SimpleLoaded(
                        data = shoot.shootId to FullShootInfo(this, true),
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
        verify(db.shootDao.mock).getFullShootInfo(1)
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
        db.shootDao.fullShoots = listOf(shootInfo2)
        db.shootDao.secondFullShoots = listOf()
        advanceTimeBy(1)
        verify(db.shootDao.mock).getFullShootInfo(2)
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
        val sut = getSut(listOf())
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
