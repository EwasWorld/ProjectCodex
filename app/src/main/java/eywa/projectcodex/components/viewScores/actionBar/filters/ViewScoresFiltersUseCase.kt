package eywa.projectcodex.components.viewScores.actionBar.filters

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Singleton
import kotlin.random.Random

typealias Id = Int

/**
 * Allows [ViewScoresFiltersState] to be accessed from ViewScores and ViewScoresFilters which are separate destinations
 * in the nav graph
 */
class ViewScoresFiltersUseCase {
    private val state = MutableStateFlow(emptyMap<Id, ViewScoresFiltersState>())

    fun getState(id: Id) = state.map { it[id] }

    fun clearState(id: Id) {
        state.update { it.minus(id) }
    }

    fun handle(id: Id, action: ViewScoresFiltersIntent) {
        state.update {
            val state = it[id] ?: return@update it
            it.plus(id to action.handle(state))
        }
    }

    /**
     * Generates new id and state
     */
    fun initialiseNew(): Id {
        synchronized(this) {
            var id: Int
            do {
                id = Random.nextInt()
            } while (state.value.containsKey(id))

            state.update { it.plus(id to ViewScoresFiltersState()) }
            return id
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
object ViewScoresModule {
    @Singleton
    @Provides
    fun provideViewScoresFiltersRepo() = ViewScoresFiltersUseCase()
}
