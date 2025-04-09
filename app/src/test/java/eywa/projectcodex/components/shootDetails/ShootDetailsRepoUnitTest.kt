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
import eywa.projectcodex.components.shootDetails.diShootComponent.ShootComponentManager
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import eywa.projectcodex.datastore.DatastoreKey.Use2023HandicapSystem
import eywa.projectcodex.datastore.DatastoreKey.UseBetaFeatures
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.model.user.CodexUserPreviewHelper
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ShootDetailsRepoUnitTest {
    private val db = MockScoresRoomDatabase()
    private val datastore = MockDatastore()
    private val helpShowcase = mock<HelpShowcaseUseCase> { }

    private val context = SupervisorJob()
    private val jobs = mutableListOf<Job>()

    private val shootInfo = ShootPreviewHelperDsl.create {
        shoot = shoot.copy(shootId = 1)
        addIdenticalArrows(1, 10)
    }
    private val initialState = ShootDetailsState(
            shootId = 1,
            fullShootInfo = shootInfo,
            use2023System = Use2023HandicapSystem.defaultValue,
            useBetaFeatures = UseBetaFeatures.defaultValue,
            archerHandicaps = listOf(),
            bow = DatabaseBowPreviewHelper.default,
            archerInfo = DatabaseArcherPreviewHelper.default,
            wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
            wa18FullRoundInfo = RoundPreviewHelper.wa18RoundData,
            user = CodexUserPreviewHelper.allCapabilities,
    )

    private fun TestScope.getSut(
            shoots: FullShootInfo? = shootInfo
    ): ShootDetailsRepo {
        shoots?.let { db.shootRepo.fullShoots = listOf(it.asDatabaseFullShootInfo()) }
        val sut = ShootDetailsRepo(
                shootId = 1,
                shootScope = CoroutineScope(this.coroutineContext + context),
                shootComponentManager = mock<ShootComponentManager> { },
                db = db.mock,
                datastore = datastore.mock,
                helpShowcase = helpShowcase,
                user = CodexUserPreviewHelper.allCapabilities,
        )

        advanceTimeBy(1)

        return sut
    }

    private fun teardown() {
        jobs.forEach { it.cancel() }
        context.cancel()
    }

    private fun TestScope.collectLatestState(
            sut: ShootDetailsRepo,
            collector: (ShootDetailsResponse<ShootDetailsState>) -> Unit
    ) {
        jobs.add(launch { sut.getState { it }.collect(collector) })
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

        db.roundsRepo.fullRoundsInfo = listOf(RoundPreviewHelper.yorkRoundData)
        db.shootRepo.highestScoreForRound = highest
        db.shootRepo.mostRecentForRound = recent
        db.archerRepo.handicaps = ArcherHandicapsPreviewHelper.handicaps.take(1)
        db.archerRepo.defaultArcher =
                DatabaseArcherPreviewHelper.default.copy(isGent = false, age = ClassificationAge.OVER_50)
        db.bowRepo.defaultBow = DatabaseBowPreviewHelper.default.copy(type = ClassificationBow.COMPOUND)
        db.sightMarksRepo.sightMarks = sightMark
        datastore.values = mapOf(
                Use2023HandicapSystem to false,
                UseBetaFeatures to true,
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
                        bow = db.bowRepo.defaultBow,
                        wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
                        wa18FullRoundInfo = RoundPreviewHelper.wa18RoundData,
                        classification = null,
                        roundPbs = highest.map { it.asShort() },
                        pastRoundRecords = recent.map { it.asShort() },
                        sightMark = sightMark.first(),
                        user = CodexUserPreviewHelper.allCapabilities,
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
        val initialResponse = ShootDetailsResponse.Loaded(initialState, 1, null, false, false)
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
