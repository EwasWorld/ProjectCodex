package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.*
import eywa.projectcodex.components.shootDetails.diShootComponent.Shoot
import eywa.projectcodex.components.shootDetails.diShootComponent.ShootComponentManager
import eywa.projectcodex.components.shootDetails.diShootComponent.ShootScoped
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archer.DEFAULT_ARCHER_ID
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey.*
import eywa.projectcodex.datastore.get
import eywa.projectcodex.datastore.retrieve
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.SightMark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private typealias DbShortShoots = List<DatabaseShootShortRecord>

class ShootDetailsError : Exception()

/**
 * Common repo for data needed on all shootDetails screens.
 * Used to minimise loading screens as users will flick between these screens a lot.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@ShootScoped
class ShootDetailsRepo @Inject constructor(
        val shootId: Int,
        @Shoot shootScope: CoroutineScope,
        private val shootComponentManager: ShootComponentManager,
        val db: ScoresRoomDatabase,
        private val datastore: CodexDatastore,
        private val helpShowcase: HelpShowcaseUseCase,
) {
    private val state: MutableStateFlow<ShootDetailsState> = MutableStateFlow(ShootDetailsState(shootId))

    init {
        shootScope.launch {
            db.archerRepo().getLatestHandicaps(DEFAULT_ARCHER_ID)
                    .combine(db.archerRepo().defaultArcher) { a, b -> a to b }
                    .collectLatest { (handicaps, archerInfo) ->
                        state.update { it.copy(archerHandicaps = handicaps, archerInfo = archerInfo) }
                    }
        }
        shootScope.launch {
            db.bowRepo().defaultBow
                    .distinctUntilChanged()
                    .collectLatest { bow -> state.update { it.copy(bow = bow) } }
        }
        shootScope.launch {
            db.roundsRepo().wa1440FullRoundInfo
                    .distinctUntilChanged()
                    .collectLatest { info -> state.update { it.copy(wa1440FullRoundInfo = info) } }
        }
        shootScope.launch {
            datastore.get(
                    Use2023HandicapSystem,
                    UseBetaFeatures,
                    UseSimpleStatsView,
            ).collectLatest { result ->
                state.update {
                    it.copy(
                            useBetaFeatures = result.retrieve(UseBetaFeatures),
                            use2023System = result.retrieve(Use2023HandicapSystem),
                            useSimpleView = result.retrieve(UseSimpleStatsView),
                    )
                }
            }
        }
        shootScope.launch {
            db.shootsRepo().getFullShootInfo(shootId).collectLatest { dbInfo ->
                if (dbInfo == null) {
                    state.update { ShootDetailsState(shootId = shootId, isError = true) }
                    return@collectLatest
                }

                state.update {
                    val system = it.use2023System ?: Use2023HandicapSystem.defaultValue
                    val info = FullShootInfo(dbInfo, system)
                    it.copy(fullShootInfo = info)
                }
            }
        }
        shootScope.launch {
            state
                    .map { it.fullShootInfo?.shootRound?.let { sr -> sr.roundId to sr.roundSubTypeId } }
                    .distinctUntilChanged()
                    .flatMapLatest {
                        if (it == null) {
                            return@flatMapLatest flow<Pair<DbShortShoots?, DbShortShoots?>> { emit(null to null) }
                        }
                        val (roundId, subTypeId) = it
                        db.shootsRepo()
                                .getMostRecentShootsForRound(10, roundId, subTypeId ?: 1)
                                .combine(
                                        db.shootsRepo().getHighestScoreShootsForRound(10, roundId, subTypeId ?: 1),
                                ) { latest, pbs -> latest to pbs }
                    }.collect { (latest, pbs) ->
                        state.update { it.copy(roundPbs = pbs, pastRoundRecords = latest) }
                    }
        }
        shootScope.launch {
            state
                    .map {
                        val distance = it.fullShootInfo?.remainingArrowsAtDistances?.firstOrNull()?.second
                                ?: it.fullShootInfo?.fullRoundInfo?.roundDistances?.minOfOrNull { d -> d.distance }
                        distance to it.fullShootInfo?.round?.isMetric
                    }
                    .distinctUntilChanged()
                    .flatMapLatest { (distance, isMetric) ->
                        if (distance == null || isMetric == null) return@flatMapLatest emptyFlow()
                        db.sightMarkRepo().getSightMarkForDistance(distance, isMetric)
                    }.collect { sightMark ->
                        state.update { it.copy(sightMark = sightMark?.let { sm -> SightMark(sm) }) }
                    }
        }
    }

    fun handle(
            action: ShootDetailsIntent,
            screen: CodexNavRoute,
            scope: CoroutineScope? = null,
    ) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            ReturnToMenuClicked -> {
                shootComponentManager.exitShootDetails(shootId)
                state.update { it.copy(mainMenuClicked = true) }
            }

            ReturnToMenuHandled -> state.update { it.copy(mainMenuClicked = false) }
            ToggleSimpleView -> scope?.launch { datastore.toggle(UseSimpleStatsView) }
            is SelectScorePadEnd -> state.update { it.copy(scorePadSelectedEnd = action.endNumber) }
            is NavBarClicked -> state.update { it.copy(navBarClickedItem = action.screen) }
            is NavBarClickHandled ->
                state.update {
                    if (it.navBarClickedItem != action.screen) it
                    else it.copy(navBarClickedItem = null)
                }

            is SetInputtedArrows -> state.update { it.copy(addEndArrows = action.arrows) }
            is SetAddEndEndSize -> state.update { it.copy(addEndArrows = emptyList(), addEndSize = action.size) }
            is SetScorePadEndSize -> state.update { it.copy(scorePadEndSize = action.size) }
            BackClicked -> {
                shootComponentManager.exitShootDetails(shootId)
                state.update { it.copy(backClicked = true) }
            }

            BackHandled -> state.update { it.copy(backClicked = false) }
        }
    }

    fun <T : Any> getState(
            converter: (ShootDetailsState) -> T,
    ): Flow<ShootDetailsResponse<T>> {
        return state.map { combineStates(it, null) { s, _ -> converter(s) } }
    }

    fun <T : Any, E> getState(
            extraFlow: StateFlow<E>,
            converter: (ShootDetailsState, E) -> T,
    ): Flow<ShootDetailsResponse<T>> {
        return state.combine(extraFlow) { main, extra ->
            combineStates(main, extra) { s, e -> converter(s, e!!) }
        }
    }

    fun <T : Any, E> getStateNullableExtra(
            extraFlow: StateFlow<E>,
            converter: (ShootDetailsState, E?) -> T,
    ): Flow<ShootDetailsResponse<T>> {
        return state.combine(extraFlow) { main, extra ->
            combineStates(main, extra) { s, e -> converter(s, e) }
        }
    }


    private fun <T : Any, E> combineStates(
            state: ShootDetailsState,
            extra: E?,
            converter: (ShootDetailsState, E?) -> T
    ): ShootDetailsResponse<T> =
            when {
                state.isError -> ShootDetailsResponse.Error(state.mainMenuClicked)
                state.fullShootInfo == null -> ShootDetailsResponse.Loading

                else -> {
                    try {
                        ShootDetailsResponse.Loaded(
                                data = converter(state, extra),
                                shootId = state.shootId,
                                navBarClicked = state.navBarClickedItem,
                                backClicked = state.backClicked,
                                isCounting = state.fullShootInfo.arrowCounter != null,
                        )
                    }
                    catch (e: ShootDetailsError) {
                        ShootDetailsResponse.Error(state.mainMenuClicked)
                    }
                }
            }
}
