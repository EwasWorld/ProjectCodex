package eywa.projectcodex.components.shootDetails

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey.Use2023HandicapSystem
import eywa.projectcodex.datastore.DatastoreKey.UseBetaFeatures
import eywa.projectcodex.datastore.get
import eywa.projectcodex.datastore.retrieve
import eywa.projectcodex.model.FullShootInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Singleton

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

    fun handle(action: ShootDetailsIntent, screen: CodexNavRoute) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            ReturnToMenuClicked -> state.update { it.copy(mainMenuClicked = true) }
            ReturnToMenuHandled -> state.update { it.copy(mainMenuClicked = false) }
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
        }
    }

    fun connect(launch: (block: suspend () -> Unit) -> Unit) {
        launch {
            datastore.get(
                    Use2023HandicapSystem,
                    UseBetaFeatures,
            ).collectLatest { result ->
                state.update {
                    it.copy(
                            useBetaFeatures = result.retrieve(UseBetaFeatures),
                            use2023System = result.retrieve(Use2023HandicapSystem),
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
                                else ShootDetailsState(shootId = shootId, isError = true).preserveDatastoreInfo(it)
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
    }

    fun <T> getState(
            shootId: Int?,
            converter: (ShootDetailsState) -> T,
    ): Flow<ShootDetailsResponse<T>> {
        setupState(shootId)
        return state.map { combineStates(shootId, it, null) { s, _ -> converter(s) } }
    }

    fun <T, E> getState(
            shootId: Int?,
            extraFlow: StateFlow<E>,
            converter: (ShootDetailsState, E) -> T,
    ): Flow<ShootDetailsResponse<T>> {
        setupState(shootId)
        return state.combine(extraFlow) { main, extra -> main to extra }
                .map { (main, extra) -> combineStates(shootId, main, extra) { s, e -> converter(s, e!!) } }
    }

    private fun setupState(shootId: Int?) {
        when {
            shootId == null ->
                state.update { ShootDetailsState(isError = true).preserveDatastoreInfo(it) }
            state.value.shootId != shootId ->
                state.update { ShootDetailsState(shootId = shootId).preserveDatastoreInfo(it) }
        }
    }

    private fun <T, E> combineStates(
            shootId: Int?,
            state: ShootDetailsState,
            extra: E?,
            converter: (ShootDetailsState, E?) -> T
    ) =
            when {
                shootId == null || state.isError -> ShootDetailsResponse.Error(state.mainMenuClicked)
                shootId != state.shootId || state.fullShootInfo == null || state.fullShootInfo.id != shootId ->
                    @Suppress("UNCHECKED_CAST")
                    ShootDetailsResponse.Loading as ShootDetailsResponse<T>
                else -> ShootDetailsResponse.Loaded(converter(state, extra), state.shootId, state.navBarClickedItem)
            }

    private fun ShootDetailsState.preserveDatastoreInfo(oldState: ShootDetailsState) =
            copy(useBetaFeatures = oldState.useBetaFeatures, use2023System = oldState.use2023System)
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