package eywa.projectcodex.components.shootDetails

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archer.DEFAULT_ARCHER_ID
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
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
import javax.inject.Singleton

private typealias DbShortShoots = List<DatabaseShootShortRecord>

/**
 * Common repo for data needed on all shootDetails screens.
 * Used to minimise loading screens as users will flick between these screens a lot.
 *
 * Usage: [connect] is called from MainActivity, then each view model uses [getState]
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ShootDetailsRepo(
        val db: ScoresRoomDatabase,
        private val datastore: CodexDatastore,
        private val helpShowcase: HelpShowcaseUseCase,
) {
    private val state: MutableStateFlow<ShootDetailsState> = MutableStateFlow(ShootDetailsState())

    fun handle(
            action: ShootDetailsIntent,
            screen: CodexNavRoute,
            scope: CoroutineScope? = null,
    ) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            ReturnToMenuClicked -> state.update { it.copy(mainMenuClicked = true) }
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
            ClearState -> setupState(null)
        }
    }

    fun connect(launch: (block: suspend () -> Unit) -> Unit) {
        launch {
            db.archerRepo().getLatestHandicaps(DEFAULT_ARCHER_ID)
                    .combine(db.archerRepo().defaultArcher) { a, b -> a to b }
                    .collectLatest { (handicaps, archerInfo) ->
                        state.update { it.copy(archerHandicaps = handicaps, archerInfo = archerInfo) }
                    }
        }
        launch {
            db.bowRepo().defaultBow
                    .distinctUntilChanged()
                    .collectLatest { bow -> state.update { it.copy(bow = bow) } }
        }
        launch {
            db.roundsRepo().wa1440FullRoundInfo
                    .distinctUntilChanged()
                    .collectLatest { info -> state.update { it.copy(wa1440FullRoundInfo = info) } }
        }
        launch {
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
        launch {
            state
                    .map { it.shootId }
                    .distinctUntilChanged()
                    .flatMapLatest { shootId ->
                        if (shootId == null) {
                            return@flatMapLatest flow<Pair<Int?, DatabaseFullShootInfo?>> { emit(null to null) }
                        }
                        db.shootsRepo().getFullShootInfo(shootId).map { shootId to it }
                    }
                    .collectLatest { (shootId, dbInfo) ->
                        if (shootId == null) return@collectLatest
                        if (dbInfo == null) {
                            state.update {
                                if (shootId != it.shootId) it
                                else ShootDetailsState(shootId = shootId, isError = true).preserveFixedInfo(it)
                            }
                            return@collectLatest
                        }

                        state.update {
                            if (it.shootId != shootId) return@update it
                            val system = it.use2023System ?: Use2023HandicapSystem.defaultValue
                            val info = FullShootInfo(dbInfo, system)
                            it.copy(fullShootInfo = info)
                        }
                    }
        }
        launch {
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
                                        db.shootsRepo().getHighestScoreShootsForRound(10, roundId, subTypeId ?: 1)
                                ) { latest, pbs -> latest to pbs }
                    }.collect { (latest, pbs) ->
                        state.update { it.copy(roundPbs = pbs, pastRoundRecords = latest) }
                    }
        }
        launch {
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

    fun <T : Any> getState(
            shootId: Int?,
            converter: (ShootDetailsState) -> T,
    ): Flow<ShootDetailsResponse<T>> {
        setupState(shootId)
        return state.map { combineStates(shootId, it, null) { s, _ -> converter(s) } }
    }

    fun <T : Any, E> getState(
            shootId: Int?,
            extraFlow: StateFlow<E>,
            converter: (ShootDetailsState, E) -> T,
    ): Flow<ShootDetailsResponse<T>> {
        setupState(shootId)
        return state.combine(extraFlow) { main, extra ->
            combineStates(shootId, main, extra) { s, e -> converter(s, e!!) }
        }
    }

    private fun setupState(shootId: Int?) {
        when {
            shootId == null -> state.update { ShootDetailsState(isError = true).preserveFixedInfo(it) }

            state.value.shootId != shootId ->
                state.update { ShootDetailsState(shootId = shootId).preserveFixedInfo(it) }
        }
    }

    private fun <T : Any, E> combineStates(
            shootId: Int?,
            state: ShootDetailsState,
            extra: E?,
            converter: (ShootDetailsState, E?) -> T
    ): ShootDetailsResponse<T> =
            when {
                shootId == null || state.isError -> ShootDetailsResponse.Error(state.mainMenuClicked)
                shootId != state.shootId || state.fullShootInfo == null || state.fullShootInfo.id != shootId ->
                    ShootDetailsResponse.Loading

                else -> ShootDetailsResponse.Loaded(
                        data = converter(state, extra),
                        shootId = state.shootId,
                        navBarClicked = state.navBarClickedItem,
                        isCounting = state.fullShootInfo.arrowCounter != null,
                )
            }

    private fun ShootDetailsState.preserveFixedInfo(oldState: ShootDetailsState) =
            copy(
                    useBetaFeatures = oldState.useBetaFeatures,
                    use2023System = oldState.use2023System,
                    useSimpleView = oldState.useSimpleView,
                    archerInfo = oldState.archerInfo,
                    archerHandicaps = oldState.archerHandicaps,
                    bow = oldState.bow,
                    wa1440FullRoundInfo = oldState.wa1440FullRoundInfo,
            )
}

@Module
@InstallIn(SingletonComponent::class)
class ShootDetailsModule {
    @Singleton
    @Provides
    fun provideShootDetailsRepo(
            db: ScoresRoomDatabase,
            datastore: CodexDatastore,
            helpShowcase: HelpShowcaseUseCase,
    ) = ShootDetailsRepo(db, datastore, helpShowcase)
}
